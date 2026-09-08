package cn.iocoder.yudao.module.ai.core.workflow.engine.node;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.ModelToolDO;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ModelChatOptions;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ToolSettings;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.DatabaseStatementConfig;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatModelService;
import cn.iocoder.yudao.module.ai.core.chat.service.AiToolService;
import cn.iocoder.yudao.module.ai.core.tools.callback.DatabaseToolCallback;
import cn.iocoder.yudao.module.ai.core.tools.callback.HttpToolCallback;
import cn.iocoder.yudao.module.ai.core.tools.factory.AITool;
import cn.iocoder.yudao.module.ai.core.tools.factory.ToolFactory;
import cn.iocoder.yudao.module.ai.core.tools.skill.SkillsHookFactory;
import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowGraphNode;
import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowRunContext;
import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowVariableResolver;
import cn.iocoder.yudao.module.ai.core.workflow.engine.handler.WorkflowNodeHandlerAdapter;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.AiContent;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ChatTool;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.Hook;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.modelcontextprotocol.client.McpSyncClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 智能体节点：基于 ReactAgent 框架，支持模型调用 + 真实工具执行
 * <p>
 * Agent 节点集成 yudao 工具系统，支持：
 * <ul>
 *   <li>系统内置工具（SYSTEM_TOOL）—— 通过 ToolFactory 加载</li>
 *   <li>MCP 协议工具（MCP_TOOL）—— 通过 SyncMcpToolCallbackProvider 注册</li>
 *   <li>HTTP 动态工具（HTTP_DYNAMIC_TOOL）—— 通过 HttpToolCallback 注册</li>
 * </ul>
 * 工具通过 toolIds 配置，运行时从 AiToolService 加载实际工具实例。
 * 使用 ReactAgent.streamMessages() 进行流式输出，通过 WorkflowRunContext 发送 SSE 事件。
 *
 * @author yudao
 */
@Slf4j
@Component
public class AgentNodeHandler implements WorkflowNodeHandler, WorkflowNodeHandlerAdapter {

    private static final String DEFAULT_OUTPUT = "answer";

    private final AiChatModelService chatModelService;
    private final AiToolService toolService;
    private final ToolFactory toolFactory;
    private final SkillsHookFactory skillsHookFactory;

    public AgentNodeHandler(AiChatModelService chatModelService,
                            AiToolService toolService,
                            ToolFactory toolFactory,
                            SkillsHookFactory skillsHookFactory) {
        this.chatModelService = chatModelService;
        this.toolService = toolService;
        this.toolFactory = toolFactory;
        this.skillsHookFactory = skillsHookFactory;
    }

    @Override
    public String[] types() {
        return new String[]{"agentNode", "agent"};
    }

    @Override
    public Map<String, Object> execute(WorkflowGraphNode node, WorkflowRunContext ctx, Map<String, Object> memory) {
        JSONObject data = node.getData();
        Long modelId = data.getLong("llmId");
        if (modelId == null) {
            throw new IllegalArgumentException("Agent 节点 [%s] 未配置模型".formatted(
                    node.getName() == null ? node.getId() : node.getName()));
        }
        String promptTemplate = data.getString("userPrompt");
        if (!StringUtils.hasText(promptTemplate)) {
            throw new IllegalArgumentException("Agent 节点 [%s] 未配置提示词".formatted(
                    node.getName() == null ? node.getId() : node.getName()));
        }
        String systemPromptTemplate = data.getString("systemPrompt");

        String userPrompt = WorkflowVariableResolver.render(promptTemplate, memory);
        String systemPrompt = WorkflowVariableResolver.render(systemPromptTemplate, memory);

        // 构建 ChatModel
        ModelChatOptions.ModelChatOptionsBuilder optionsBuilder = ModelChatOptions.builder();
        if (data.getDouble("temperature") != null) {
            optionsBuilder.temperature(data.getDouble("temperature"));
        }
        ChatModel chatModel = TenantUtils.executeIgnore(() ->
                chatModelService.getChatModel(modelId, optionsBuilder.build()));

        // 加载工具
        List<ModelToolDO> modelTools = loadTools(data);
        List<String> systemToolNames = new ArrayList<>();
        List<ModelToolDO> mcpTools = new ArrayList<>();
        List<ModelToolDO> httpTools = new ArrayList<>();
        List<ModelToolDO> databaseTools = new ArrayList<>();

        for (ModelToolDO tool : modelTools) {
            switch (tool.getType()) {
                case SYSTEM_TOOL -> systemToolNames.add(tool.getName());
                case MCP_TOOL -> mcpTools.add(tool);
                case HTTP_DYNAMIC_TOOL -> httpTools.add(tool);
                case DATABASE_TOOL -> databaseTools.add(tool);
            }
        }

        // 系统工具实例
        List<Object> toolInstances = toolFactory.getToolInstancesByIds(systemToolNames);

        // 技能支持（参考 AgentStreamProcessor）：当智能体节点配置了技能时，
        // 自动添加技能脚本执行工具，并通过 SkillsAgentHook 加载指定的技能
        List<Long> skillIds = loadSkillIds(data);
        boolean hasSkills = skillsHookFactory.hasSkills(skillIds);
        if (hasSkills) {
            List<Object> mutableTools = new ArrayList<>(toolInstances);
            toolFactory.getToolInstancesByIds(List.of("skill-workspace")).stream()
                    .filter(t -> !mutableTools.contains(t))
                    .forEach(mutableTools::add);
            toolInstances = mutableTools;
        }

        // 预解析"工具调用名(@Tool 方法名，如 getStockPrice) -> 资源 ID"，随 SSE 事件带给前端，
        // 使前端能把工具/技能调用动画准确映射到对应画布资源节点（资源节点以数据库 toolId/skillId 标识）。
        Map<String, String> callNameToResourceId = new HashMap<>();
        {
            Map<String, String> systemNameToId = new HashMap<>();
            for (ModelToolDO tool : modelTools) {
                if (systemToolNames.contains(tool.getName())) {
                    systemNameToId.put(tool.getName(), String.valueOf(tool.getId()));
                }
            }
            for (Object instance : toolInstances) {
                if (!(instance instanceof AITool aiTool)) {
                    continue;
                }
                String beanName = aiTool.getName();
                String resourceId = systemNameToId.get(beanName);
                if (resourceId == null && "skill-workspace".equals(beanName)) {
                    // 技能工作区方法调用视为技能执行；仅当唯一技能时才能唯一定位
                    resourceId = skillIds.size() == 1 ? "skill:" + skillIds.getFirst() : "";
                }
                if (resourceId == null || resourceId.isEmpty()) {
                    continue;
                }
                for (Method m : aiTool.getClass().getDeclaredMethods()) {
                    if (m.getAnnotation(Tool.class) != null) {
                        callNameToResourceId.put(m.getName(), resourceId);
                    }
                }
            }
        }
        // 创建技能 Hook（仅加载节点配置的 skillIds）
        List<Hook> hooks = hasSkills
                ? List.of(TenantUtils.executeIgnore(() -> skillsHookFactory.createHook(skillIds)))
                : List.of();

        // HTTP 工具回调
        List<ToolCallback> toolCallbacks = new ArrayList<>();
        for (ModelToolDO httpTool : httpTools) {
            ToolSettings settings = httpTool.getSettings();
            if (settings != null && settings.getHttpConfigs() != null) {
                for (var httpConfig : settings.getHttpConfigs()) {
                    toolCallbacks.add(new HttpToolCallback(httpConfig, WebClient.builder()));
                }
            }
        }

        // 数据库连接器回调
        for (ModelToolDO databaseTool : databaseTools) {
            ToolSettings settings = databaseTool.getSettings();
            if (settings != null && settings.getDatabaseConfigs() != null) {
                for (var databaseConfig : settings.getDatabaseConfigs()) {
                    if (databaseConfig.getStatements() != null) {
                        for (DatabaseStatementConfig statement : databaseConfig.getStatements()) {
                            toolCallbacks.add(new DatabaseToolCallback(databaseConfig, statement));
                        }
                    }
                    if (Boolean.TRUE.equals(databaseConfig.getAllowFreeQuery())) {
                        toolCallbacks.add(new DatabaseToolCallback(databaseConfig));
                    }
                }
            }
        }

        // MCP 工具回调提供者
        SyncMcpToolCallbackProvider mcpProvider = SyncMcpToolCallbackProvider.builder().build();
        if (!mcpTools.isEmpty()) {
            try {
                List<McpSyncClient> mcpClients = toolService.loadingMcpSyncClients(mcpTools);
                if (mcpClients != null && !mcpClients.isEmpty()) {
                    mcpProvider = SyncMcpToolCallbackProvider.builder()
                            .mcpClients(mcpClients)
                            .build();
                }
            } catch (Exception e) {
                log.warn("[Agent节点] MCP 工具加载失败，将跳过 MCP 工具: {}", e.getMessage());
            }
        }

        // 构建 ReactAgent
        ReactAgent agent = ReactAgent.builder()
                .name(node.getName() != null ? node.getName() : "Agent")
                .description("Workflow Agent Node")
                .model(chatModel)
                .systemPrompt(StringUtils.hasText(systemPrompt) ? systemPrompt : "")
                .methodTools(toolInstances.toArray())
                .toolCallbackProviders(mcpProvider, ToolCallbackProvider.from(toolCallbacks))
                .hooks(hooks)
                .build();

        // 构建消息列表
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage(userPrompt));

        // 流式执行 ReactAgent：按轮次(round)累积 AiContent（思考 + 正文 + 工具），
        // 参照 AgentStreamProcessor 的 output 事件模型推送给前端，避免跨轮覆盖把正文丢失
        List<AiContent> aiContents = new ArrayList<>();        // 各轮内容，index 标识轮次
        List<ChatTool> tools = new ArrayList<>();              // 本次执行产生的全部工具（去重）
        AtomicReference<AiContent> currentAiContent = new AtomicReference<>();
        AtomicBoolean isNewContent = new AtomicBoolean(true);
        AtomicReference<String> lastSentThinking = new AtomicReference<>("");
        AtomicReference<String> lastSentContent = new AtomicReference<>("");
        AtomicReference<List<ChatTool>> lastSentTools = new AtomicReference<>(null);
        // 最终产物（presentFiles 工具生成的文件）：本次执行收集 + 去重，经 AiContent.fileInfos 下发给前端
        AtomicReference<List<Object>> pendingPresentedFiles = new AtomicReference<>(new ArrayList<>());
        Set<String> sentPresentedFileIds = new HashSet<>();

        try {
            Flux<Message> flux = agent.streamMessages(messages)
                    .doOnError(error -> log.error("[Agent节点] ReactAgent 执行错误: {}", error.getMessage(), error));

            // 以"可取消订阅"替代 blockLast()：停止时 dispose 立即中断在途的模型/工具流式请求，
            // 否则执行线程被阻塞到流程彻底结束，模型仍会持续输出。
            AtomicReference<Throwable> streamError = new AtomicReference<>();
            java.util.concurrent.CountDownLatch streamDone = new java.util.concurrent.CountDownLatch(1);
            reactor.core.Disposable streamSubscription = flux.doOnNext(msg -> {
                AiContent aiContent;
                boolean isToolResponse = msg instanceof ToolResponseMessage;

                // 每个"轮次"用一条 AiContent 承载，工具响应后开启新轮
                if (isToolResponse) {
                    aiContent = currentAiContent.get();
                    if (aiContent != null) {
                        isNewContent.set(true);
                    }
                } else {
                    if (isNewContent.get()) {
                        aiContent = new AiContent();
                        aiContents.add(aiContent);
                        currentAiContent.set(aiContent);
                        isNewContent.set(false);
                        lastSentThinking.set("");
                        lastSentContent.set("");
                        lastSentTools.set(null);
                    } else {
                        aiContent = currentAiContent.get();
                    }
                }

                if (msg instanceof AssistantMessage assistantMessage) {
                    // 1) 模型请求调用工具：登记新工具（按 id 去重），跳过无名称的内部调用
                    List<ChatTool> curTools = new ArrayList<>();
                    var toolCalls = assistantMessage.getToolCalls();
                    if (toolCalls != null) {
                        for (var toolCall : toolCalls) {
                            if (!StringUtils.hasText(toolCall.name())) {
                                continue;
                            }
                            String toolId = StringUtils.hasText(toolCall.id()) ? toolCall.id() : UUID.randomUUID().toString();
                            boolean exists = tools.stream().anyMatch(t -> toolId.equals(t.getId()));
                            if (!exists) {
                                ChatTool newTool = ChatTool.builder()
                                        .id(toolId)
                                        .type(toolCall.type())
                                        .name(toolCall.name())
                                        .arguments(toolCall.arguments())
                                        .build();
                                curTools.add(newTool);
                            }
                        }
                        tools.addAll(curTools);
                    }
                    // 实时推送"工具开始调用"事件（作为 Agent 节点的内部步骤，按真实调用返回，而非静态资源节点）
                    if (!curTools.isEmpty()) {
                        for (ChatTool t : curTools) {
                            String resourceId = callNameToResourceId.get(t.getName());
                            Map<String, Object> startedEvt = new HashMap<>(Map.of(
                                    "nodeId", node.getId(),
                                    "nodeName", node.getName() == null ? node.getId() : node.getName(),
                                    "toolId", t.getId(),
                                    "toolName", t.getName(),
                                    "arguments", t.getArguments() == null ? "" : t.getArguments()));
                            if (resourceId != null) {
                                if (resourceId.startsWith("skill:")) {
                                    startedEvt.put("resourceSkillId", resourceId.substring("skill:".length()));
                                } else {
                                    startedEvt.put("resourceToolId", resourceId);
                                }
                            }
                            ctx.sendEvent("agent_tool_started", startedEvt);
                        }
                    }
                    if (aiContent != null) {
                        // 2) 思考（reasoningContent，全量/增量兼容）
                        Object reasoningContent = assistantMessage.getMetadata().get("reasoningContent");
                        if (reasoningContent != null) {
                            String reasoning = reasoningContent.toString();
                            if (aiContent.getThinking() == null) {
                                aiContent.setThinking(new StringBuffer(reasoning));
                            } else {
                                String existing = aiContent.getThinking().toString();
                                if (!existing.isEmpty() && reasoning.startsWith(existing)) {
                                    aiContent.getThinking().replace(0, aiContent.getThinking().length(), reasoning);
                                } else {
                                    aiContent.getThinking().append(reasoning);
                                }
                            }
                        }
                        // 3) 正文（全量/增量兼容）
                        String text = assistantMessage.getText();
                        if (text != null) {
                            if (aiContent.getContent() == null) {
                                aiContent.setContent(new StringBuffer(text));
                            } else {
                                String existing = aiContent.getContent().toString();
                                if (!existing.isEmpty() && text.startsWith(existing)) {
                                    aiContent.getContent().replace(0, aiContent.getContent().length(), text);
                                } else {
                                    aiContent.getContent().append(text);
                                }
                            }
                        }
                        if (!assistantMessage.getToolCalls().isEmpty()) {
                            aiContent.setTools(curTools);
                        }
                    }
                } else if (msg instanceof ToolResponseMessage toolResponseMessage) {
                    // 4) 工具执行结果：回填 responseData，标记思考完成
                    List<ToolResponseMessage.ToolResponse> responses = toolResponseMessage.getResponses();
                    List<ChatTool> resTools = new ArrayList<>();
                    if (responses != null) {
                        boolean hasAllId = responses.stream().noneMatch(r -> StringUtils.hasText(r.id()));
                        if (hasAllId) {
                            int i = 0;
                            for (ChatTool tool : tools) {
                                if (i >= responses.size()) {
                                    break;
                                }
                                ToolResponseMessage.ToolResponse resp = responses.get(i);
                                if (tool.getResponseData() == null && resp != null && resp.name().equals(tool.getName())) {
                                    tool.setResponseData(StringUtils.hasText(resp.responseData()) ? resp.responseData() : "未找到相关结果");
                                    resTools.add(tool);
                                    i++;
                                }
                            }
                        } else {
                            for (ToolResponseMessage.ToolResponse resp : responses) {
                                for (ChatTool tool : tools) {
                                    if (resp.id().equals(tool.getId())) {
                                        tool.setResponseData(StringUtils.hasText(resp.responseData()) ? resp.responseData() : "未找到相关结果");
                                        resTools.add(tool);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    // 实时推送"工具调用完成"事件（作为 Agent 节点的内部步骤）
                    if (!resTools.isEmpty()) {
                        for (ChatTool t : resTools) {
                            String resourceId = callNameToResourceId.get(t.getName());
                            Map<String, Object> finishedEvt = new HashMap<>(Map.of(
                                    "nodeId", node.getId(),
                                    "nodeName", node.getName() == null ? node.getId() : node.getName(),
                                    "toolId", t.getId(),
                                    "toolName", t.getName(),
                                    "responseData", t.getResponseData() == null ? "" : t.getResponseData()));
                            if (resourceId != null) {
                                if (resourceId.startsWith("skill:")) {
                                    finishedEvt.put("resourceSkillId", resourceId.substring("skill:".length()));
                                } else {
                                    finishedEvt.put("resourceToolId", resourceId);
                                }
                            }
                            ctx.sendEvent("agent_tool_finished", finishedEvt);
                        }
                    }
                    // 收集最终产物：presentFiles 工具返回后解析其文件列表，作为该节点的输出产物
                    // （同时写入运行上下文，供 ai_workflow_run 持久化）
                    for (ChatTool t : resTools) {
                        if ("presentFiles".equals(t.getName()) && StringUtils.hasText(t.getResponseData())) {
                            List<Object> files = parsePresentedFiles(t.getResponseData());
                            ctx.addPresentedFiles(files);
                            for (Object file : files) {
                                JSONObject jf = (JSONObject) file;
                                String fid = jf.getString("id");
                                if (fid != null && !sentPresentedFileIds.add(fid)) {
                                    continue; // 已下发过，跳过
                                }
                                pendingPresentedFiles.get().add(file);
                            }
                        }
                    }
                    if (aiContent != null) {
                        aiContent.setTools(resTools);
                        aiContent.setThinkingComplete(true);
                    }
                }

                // 发送当前轮次的增量（thinking/content 为纯增量，tools 为新工具列表）
                AiContent cur = currentAiContent.get();
                if (cur != null) {
                    String fullContent = cur.getContent() != null ? cur.getContent().toString() : "";
                    String fullThinking = cur.getThinking() != null ? cur.getThinking().toString() : "";

                    String contentChunk = fullContent;
                    String thinkingChunk = fullThinking;
                    String prevContent = lastSentContent.get();
                    String prevThinking = lastSentThinking.get();
                    if (prevContent != null && fullContent.startsWith(prevContent)) {
                        contentChunk = fullContent.substring(prevContent.length());
                    }
                    if (prevThinking != null && fullThinking.startsWith(prevThinking)) {
                        thinkingChunk = fullThinking.substring(prevThinking.length());
                    }
                    lastSentContent.set(fullContent);
                    lastSentThinking.set(fullThinking);

                    List<ChatTool> currentTools = cur.getTools();
                    List<ChatTool> prevTools = lastSentTools.get();
                    List<ChatTool> deltaTools = (currentTools != prevTools) ? currentTools : null;
                    if (currentTools != prevTools) {
                        lastSentTools.set(currentTools);
                    }

                    // 最终产物（presentFiles）增量：仅下发本批新增的文件，随后清空待发列表
                    List<Object> pendingFiles = pendingPresentedFiles.get();
                    List<Object> deltaFiles = pendingFiles.isEmpty() ? null : new ArrayList<>(pendingFiles);
                    pendingFiles.clear();

                    AiContent delta = AiContent.builder()
                            .index(aiContents.indexOf(cur))
                            .thinking(!thinkingChunk.isEmpty() ? new StringBuffer(thinkingChunk) : null)
                            .content(!contentChunk.isEmpty() ? new StringBuffer(contentChunk) : null)
                            .tools(deltaTools)
                            .thinkingComplete(cur.getThinkingComplete())
                            .fileInfos(deltaFiles)
                            .build();
                    ctx.sendEvent("output", Map.of(
                            "nodeId", node.getId(),
                            "aiContent", List.of(delta)));
                }

                if (isToolResponse) {
                    currentAiContent.set(null);
                }
            }).doFinally(signal -> streamDone.countDown())
            .subscribe(null, streamError::set, streamDone::countDown);
            ctx.trackStream(streamSubscription);
            try {
                streamDone.await();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            ctx.untrackStream(streamSubscription);

            // 被停止：跳过兜底补发，直接返回当前已累积内容
            if (ctx.getStopped().get()) {
                return Map.of(outputNameOf(node), lastContent(aiContents));
            }
            // 真实异常：抛出交由外层 catch 返回报错内容（区分于停止）
            Throwable streamEx = streamError.get();
            if (streamEx != null) {
                throw new RuntimeException("[Agent节点] ReactAgent 流式调用失败: "
                        + (streamEx.getMessage() == null ? "未知错误" : streamEx.getMessage()), streamEx);
            }

            // 流结束后兜底：对"只有思考没有正文"的轮次（如纯思考收尾轮）补发一次思考完成标记，
            // 防止这类轮次从未触发 tool 响应、也没有正文输出，导致前端一直停留在"思考中"。
            // 已由 tool 响应标记完成（thinkingComplete=true）的轮次会被跳过，避免重复发送。
            for (int i = 0; i < aiContents.size(); i++) {
                AiContent ac = aiContents.get(i);
                if (Boolean.TRUE.equals(ac.getThinkingComplete())) {
                    continue;
                }
                boolean hasThinking = ac.getThinking() != null && ac.getThinking().length() > 0;
                boolean hasContent = ac.getContent() != null && ac.getContent().length() > 0;
                if (hasThinking && !hasContent) {
                    ac.setThinkingComplete(true);
                    AiContent doneDelta = AiContent.builder()
                            .index(i)
                            .thinkingComplete(true)
                            .build();
                    ctx.sendEvent("output", Map.of(
                            "nodeId", node.getId(),
                            "aiContent", List.of(doneDelta)));
                }
            }
        } catch (Exception e) {
            log.error("[Agent节点] ReactAgent 执行出错: {}", e.getMessage(), e);
            // 报错后也要返回报错内容，便于前端在最终结果中展示
            return Map.of(outputNameOf(node), "Agent 节点执行出错: " + (e.getMessage() == null ? "未知错误" : e.getMessage()));
        }

        return Map.of(outputNameOf(node), lastContent(aiContents));
    }

    /**
     * 取最终回答：直接取最后一个轮次的正文（不做遍历拼接）。
     * 若没有轮次或最后轮次无正文，则回退为空字符串。
     */
    private String lastContent(List<AiContent> aiContents) {
        if (aiContents.isEmpty()) {
            return "";
        }
        AiContent last = aiContents.getLast();
        return last.getContent() == null ? "" : last.getContent().toString();
    }

    /**
     * 解析 presentFiles 工具返回的文件列表（JSON 数组字符串）。
     * 返回 {@code [{id, filename, fileSize, fileType, url}, ...]} 的对象列表，
     * 作为"最终产物"下发给前端。解析失败时返回空列表。
     */
    private List<Object> parsePresentedFiles(String responseData) {
        if (!StringUtils.hasText(responseData)) {
            return List.of();
        }
        try {
            List<Object> files = new ArrayList<>();
            JSONArray array = JSONArray.parseArray(responseData);
            if (array != null) {
                for (int i = 0; i < array.size(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    if (obj != null) {
                        files.add(obj);
                    }
                }
            }
            return files;
        } catch (Exception e) {
            log.warn("[Agent节点] 解析 presentFiles 最终产物失败: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 从节点配置中加载工具列表
     */
    private List<ModelToolDO> loadTools(JSONObject data) {
        try {
            List<Long> toolIds = new ArrayList<>();
            JSONArray toolIdsArr = data.getJSONArray("toolIds");
            if (toolIdsArr != null && !toolIdsArr.isEmpty()) {
                for (int i = 0; i < toolIdsArr.size(); i++) {
                    toolIds.add(toolIdsArr.getLong(i));
                }
            }
            if (toolIds.isEmpty()) {
                return List.of();
            }
            return TenantUtils.executeIgnore(() -> toolService.getOnlineToolsByIds(toolIds));
        } catch (Exception e) {
            log.warn("[Agent节点] 加载工具失败: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 从节点配置中加载技能 ID 列表
     */
    private List<Long> loadSkillIds(JSONObject data) {
        try {
            List<Long> skillIds = new ArrayList<>();
            JSONArray skillIdsArr = data.getJSONArray("skillIds");
            if (skillIdsArr != null && !skillIdsArr.isEmpty()) {
                for (int i = 0; i < skillIdsArr.size(); i++) {
                    skillIds.add(skillIdsArr.getLong(i));
                }
            }
            return skillIds;
        } catch (Exception e) {
            log.warn("[Agent节点] 加载技能失败: {}", e.getMessage());
            return List.of();
        }
    }

    private String outputNameOf(WorkflowGraphNode node) {
        JSONArray outputDefs = node.getData() == null ? null : node.getData().getJSONArray("outputDefs");
        if (outputDefs != null && !outputDefs.isEmpty()) {
            String name = outputDefs.getJSONObject(0).getString("name");
            if (StringUtils.hasText(name)) {
                return name;
            }
        }
        return DEFAULT_OUTPUT;
    }
}