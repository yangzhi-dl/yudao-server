package cn.iocoder.yudao.module.ai.core.chat.stream.processor;

import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai.core.chat.service.AiProfessionAppService;
import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import cn.iocoder.yudao.module.system.service.acl.engine.AclDecisionEngine;
import cn.iocoder.yudao.module.ai.core.chat.enums.AgentStatus;
import cn.iocoder.yudao.module.ai.core.chat.enums.AiApiType;
import cn.iocoder.yudao.module.ai.core.chat.enums.ToolType;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiAgentDO;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.ModelToolDO;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.*;
import cn.iocoder.yudao.module.ai.core.chat.service.AiAgentService;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatModelService;
import cn.iocoder.yudao.module.ai.core.chat.service.AiToolService;
import cn.iocoder.yudao.module.ai.core.chat.stream.AiStreamProcessor;
import cn.iocoder.yudao.module.ai.core.chat.stream.StreamContext;
import cn.iocoder.yudao.module.ai.core.chat.stream.UsageTrackingChatModel;
import cn.iocoder.yudao.module.ai.core.chat.utils.ChatContentUtil;
import cn.iocoder.yudao.module.ai.core.chat.utils.ChatToolUtil;
import cn.iocoder.yudao.module.ai.core.tools.callback.DatabaseToolCallback;
import cn.iocoder.yudao.module.ai.core.tools.callback.HttpToolCallback;
import cn.iocoder.yudao.module.ai.core.tools.factory.ToolFactory;
import cn.iocoder.yudao.module.ai.core.tools.skill.SkillsHookFactory;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.Hook;
import com.alibaba.cloud.ai.graph.agent.hook.skills.SkillsAgentHook;
import io.modelcontextprotocol.client.McpSyncClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.common.constans.ErrorCodeConstants.NO_EXECUTE_PERMISSION;

/**
 * 多智能体流式处理策略 —— 基于 DeepAgent 模式实现
 *
 * <p>架构说明：
 * <ul>
 *   <li>协调者(Orchestrator)：一个 ReactAgent，拥有 task 工具和自身工具，通过 ReAct 循环自主决策调用哪个子智能体</li>
 *   <li>子智能体(Sub-Agent)：独立的 ReactAgent，各自拥有专属的工具集和系统提示词</li>
 *   <li>委托机制：协调者通过 task(subagentName, description) 工具将任务委托给子智能体，子智能体完成后返回结果</li>
 * </ul>
 *
 * <p>与 LangChain DeepAgents 的对应关系：
 * <ul>
 *   <li>DeepAgents.create_deep_agent(subagents=[...]) → 本类的 DeepAgent 编排</li>
 *   <li>SubAgentMiddleware.task(description, subagent_type) → SubAgentTaskTool.task()</li>
 *   <li>CompiledSubAgent → 每个 ReactAgent 子智能体</li>
 * </ul>
 *
 */
@Slf4j
@Component
@DataPermission(enable = false)
public class MultiAgentStreamProcessor implements AiStreamProcessor {

    private final ChatContentUtil chatContentUtil;
    private final ToolFactory toolFactory;
    private final AiAgentService agentService;
    private final AiChatModelService chatModelService;
    private final ChatToolUtil chatToolUtil;
    private final AiToolService toolService;
    private final AiProfessionAppService professionAppService;
    private final AclDecisionEngine aclDecisionEngine;
    private final SkillsHookFactory skillsHookFactory;

    public MultiAgentStreamProcessor(ChatContentUtil chatContentUtil, ToolFactory toolFactory,
                                     AiAgentService agentService, AiChatModelService chatModelService,
                                     ChatToolUtil chatToolUtil, AiToolService toolService,
                                     AiProfessionAppService professionAppService,
                                     AclDecisionEngine aclDecisionEngine,
                                     SkillsHookFactory skillsHookFactory) {
        this.chatContentUtil = chatContentUtil;
        this.toolFactory = toolFactory;
        this.agentService = agentService;
        this.chatModelService = chatModelService;
        this.chatToolUtil = chatToolUtil;
        this.toolService = toolService;
        this.professionAppService = professionAppService;
        this.aclDecisionEngine = aclDecisionEngine;
        this.skillsHookFactory = skillsHookFactory;
    }

    @Override
    public boolean supports(AiApiType apiType) {
        return Objects.equals(apiType, AiApiType.MULTI_AGENT);
    }

    @Override
    public void processStream(StreamContext ctx) {
        String filePrompt = ctx.getFileContext() != null && !ctx.getFileContext().isEmpty()
                ? new SystemPromptTemplate("""
                --- 上传的文件信息 ---
                {context}
                ----------------""").createMessage(Map.of("context", ctx.getFileContext())).getText()
                : "";
        processStream(ctx.getUuid(), ctx.getEndpointId(), filePrompt,
                ctx.getMessages(), ctx.getMessageData(), ctx.getEmitter(), ctx.getMsgMap(),
                ctx.isAgentPermissionValidated());
    }

    /**
     * DeepAgent 多智能体流式处理入口
     */
    private void processStream(Long uuid, Long endpointId,
                               String filePrompt, List<Message> messages,
                               MessageData messageData, SseEmitter emitter,
                               Map<Long, MessageData> msgMap,
                               boolean agentPermissionValidated) {

        if (!agentPermissionValidated
                && !aclDecisionEngine.canAccess(ResourceType.AGENT, endpointId, Permission.EXECUTE)) {
            throw exception(NO_EXECUTE_PERMISSION);
        }

        AiAgentDO agent = agentService.selectAccessibleAgentById(endpointId);
        ChatModel chatModel = TenantUtils.executeIgnore(() -> chatModelService.getChatModel(agent.getModelId()));
        UsageTrackingChatModel trackingModel = new UsageTrackingChatModel(chatModel);

        // 收集所有工具ID（智能体级 + 各子智能体级）
        Set<Long> allToolIds = new HashSet<>();
        if (agent.getTools() != null) allToolIds.addAll(agent.getTools());
        List<MultiAgentSettings> settings = agent.getSettings();
        for (MultiAgentSettings setting : settings) {
            if (setting.getTools() != null) allToolIds.addAll(setting.getTools());
        }

        // 一次性批量查询所有工具
        List<ModelToolDO> allModelTools = TenantUtils.executeIgnore(() -> toolService.getOnlineToolsByIds(new ArrayList<>(allToolIds)));
        Map<Long, ModelToolDO> toolMap = allModelTools.stream()
                .collect(Collectors.toMap(ModelToolDO::getId, Function.identity()));

        ToolProcessor toolProcessor = new ToolProcessor(toolMap);

        // 第一步：预填充 ID 映射（后续构建 documentStore 和子智能体提示词需要）
        Map<String, String> idToNameMap = new LinkedHashMap<>();   // agentId -> displayName（中文）
        Map<String, String> nameToIdMap = new LinkedHashMap<>();    // displayName -> agentId（反向映射）
        for (MultiAgentSettings multiAgentSettings : settings) {
            String agentId = multiAgentSettings.getEnName();
            if (agentId == null || agentId.isBlank()) {
                throw new IllegalStateException("子智能体未配置英文名，请先配置后再使用集群智能体");
            }
            idToNameMap.put(agentId, multiAgentSettings.getName());
            nameToIdMap.put(multiAgentSettings.getName(), agentId);
        }

        // 协调者分析内容缓存 与 子智能体输出缓存
        AiAgentMessage aiAgentMessage = new AiAgentMessage();
        // 立刻设置到 messageData，确保 stop 时 saveAiDialogueAndComplete 能读取到已累积的内容
        messageData.setAiAgentMessage(aiAgentMessage);
        msgMap.put(uuid, messageData);
        Map<String, AiAgentContent> subAgentOutputMap = Collections.synchronizedMap(new LinkedHashMap<>());

        // 主智能体分段索引：每经过一轮子智能体后自增，确保主智能体前后输出分属不同段落
        int[] coordinatorSegmentIdx = {0};

        // SSE 增量发送状态
        Map<String, Map<Integer, String[]>> lastSentMap = Collections.synchronizedMap(new HashMap<>());
        Map<String, Map<Integer, List<ChatTool>>> lastSentToolsMap = Collections.synchronizedMap(new HashMap<>());
        Map<String, Boolean> lastIsCompleteMap = Collections.synchronizedMap(new HashMap<>());

        // 渐进式披露文档：共享状态存储（子智能体可互相读取已完成同行的输出）
        SubAgentDocumentStore documentStore = new SubAgentDocumentStore();

        // 预计算可用子智能体列表（供子智能体和协调者提示词使用）
        String availableAgentList = settings.stream()
                .map(s -> {
                    String agentId = nameToIdMap.get(s.getName());
                    return String.format("- **%s**（%s）：%s", agentId, s.getName(), s.getDescription());
                })
                .collect(Collectors.joining("\n"));

        // 第二步：构建子智能体 ReactAgent 列表
        List<ReactAgent> subAgents = new ArrayList<>();
        for (MultiAgentSettings setting : settings) {
            ToolProcessor.ToolConfig subConfig = toolProcessor.process(setting.getTools());

            // 使用子智能体英文名作为唯一 ID
            String agentId = setting.getEnName();

            // 构建子智能体工具列表（含渐进式披露文档工具，每个子智能体持有独立实例以便识别调用者）
            List<Object> subMethodTools = new ArrayList<>(subConfig.getToolInstances());
            subMethodTools.add(new SubAgentDocumentTool(documentStore, agentId, idToNameMap));

            // 当子智能体配置了技能时，自动添加技能脚本执行工具
            if (skillsHookFactory.hasSkills(setting.getSkillIds())) {
                toolFactory.getToolInstancesByIds(List.of("skill-workspace")).stream()
                        .filter(t -> !subMethodTools.contains(t))
                        .forEach(subMethodTools::add);
            }

            String prompt = setting.getPrompt() + filePrompt + String.format("""
                    
                    ## 可用工具
                    %s
                    
                    每个工具都有特定的功能和参数要求，请严格按照格式调用。
                    
                    ## 信息共享（渐进式披露）
                    你可以使用 `readSubAgentOutput` 工具读取其他子智能体已经完成的收集内容。
                    当你需要参考他人成果、避免重复劳动、或者基于已有信息进行深入分析时，请主动调用该工具。
                    注意：你不能读取自己的输出，只能读取其他子智能体的成果。
                    
                    如需了解当前有哪些同僚专家及其状态，可调用 `listAvailablePeers` 工具获取实时列表。
                    """, subConfig.getPromptText());

            // 创建子智能体技能 Hook
            SkillsAgentHook subSkillsHook = TenantUtils.executeIgnore(() -> skillsHookFactory.createHook(setting.getSkillIds()));
            List<Hook> subHooks = skillsHookFactory.hasSkills(setting.getSkillIds())
                    ? List.of(subSkillsHook)
                    : List.of();

            ReactAgent subAgent = ReactAgent.builder()
                    .name(agentId)
                    .description(setting.getDescription())
                    .model(trackingModel)
                    .systemPrompt(prompt)
                    .methodTools(subMethodTools.toArray())
                    .toolCallbackProviders(subConfig.getMcpCallbackProvider(),
                            ToolCallbackProvider.from(subConfig.getToolCallbacks()))
                    .hooks(subHooks)
                    .build();

            subAgents.add(subAgent);
        }

        // 构建子智能体委托工具（DeepAgent 核心：task 工具）
        SubAgentTaskTool taskTool = new SubAgentTaskTool(subAgents, idToNameMap,
                subAgentOutputMap, lastSentMap, lastSentToolsMap, lastIsCompleteMap,
                aiAgentMessage, documentStore, coordinatorSegmentIdx);
        taskTool.setStreamingContext(uuid, messageData, emitter, msgMap);

        // 处理协调者自身的工具
        ToolProcessor.ToolConfig coordinatorConfig = toolProcessor.process(agent.getTools());

        // 协调者系统提示词（DeepAgent 模式）
        String coordinatorPrompt = String.format("""
                # 系统角色
                你是一个多智能体协调者(Orchestrator)。你管理着一个专家团队，每个专家都是独立的AI智能体。
                
                ## 你的职责
                1. 分析用户的请求，理解其意图和复杂度
                2. 对于简单任务，使用你自己的工具直接处理
                3. 对于复杂或需要专业知识的任务，使用 `task` 工具委托给合适的专家子智能体
                4. 综合所有结果，给出完整、连贯的最终回答
                5. 你可以多次调用 `task` 工具，让不同专家处理不同子任务
                
                ## 可用专家子智能体
                %s
                
                ## 委托规则
                - 使用 `task` 工具进行委托，参数说明：
                  - subagentName: 子智能体的唯一ID（必须从上述列表的粗体标识中选择，如 sub_analyst）
                  - description: 给子智能体的详细任务描述，越具体越好
                - `task` 是非阻塞的：首次委派立即返回，子智能体在后台异步执行。
                  你可以一次性委派给多个专家，它们会并行运行、同时输出。
                - 委派后，在同一轮 ReAct 中继续委派其他专家，无需等待上一个完成。
                - 子智能体完成后，再次调用 `task` 或 `readSubAgentOutput` 即可获取结果。
                - 子智能体之间可以通过 `readSubAgentOutput` 工具互相读取已完成同行的成果，
                  因此在 description 中可以提示子智能体在需要时参考其他专家的输出
                
                ## 你的背景能力
                %s
                
                ## 输出要求
                - 最终回答要综合所有子智能体的结果
                - 回答要专业、完整、有条理
                - 如果你自己能处理，就不需要委托
                """, availableAgentList, agent.getPrompt()) + filePrompt;

        // 构建协调者 ReactAgent（含 task 工具 + 自身工具 + 技能）
        List<Object> coordinatorTools = new ArrayList<>(coordinatorConfig.getToolInstances());
        coordinatorTools.add(taskTool);

        // 当协调者配置了技能时，自动添加技能脚本执行工具
        if (skillsHookFactory.hasSkills(agent.getSkillIds())) {
            toolFactory.getToolInstancesByIds(List.of("skill-workspace")).stream()
                    .filter(t -> !coordinatorTools.contains(t))
                    .forEach(coordinatorTools::add);
        }

        // 创建协调者技能 Hook
        SkillsAgentHook coordinatorSkillsHook = TenantUtils.executeIgnore(() -> skillsHookFactory.createHook(agent.getSkillIds()));
        List<Hook> coordinatorHooks = skillsHookFactory.hasSkills(agent.getSkillIds())
                ? List.of(coordinatorSkillsHook)
                : List.of();

        ReactAgent coordinatorAgent = ReactAgent.builder()
                .name(agent.getName())
                .description(agent.getDescription())
                .model(trackingModel)
                .systemPrompt(coordinatorPrompt)
                .methodTools(coordinatorTools.toArray())
                .toolCallbackProviders(coordinatorConfig.getMcpCallbackProvider(),
                        ToolCallbackProvider.from(coordinatorConfig.getToolCallbacks()))
                .hooks(coordinatorHooks)
                .build();

        // DeepAgent 流式执行
        new Thread(() -> {
            try {
                professionAppService.updateStatus(endpointId, AgentStatus.RUNNING);

                // 协调者增量发送状态
                Map<Integer, String[]> coordinatorLastSent = new ConcurrentHashMap<>();
                Map<Integer, List<ChatTool>> coordinatorLastTools = new ConcurrentHashMap<>();

                // 工具 ID 到 ChatTool 的映射
                Map<String, ChatTool> chatToolMap = new ConcurrentHashMap<>();

                // task 工具调用计数（追踪子智能体委托次数）
                int[] toolCallCount = {0};

                // 追踪协调者当前 AiContent（用于 ToolResponseMessage 时匹配工具响应）
                AtomicReference<AiContent> currentCoordinatorAiContent = new AtomicReference<>();

                // 运行协调者 ReAct 循环
                messageData.setStreamDisposable(
                    coordinatorAgent.streamMessages(messages)
                        .doOnNext(msg -> {
                            try {
                                if (msg instanceof AssistantMessage am) {
                                    handleCoordinatorAssistantMessage(uuid, am, aiAgentMessage,
                                            idToNameMap, chatToolMap, coordinatorLastSent, coordinatorLastTools,
                                            toolCallCount, coordinatorSegmentIdx, messageData, emitter,
                                            currentCoordinatorAiContent);
                                } else if (msg instanceof ToolResponseMessage trm) {
                                    handleCoordinatorToolResponse(uuid, trm, aiAgentMessage,
                                            coordinatorSegmentIdx, chatToolMap, coordinatorLastSent,
                                            coordinatorLastTools, messageData, emitter,
                                            currentCoordinatorAiContent);
                                }
                            } catch (Exception e) {
                                log.error("协调者消息处理出错: {}", e.getMessage(), e);
                            }
                        })
                        .doOnError(error -> {
                            log.error("DeepAgent 执行出错: {}", error.getMessage(), error);
                            chatContentUtil.saveAiDialogueAndComplete(msgMap, uuid);
                            professionAppService.updateStatus(endpointId, AgentStatus.ERROR);
                        })
                        .doOnComplete(() -> {
                            try {
                                messageData.setPromptTokens(trackingModel.getPromptTokens());
                                messageData.setCompletionTokens(trackingModel.getCompletionTokens());
                                messageData.setTotalTokens(trackingModel.getTotalTokens());
                                log.info("Multi-Agent 本次真实 Token -> prompt: {}, completion: {}, total: {}",
                                        trackingModel.getPromptTokens(), trackingModel.getCompletionTokens(), trackingModel.getTotalTokens());
                                messageData.setAiAgentMessage(aiAgentMessage);
                                msgMap.put(uuid, messageData);
                                chatContentUtil.saveAiDialogueAndComplete(msgMap, uuid);
                                professionAppService.updateStatus(endpointId, AgentStatus.IDLE);
                            } catch (Exception e) {
                                log.error("DeepAgent 完成事件发送出错: {}", e.getMessage(), e);
                            }
                        })
                        .subscribe());
            } catch (Exception e) {
                log.error("DeepAgent 启动失败: {}", e.getMessage(), e);
                chatContentUtil.saveAiDialogueAndComplete(msgMap, uuid);
                professionAppService.updateStatus(endpointId, AgentStatus.ERROR);
            }
        }).start();
    }

    /**
     * 处理协调者的 AssistantMessage（含分段管理，支持多子智能体交错输出）
     */
    private void handleCoordinatorAssistantMessage(Long uuid, AssistantMessage am,
                                                    AiAgentMessage aiAgentMessage,
                                                    @SuppressWarnings("unused") Map<String, String> subAgentNameMap,
                                                    Map<String, ChatTool> chatToolMap,
                                                    Map<Integer, String[]> coordinatorLastSent,
                                                    Map<Integer, List<ChatTool>> coordinatorLastTools,
                                                    int[] toolCallCount,
                                                    int[] coordinatorSegmentIdx,
                                                    MessageData messageData, SseEmitter emitter,
                                                    AtomicReference<AiContent> currentCoordinatorAiContent) throws Exception {
        // 处理推理内容（存入 aiContent 的首个块）
        Object reasoning = am.getMetadata().get("reasoningContent");

        // 流式文本 chunk（由前端做累积拼接）
        String text = am.getText();
        boolean hasToolCalls = !am.getToolCalls().isEmpty();

        // 获取或创建当前主智能体分段
        String segmentId = "main_" + coordinatorSegmentIdx[0];
        List<AiAgentContent> agentContentList = aiAgentMessage.getAgentContentList();
        if (agentContentList == null) {
            agentContentList = new ArrayList<>();
            aiAgentMessage.setAgentContentList(agentContentList);
        }
        AiAgentContent coordinatorSegment;
        if (!agentContentList.isEmpty()) {
            AiAgentContent last = agentContentList.getLast();
            if (segmentId.equals(last.getId())) {
                coordinatorSegment = last;
            } else {
                coordinatorSegment = AiAgentContent.builder().id(segmentId).name("主智能体").isComplete(false).build();
                agentContentList.add(coordinatorSegment);
            }
        } else {
            coordinatorSegment = AiAgentContent.builder().id(segmentId).name("主智能体").isComplete(false).build();
            agentContentList.add(coordinatorSegment);
        }

        List<AiContent> aiContents = coordinatorSegment.getAiContent();
        if (aiContents == null) {
            aiContents = new ArrayList<>();
            coordinatorSegment.setAiContent(aiContents);
        }

        // 处理工具调用
        if (hasToolCalls) {
            List<ChatTool> chatTools = new ArrayList<>();
            am.getToolCalls().forEach(tc -> {
                ChatTool ct = new ChatTool();
                ct.setId(tc.id());
                ct.setName(tc.name());
                ct.setArguments(tc.arguments());
                chatTools.add(ct);
                chatToolMap.put(tc.id(), ct);

                // 追踪是否是 task 工具调用
                if ("task".equals(tc.name())) {
                    toolCallCount[0]++;
                }
            });

            // 记录工具调用到当前协调者分段（过滤 task 工具：内部编排，不传输到 SSE）
            List<ChatTool> visibleTools = chatTools.stream()
                    .filter(t -> !"task".equals(t.getName()))
                    .collect(Collectors.toList());
            AiContent aiContent = new AiContent();
            aiContent.setContent(new StringBuffer(text != null ? text : ""));
            if (reasoning != null) {
                aiContent.setThinking(new StringBuffer(reasoning.toString()));
            }
            aiContent.setTools(visibleTools.isEmpty() ? null : visibleTools);
            aiContents.add(aiContent);
            // 追踪当前 AiContent，供 ToolResponseMessage 处理时匹配工具响应
            if (!visibleTools.isEmpty()) {
                currentCoordinatorAiContent.set(aiContent);
            }
        } else if (text != null && !text.isEmpty()) {
            // 无工具调用的纯文本：存入当前分段的增量块
            if (!aiContents.isEmpty() && aiContents.getLast().getTools() == null) {
                AiContent last = aiContents.getLast();
                StringBuffer existingContent = last.getContent();
                if (existingContent != null) {
                    String existing = existingContent.toString();
                    if (!existing.isEmpty() && text.startsWith(existing)) {
                        // 全量模式：框架返回的是累计文本，直接替换
                        existingContent.replace(0, existingContent.length(), text);
                    } else {
                        // 增量模式：框架返回的是增量文本，追加
                        existingContent.append(text);
                    }
                } else {
                    last.setContent(new StringBuffer(text));
                }
                if (reasoning != null) {
                    String reasoningStr = reasoning.toString();
                    StringBuffer existingThinking = last.getThinking();
                    if (existingThinking != null) {
                        String existing = existingThinking.toString();
                        if (!existing.isEmpty() && reasoningStr.startsWith(existing)) {
                            // 全量模式：框架返回的是累计文本，直接替换
                            existingThinking.replace(0, existingThinking.length(), reasoningStr);
                        } else {
                            // 增量模式：框架返回的是增量文本，追加
                            existingThinking.append(reasoningStr);
                        }
                    } else {
                        last.setThinking(new StringBuffer(reasoningStr));
                    }
                }
            } else {
                AiContent newBlock = new AiContent();
                newBlock.setContent(new StringBuffer(text));
                if (reasoning != null) {
                    newBlock.setThinking(new StringBuffer(reasoning.toString()));
                }
                aiContents.add(newBlock);
            }
        }

        // 发送协调者增量
        sendCoordinatorDelta(uuid, coordinatorSegment, segmentId, coordinatorLastSent,
                coordinatorLastTools, messageData, emitter);
    }

    /**
     * 发送协调者增量内容（按分段发送）
     */
    private void sendCoordinatorDelta(Long uuid, AiAgentContent coordinatorSegment, String segmentId,
                                       Map<Integer, String[]> coordinatorLastSent,
                                       Map<Integer, List<ChatTool>> coordinatorLastTools,
                                       MessageData messageData, SseEmitter emitter) throws Exception {
        List<AiContent> aiContents = coordinatorSegment.getAiContent();
        if (aiContents == null || aiContents.isEmpty()) {
            return;
        }

        // 增量发送
        List<AiContent> deltaContents = new ArrayList<>();
        for (int i = 0; i < aiContents.size(); i++) {
            AiContent item = aiContents.get(i);
            String fullContent = item.getContent() != null ? item.getContent().toString() : "";
            String fullThinking = item.getThinking() != null ? item.getThinking().toString() : "";

            String[] prev = coordinatorLastSent.get(i);
            String contentChunk = fullContent;
            String thinkingChunk = fullThinking;

            if (prev != null) {
                if (prev[0] != null && fullContent.startsWith(prev[0])) {
                    contentChunk = fullContent.substring(prev[0].length());
                }
                if (prev[1] != null && fullThinking.startsWith(prev[1])) {
                    thinkingChunk = fullThinking.substring(prev[1].length());
                }
            }

            coordinatorLastSent.put(i, new String[]{fullContent, fullThinking});

            boolean hasContentDelta = !contentChunk.isEmpty() || !thinkingChunk.isEmpty();
            List<ChatTool> currentTools = item.getTools();
            List<ChatTool> prevTools = coordinatorLastTools.get(i);
            boolean toolsChanged = currentTools != null && !currentTools.isEmpty() && currentTools != prevTools;
            if (toolsChanged) {
                coordinatorLastTools.put(i, currentTools);
            }

            if (hasContentDelta || toolsChanged) {
                deltaContents.add(AiContent.builder()
                        .index(i)
                        .thinking(!thinkingChunk.isEmpty() ? new StringBuffer(thinkingChunk) : null)
                        .content(!contentChunk.isEmpty() ? new StringBuffer(contentChunk) : null)
                        .tools(toolsChanged ? currentTools : null)
                        .build());
            }
        }

        if (!deltaContents.isEmpty()) {
            AiAgentContent deltaSegment = AiAgentContent.builder()
                    .id(segmentId)
                    .name("主智能体")
                    .aiContent(deltaContents)
                    .build();
            AiAgentMessage deltaMsg = AiAgentMessage.builder()
                    .agentContentList(List.of(deltaSegment))
                    .build();
            emitter.send(SseEmitter.event().name("output")
                    .id(String.valueOf(uuid))
                    .data(SendOutputData.builder()
                            .topicId(messageData.getTopicId())
                            .aiAgentMessage(deltaMsg)
                            .build()));
        }
    }

    /**
     * 处理协调者的 ToolResponseMessage —— 将工具执行结果写入对应的 ChatTool，
     * 并发送增量更新给前端，解决主智能体工具调用一直显示"执行中"的问题。
     */
    private void handleCoordinatorToolResponse(Long uuid, ToolResponseMessage trm,
                                                AiAgentMessage aiAgentMessage,
                                                int[] coordinatorSegmentIdx,
                                                Map<String, ChatTool> chatToolMap,
                                                Map<Integer, String[]> coordinatorLastSent,
                                                Map<Integer, List<ChatTool>> coordinatorLastTools,
                                                MessageData messageData, SseEmitter emitter,
                                                AtomicReference<AiContent> currentCoordinatorAiContent) throws Exception {
        // 更新 chatToolMap 中的工具响应数据
        trm.getResponses().forEach(resp -> {
            ChatTool tool = chatToolMap.get(resp.id());
            if (tool != null) {
                tool.setResponseData(resp.responseData());
            }
        });

        // 获取当前协调者分段
        String segmentId = "main_" + coordinatorSegmentIdx[0];
        List<AiAgentContent> agentContentList = aiAgentMessage.getAgentContentList();
        if (agentContentList == null) {
            return;
        }
        AiAgentContent coordinatorSegment = null;
        for (AiAgentContent seg : agentContentList) {
            if (segmentId.equals(seg.getId())) {
                coordinatorSegment = seg;
                break;
            }
        }
        if (coordinatorSegment == null) {
            return;
        }

        // 更新当前 AiContent 的工具列表引用（触发 sendCoordinatorDelta 的 toolsChanged 检测）
        AiContent current = currentCoordinatorAiContent.get();
        if (current != null && current.getTools() != null && !current.getTools().isEmpty()) {
            // 检查是否有工具响应匹配当前 AiContent 中的工具
            boolean hasMatch = trm.getResponses().stream()
                    .anyMatch(resp -> current.getTools().stream()
                            .anyMatch(t -> Objects.equals(t.getId(), resp.id())));
            if (hasMatch) {
                // 重新 setTools 创建新列表引用，触发 toolsChanged 检测
                current.setTools(new ArrayList<>(current.getTools()));
                // 工具响应到达，标记思考完成
                current.setThinkingComplete(true);
            }
        }

        // 发送增量更新给前端
        sendCoordinatorDelta(uuid, coordinatorSegment, segmentId, coordinatorLastSent,
                coordinatorLastTools, messageData, emitter);
    }

    /**
     * 发送子智能体增量消息（与原实现保持一致）
     */
    private void sendDeltaMessage(Long uuid, String agentId, AiAgentContent agentContent,
                                   Map<String, Map<Integer, String[]>> lastSentMap,
                                   Map<String, Map<Integer, List<ChatTool>>> lastSentToolsMap,
                                   Map<String, Boolean> lastIsCompleteMap,
                                   MessageData messageData, SseEmitter emitter) throws Exception {
        List<AiContent> aiContents = agentContent.getAiContent();
        if (aiContents != null && !aiContents.isEmpty()) {
            Map<Integer, String[]> agentLastSent = lastSentMap.computeIfAbsent(agentId, k -> new ConcurrentHashMap<>());
            List<AiContent> deltaContents = new ArrayList<>();

            for (int i = 0; i < aiContents.size(); i++) {
                AiContent item = aiContents.get(i);
                String fullContent = item.getContent() != null ? item.getContent().toString() : "";
                String fullThinking = item.getThinking() != null ? item.getThinking().toString() : "";

                String[] prev = agentLastSent.get(i);
                String contentChunk = fullContent;
                String thinkingChunk = fullThinking;

                if (prev != null) {
                    if (prev[0] != null && fullContent.startsWith(prev[0])) {
                        contentChunk = fullContent.substring(prev[0].length());
                    }
                    if (prev[1] != null && fullThinking.startsWith(prev[1])) {
                        thinkingChunk = fullThinking.substring(prev[1].length());
                    }
                }

                agentLastSent.put(i, new String[]{fullContent, fullThinking});

                boolean hasContentDelta = !contentChunk.isEmpty() || !thinkingChunk.isEmpty();
                List<ChatTool> currentTools = item.getTools();
                Map<Integer, List<ChatTool>> agentLastTools = lastSentToolsMap.computeIfAbsent(agentId, k -> new ConcurrentHashMap<>());
                List<ChatTool> prevTools = agentLastTools.get(i);
                boolean toolsChanged = currentTools != null && !currentTools.isEmpty() && currentTools != prevTools;
                if (toolsChanged) {
                    agentLastTools.put(i, currentTools);
                }
                if (hasContentDelta || toolsChanged) {
                    deltaContents.add(AiContent.builder()
                            .index(i)
                            .thinking(!thinkingChunk.isEmpty() ? new StringBuffer(thinkingChunk) : null)
                            .content(!contentChunk.isEmpty() ? new StringBuffer(contentChunk) : null)
                            .tools(toolsChanged ? currentTools : null)
                            .build());
                }
            }

            Boolean currentComplete = agentContent.getIsComplete();
            Boolean lastComplete = lastIsCompleteMap.get(agentId);
            boolean completeChanged = currentComplete != null && !currentComplete.equals(lastComplete);
            if (completeChanged) {
                lastIsCompleteMap.put(agentId, currentComplete);
            }

            if (!deltaContents.isEmpty() || completeChanged) {
                AiAgentContent deltaAgent = AiAgentContent.builder()
                        .id(agentContent.getId())
                        .name(agentContent.getName())
                        .command(agentContent.getCommand())
                        .isComplete(agentContent.getIsComplete())
                        .aiContent(deltaContents)
                        .build();
                AiAgentMessage deltaMsg = AiAgentMessage.builder()
                        .agentContentList(List.of(deltaAgent))
                        .build();
                emitter.send(SseEmitter.event().name("output")
                        .id(String.valueOf(uuid)).data(SendOutputData.builder()
                                .topicId(messageData.getTopicId()).aiAgentMessage(deltaMsg).build()));
            }
        } else {
            AiAgentContent deltaAgent = AiAgentContent.builder()
                    .id(agentContent.getId())
                    .name(agentContent.getName())
                    .command(agentContent.getCommand())
                    .isComplete(agentContent.getIsComplete())
                    .build();
            AiAgentMessage deltaMsg = AiAgentMessage.builder()
                    .agentContentList(List.of(deltaAgent))
                    .build();
            emitter.send(SseEmitter.event().name("output")
                    .id(String.valueOf(uuid)).data(SendOutputData.builder()
                            .topicId(messageData.getTopicId()).aiAgentMessage(deltaMsg).build()));
        }
    }

    // ==================== DeepAgent 核心：子智能体委托工具 ====================

    /**
     * 子智能体委托工具 —— DeepAgent 的核心机制
     * <p>
     * 协调者通过调用 task(subagentName, description) 将任务委托给子智能体。
     * 对应 LangChain DeepAgents 的 SubAgentMiddleware.task() 机制。
     * <p>
     * 工作流程：
     * <ol>
     *   <li>协调者决定调用哪个子智能体 → 调用 task("expert_name", "task description")</li>
     *   <li>task 工具找到对应的子智能体 ReactAgent</li>
     *   <li>子智能体以自己的 ReAct 循环处理任务，实时流式输出到前端</li>
     *   <li>返回子智能体的最终文本结果给协调者</li>
     * </ol>
     */
    class SubAgentTaskTool {

        private final Map<String, ReactAgent> subAgents;
        private final Map<String, String> nameMap;
        private final Map<String, AiAgentContent> subAgentOutputMap;
        private final Map<String, Map<Integer, String[]>> lastSentMap;
        private final Map<String, Map<Integer, List<ChatTool>>> lastSentToolsMap;
        private final Map<String, Boolean> lastIsCompleteMap;
        private final AiAgentMessage aiAgentMessage;
        private final SubAgentDocumentStore documentStore;
        private final int[] coordinatorSegmentIdx;

        /**
         * 子智能体异步执行结果。当首次委派时启动异步执行并存储 Future，后续调用可等待结果。
         */
        private final ConcurrentHashMap<String, CompletableFuture<String>> subAgentFutures = new java.util.concurrent.ConcurrentHashMap<>();

        // 流式上下文（由 processStream 注入）
        private volatile Long uuid;
        private volatile MessageData messageData;
        private volatile SseEmitter emitter;
        private volatile Map<Long, MessageData> msgMap;

        SubAgentTaskTool(List<ReactAgent> subAgents, Map<String, String> nameMap,
                         Map<String, AiAgentContent> subAgentOutputMap,
                         Map<String, Map<Integer, String[]>> lastSentMap,
                         Map<String, Map<Integer, List<ChatTool>>> lastSentToolsMap,
                         Map<String, Boolean> lastIsCompleteMap,
                         AiAgentMessage aiAgentMessage,
                         SubAgentDocumentStore documentStore,
                         int[] coordinatorSegmentIdx) {
            this.subAgents = subAgents.stream()
                    .collect(Collectors.toMap(ReactAgent::name, Function.identity(), (a, b) -> a, LinkedHashMap::new));
            this.nameMap = nameMap;
            this.subAgentOutputMap = subAgentOutputMap;
            this.lastSentMap = lastSentMap;
            this.lastSentToolsMap = lastSentToolsMap;
            this.lastIsCompleteMap = lastIsCompleteMap;
            this.aiAgentMessage = aiAgentMessage;
            this.documentStore = documentStore;
            this.coordinatorSegmentIdx = coordinatorSegmentIdx;
        }

        void setStreamingContext(Long uuid, MessageData messageData, SseEmitter emitter,
                                 Map<Long, MessageData> msgMap) {
            this.uuid = uuid;
            this.messageData = messageData;
            this.emitter = emitter;
            this.msgMap = msgMap;
        }

        /**
         * 委托任务给子智能体（带实时流式输出）
         *
         * @param subagentName 子智能体名称
         * @param description  详细的任务描述
         * @return 子智能体的执行结果文本
         */
        @Tool(description = """
                Delegate a task to a specialized sub-agent. Use this tool when:
                1. The task requires domain expertise that a sub-agent has
                2. You want a sub-agent to perform research, analysis, or complex reasoning
                3. You need to break down a complex request into sub-tasks for different experts
                
                IMPORTANT — this tool is non-blocking on first call per agent:
                - First call: dispatches the sub-agent and returns immediately so you can delegate
                  to multiple experts in parallel. Use readSubAgentOutput to check results.
                - Subsequent calls for the same agent: waits for and returns the result.
                
                Parameters:
                - subagentName: The exact name of the sub-agent to delegate to
                - description: A clear, detailed description of what the sub-agent should do
                """)
        public String task(
                @ToolParam(description = "The name of the sub-agent to delegate to") String subagentName,
                @ToolParam(description = "Detailed task description for the sub-agent") String description) {

            ReactAgent agent = subAgents.get(subagentName);
            if (agent == null) {
                return String.format("错误：未找到子智能体 '%s'。可用的子智能体有：%s",
                        subagentName, subAgents.keySet());
            }

            String displayName = nameMap.getOrDefault(subagentName, subagentName);

            // 构建子智能体输出容器
            AiAgentContent agentContent = subAgentOutputMap.computeIfAbsent(subagentName, k ->
                    AiAgentContent.builder()
                            .id(subagentName)
                            .name(displayName)
                            .command(description)
                            .isComplete(false)
                            .build());
            // 如果已存在，更新最新的 task 指令
            agentContent.setCommand(description);

            // 注册到文档工具，创建完成信号（供其他子智能体 readSubAgentOutput 等待）
            documentStore.registerAgent(subagentName);

            // ===== 检查是否已有正在执行的 Future =====
            CompletableFuture<String> existingFuture = subAgentFutures.get(subagentName);
            if (existingFuture != null) {
                // 已委托过，等待结果（无超时限制，子智能体可执行任意长时间）
                try {
                    log.debug("子智能体 [{}] 已在执行中，等待结果...", subagentName);
                    return existingFuture.get();
                } catch (Exception e) {
                    log.error("等待子智能体 [{}] 结果异常", subagentName, e);
                    return String.format("子智能体 '%s' 等待异常：%s", subagentName, e.getMessage());
                }
            }

            // ===== 首次委派：异步启动，立即返回 =====
            CompletableFuture<String> future = new CompletableFuture<>();
            subAgentFutures.put(subagentName, future);

            AtomicReference<String> finalText = new AtomicReference<>();

            // 子智能体流式输出收集（参照 AgentStreamProcessor：单块累积模式）
            List<AiContent> aiContents = Collections.synchronizedList(new ArrayList<>());
            agentContent.setAiContent(aiContents);
            // 将子智能体输出容器提前注册到 agentContentList，确保 stop 时已累积的内容不会丢失
            List<AiAgentContent> list = aiAgentMessage.getAgentContentList();
            if (list == null) {
                list = new ArrayList<>();
                aiAgentMessage.setAgentContentList(list);
            }
            if (list.stream().noneMatch(a -> subagentName.equals(a.getId()))) {
                list.add(agentContent);
            }
            messageData.setAiAgentMessage(aiAgentMessage);
            if (msgMap != null) {
                msgMap.put(uuid, messageData);
            }
            AtomicReference<AiContent> currentAiContent = new AtomicReference<>();
            AtomicBoolean isNewContent = new AtomicBoolean(true);

            // 子智能体内部工具追踪
            List<ChatTool> subAgentTools = Collections.synchronizedList(new ArrayList<>());

            try {
                // 发送初始状态（让前端知道子智能体已启动）
                sendDeltaMessage(uuid, subagentName, agentContent, lastSentMap,
                        lastSentToolsMap, lastIsCompleteMap, messageData, emitter);

                List<Message> subMessages = new ArrayList<>();
                subMessages.add(new UserMessage(description));

                // 订阅子智能体的流式输出，实时推送到前端
                messageData.addSubAgentDisposable(
                    agent.streamMessages(subMessages)
                        .doOnNext(msg -> {
                            try {
                                if (msg instanceof ToolResponseMessage trm) {
                                    // 工具响应：标记当前块结束，下一个 AssistantMessage 创建新块
                                    AiContent current = currentAiContent.get();
                                    if (current != null) {
                                        isNewContent.set(true);
                                    }

                                    // 更新子智能体工具响应数据：仅匹配当前 content 块中的工具，通过精确 id 匹配
                                    // 参照 AgentStreamProcessor：不跨块匹配，不通过名称回退，避免同名工具被错误覆写
                                    trm.getResponses().forEach(resp -> {
                                        List<ChatTool> toolsToMatch = current != null
                                                ? current.getTools() : subAgentTools;
                                        if (toolsToMatch != null && !toolsToMatch.isEmpty()) {
                                            for (ChatTool tool : toolsToMatch) {
                                                if (Objects.isNull(tool.getResponseData())
                                                        && Objects.equals(tool.getId(), resp.id())) {
                                                    tool.setResponseData(resp.responseData());
                                                    break;
                                                }
                                            }
                                        }
                                    });

                                    // 重新 setTools 以创建新列表引用，触发 sendDeltaMessage 的 toolsChanged 检测
                                    if (current != null && current.getTools() != null
                                            && !current.getTools().isEmpty()) {
                                        current.setTools(new ArrayList<>(current.getTools()));
                                        // 工具响应到达，表示该轮 ReAct 循环的工具执行阶段结束，
                                        // 标记思考完成，前端据此将"思考中"切换为"思考过程"
                                        current.setThinkingComplete(true);
                                    }

                                    // 实时流式发送增量
                                    sendDeltaMessage(uuid, subagentName, agentContent, lastSentMap,
                                            lastSentToolsMap, lastIsCompleteMap, messageData, emitter);

                                } else if (msg instanceof AssistantMessage am) {
                                    // 获取或创建当前累积块（参照 AgentStreamProcessor 单块累积模式）
                                    AiContent ac;
                                    if (isNewContent.getAndSet(false)) {
                                        ac = new AiContent();
                                        ac.setContent(new StringBuffer());
                                        ac.setThinking(new StringBuffer());
                                        aiContents.add(ac);
                                        currentAiContent.set(ac);
                                    } else {
                                        ac = currentAiContent.get();
                                    }

                                    // 累积推理内容（兼容增量/全量两种模式）
                                    Object reasoning = am.getMetadata().get("reasoningContent");
                                    if (reasoning != null) {
                                        String reasoningStr = reasoning.toString();
                                        StringBuffer existingThinking = ac.getThinking();
                                        if (existingThinking != null) {
                                            String existing = existingThinking.toString();
                                            if (!existing.isEmpty() && reasoningStr.startsWith(existing)) {
                                                // 全量模式：框架返回的是累计文本，直接替换
                                                existingThinking.replace(0, existingThinking.length(), reasoningStr);
                                            } else {
                                                // 增量模式：框架返回的是增量文本，追加
                                                existingThinking.append(reasoningStr);
                                            }
                                        } else {
                                            ac.setThinking(new StringBuffer(reasoningStr));
                                        }
                                    }

                                    // 累积正文内容（兼容增量/全量两种模式）
                                    String txt = am.getText();
                                    if (txt != null && !txt.isEmpty()) {
                                        StringBuffer existingContent = ac.getContent();
                                        if (existingContent != null) {
                                            String existing = existingContent.toString();
                                            if (!existing.isEmpty() && txt.startsWith(existing)) {
                                                // 全量模式：框架返回的是累计文本，直接替换
                                                existingContent.replace(0, existingContent.length(), txt);
                                            } else {
                                                // 增量模式：框架返回的是增量文本，追加
                                                existingContent.append(txt);
                                            }
                                        } else {
                                            ac.setContent(new StringBuffer(txt));
                                        }
                                        // 同步累积 finalText（子智能体最终返回给协调者的结果）
                                        String current = finalText.get();
                                        if (current != null && txt.startsWith(current)) {
                                            finalText.set(txt);
                                        } else {
                                            finalText.set(current != null ? current + txt : txt);
                                        }
                                    }

                                    // 处理工具调用
                                    if (!am.getToolCalls().isEmpty()) {
                                        List<ChatTool> chatTools = new ArrayList<>();
                                        am.getToolCalls().forEach(tc -> {
                                            ChatTool ct = new ChatTool();
                                            ct.setId(tc.id());
                                            ct.setName(tc.name());
                                            ct.setArguments(tc.arguments());
                                            chatTools.add(ct);
                                            subAgentTools.add(ct);
                                        });
                                        ac.setTools(chatTools);
                                    }

                                    // 实时流式发送增量
                                    sendDeltaMessage(uuid, subagentName, agentContent, lastSentMap,
                                            lastSentToolsMap, lastIsCompleteMap, messageData, emitter);
                                }

                            } catch (Exception e) {
                                log.error("子智能体 [{}] 流式发送异常: {}", subagentName, e.getMessage(), e);
                            }
                        })
                        .doOnComplete(() -> {
                            try {
                                // 标记完成
                                agentContent.setIsComplete(true);

                                // 将子智能体分段追加到 agentContentList（保持时间线顺序）
                                List<AiAgentContent> agentContentList = aiAgentMessage.getAgentContentList();
                                if (agentContentList == null) {
                                    agentContentList = new ArrayList<>();
                                    aiAgentMessage.setAgentContentList(agentContentList);
                                }
                                boolean alreadyInList = agentContentList.stream()
                                        .anyMatch(a -> subagentName.equals(a.getId()));
                                if (!alreadyInList) {
                                    agentContentList.add(agentContent);
                                }

                                // 更新全局状态
                                messageData.setAiAgentMessage(aiAgentMessage);
                                if (msgMap != null) {
                                    msgMap.put(uuid, messageData);
                                }

                                // 自增分段索引：下一轮协调者输出将进入新分段
                                coordinatorSegmentIdx[0]++;

                                // 发送完成增量
                                try {
                                    sendDeltaMessage(uuid, subagentName, agentContent, lastSentMap,
                                            lastSentToolsMap, lastIsCompleteMap, messageData, emitter);
                                } catch (Exception e) {
                                    log.error("子智能体 [{}] 完成事件发送异常: {}", subagentName, e.getMessage(), e);
                                }

                            } catch (Exception e) {
                                log.error("子智能体 [{}] doOnComplete 异常: {}", subagentName, e.getMessage(), e);
                            } finally {
                                // 确保无论是否异常，都存储输出并完成 future
                                String completedText = finalText.get();
                                documentStore.storeOutput(subagentName, completedText);
                                future.complete(completedText);
                            }
                        })
                        .doOnError(err -> {
                            log.error("子智能体 [{}] 执行异常: {}", subagentName, err.getMessage(), err);
                            agentContent.setIsComplete(true);
                            future.completeExceptionally(err);
                        })
                        .subscribe());

                // 流已在后台异步执行（doOnComplete/doOnError 会完成 future）
                // 立即返回，协调者可继续委派其他子智能体，实现并行执行
                return "已委托任务给子智能体 '" + subagentName + "'（" + displayName + "），正在执行中。"
                        + "请稍后调用 task 或 readSubAgentOutput 获取结果。";

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return String.format("子智能体 '%s' 被中断", subagentName);
            } catch (Exception e) {
                log.error("子智能体 [{}] 执行异常: {}", subagentName, e.getMessage(), e);
                return String.format("子智能体 '%s' 执行出错：%s", subagentName, e.getMessage());
            }
        }
    }

    // ==================== 工具处理器 ====================

    /**
     * 工具处理器类 - 负责处理工具ID到实际工具实例的转换
     */
    class ToolProcessor {
        private final Map<Long, ModelToolDO> toolMap;
        private final Map<List<Long>, ToolConfig> cache = new HashMap<>();

        ToolProcessor(Map<Long, ModelToolDO> toolMap) {
            this.toolMap = toolMap;
        }

        ToolConfig process(List<Long> toolIds) {
            if (toolIds == null || toolIds.isEmpty()) {
                return ToolConfig.empty();
            }

            if (cache.containsKey(toolIds)) {
                return cache.get(toolIds);
            }

            List<ModelToolDO> modelTools = toolIds.stream()
                    .map(toolMap::get)
                    .filter(Objects::nonNull)
                    .toList();

            List<String> systemToolNames = modelTools.stream()
                    .filter(t -> Objects.equals(t.getType(), ToolType.SYSTEM_TOOL))
                    .map(ModelToolDO::getName)
                    .toList();

            List<ModelToolDO> httpTools = modelTools.stream()
                    .filter(t -> Objects.equals(t.getType(), ToolType.HTTP_DYNAMIC_TOOL))
                    .toList();

            List<ModelToolDO> databaseTools = modelTools.stream()
                    .filter(t -> Objects.equals(t.getType(), ToolType.DATABASE_TOOL))
                    .toList();

            List<ModelToolDO> mcpTools = modelTools.stream()
                    .filter(t -> Objects.equals(t.getType(), ToolType.MCP_TOOL))
                    .toList();

            List<McpSyncClient> mcpClients = toolService.loadingMcpSyncClients(mcpTools);
            SyncMcpToolCallbackProvider mcpCallbackProvider = (mcpClients != null && !mcpClients.isEmpty())
                    ? SyncMcpToolCallbackProvider.builder().mcpClients(mcpClients).build()
                    : SyncMcpToolCallbackProvider.builder().build();

            List<ToolCallback> toolCallbacks = new ArrayList<>();
            httpTools.forEach(httpTool -> {
                for (HttpToolConfig httpConfig : httpTool.getSettings().getHttpConfigs()) {
                    toolCallbacks.add(new HttpToolCallback(httpConfig, WebClient.builder()));
                }
            });
            databaseTools.forEach(databaseTool -> {
                for (DatabaseToolConfig databaseConfig : databaseTool.getSettings().getDatabaseConfigs()) {
                    if (databaseConfig.getStatements() != null) {
                        for (DatabaseStatementConfig statement : databaseConfig.getStatements()) {
                            toolCallbacks.add(new DatabaseToolCallback(databaseConfig, statement));
                        }
                    }
                    if (Boolean.TRUE.equals(databaseConfig.getAllowFreeQuery())) {
                        toolCallbacks.add(new DatabaseToolCallback(databaseConfig));
                    }
                }
            });

            List<Object> toolInstances = toolFactory.getToolInstancesByIds(systemToolNames);
            String promptText = chatToolUtil.generatePrompt(toolInstances);

            ToolConfig config = new ToolConfig(toolInstances, toolCallbacks, mcpCallbackProvider, promptText);
            cache.put(toolIds, config);
            return config;
        }

        @Getter
        static class ToolConfig {
            private final List<Object> toolInstances;
            List<ToolCallback> toolCallbacks;
            private final SyncMcpToolCallbackProvider mcpCallbackProvider;
            private final String promptText;

            private ToolConfig(List<Object> toolInstances,
                               List<ToolCallback> toolCallbacks,
                               SyncMcpToolCallbackProvider mcpCallbackProvider,
                               String promptText) {
                this.toolInstances = toolInstances;
                this.toolCallbacks = toolCallbacks;
                this.mcpCallbackProvider = mcpCallbackProvider;
                this.promptText = promptText;
            }

            static ToolConfig empty() {
                return new ToolConfig(List.of(), List.of(), SyncMcpToolCallbackProvider.builder().build(), "");
            }
        }
    }

    // ==================== 渐进式披露文档工具 ====================

    /**
     * 渐进式披露文档共享存储 —— 子智能体间信息共享机制
     * <p>
     * 每个子智能体在完成任务后，其最终输出会被存入共享的文档存储中。
     * 后续执行的子智能体可以通过调用 readSubAgentOutput(agentId) 读取已完成同行的成果，
     * 从而实现渐进式信息披露，避免重复劳动，提升协作质量。
     * <p>
     * 设计理念：
     * <ul>
     *   <li>只读共享：子智能体只能读取他人输出，不能修改</li>
     *   <li>渐进披露：先完成的子智能体的成果对后执行的子智能体可见</li>
     *   <li>多次调用：同一子智能体可能被协调者多次委托，每次输出均保留在历史列表中</li>
     *   <li>防死锁：通过"等待关系图"检测循环等待，避免子智能体相互等待形成死锁</li>
     *   <li>线程安全：使用 ConcurrentHashMap 保证并发安全</li>
     * </ul>
     */
    static class SubAgentDocumentStore {

        /** 子智能体执行状态 */
        enum AgentExecState {
            /** 尚未启动 */
            NOT_STARTED,
            /** 正在执行中 */
            RUNNING,
            /** 已完成（至少完成过一次调用） */
            COMPLETED
        }

        /** 共享文档存储：agentId -> 历史输出列表（每次 task() 调用追加一条） */
        private final ConcurrentHashMap<String, List<String>> documentStore = new ConcurrentHashMap<>();
        /** agentId -> 完成信号（供 readSubAgentOutput 等待当前正在执行的任务完成） */
        private final ConcurrentHashMap<String, CountDownLatch> completionLatches = new ConcurrentHashMap<>();
        /** agentId -> 当前执行状态 */
        private final ConcurrentHashMap<String, AgentExecState> agentStates = new ConcurrentHashMap<>();
        /** 等待关系图：agentId -> 其当前正在等待的 agentId（用于检测循环等待死锁） */
        private final ConcurrentHashMap<String, String> waitingOn = new ConcurrentHashMap<>();
        /** 保证"检测循环 + 登记等待"的原子性，避免并发登记时漏检导致死锁 */
        private final Object waitLock = new Object();

        /**
         * 注册子智能体（在 task() 启动子智能体时调用，创建完成信号，标记为 RUNNING）。
         * 如果已有旧 latch（上次委托已完成），先唤醒等待者再替换。
         */
        void registerAgent(String agentId) {
            CountDownLatch oldLatch = completionLatches.put(agentId, new CountDownLatch(1));
            if (oldLatch != null) {
                oldLatch.countDown(); // 唤醒可能还在等待旧 latch 的调用者
            }
            agentStates.put(agentId, AgentExecState.RUNNING);
            log.debug("渐进式披露文档工具：子智能体 [{}] 已注册，状态 → RUNNING", agentId);
        }

        /**
         * 存储子智能体的最终输出并发出完成信号（由 SubAgentTaskTool 在子智能体完成时调用）。
         * <p>
         * 每次调用追加到历史列表，状态切换为 COMPLETED（表示至少完成过一次）。
         * 无论输出是否为空，都会发出完成信号（latch 表示"本次执行已完成"）。
         */
        void storeOutput(String agentId, String output) {
            if (output != null && !output.isEmpty()) {
                documentStore.computeIfAbsent(agentId, k -> Collections.synchronizedList(new ArrayList<>()))
                        .add(output);
                log.debug("渐进式披露文档工具：已存储子智能体 [{}] 的第 {} 次输出，共 {} 字符",
                        agentId, documentStore.get(agentId).size(), output.length());
            } else {
                log.debug("渐进式披露文档工具：子智能体 [{}] 已完成但无文本输出", agentId);
            }
            // 标记为 COMPLETED（至少完成过一次；若被再次调用，registerAgent 会重置为 RUNNING）
            agentStates.put(agentId, AgentExecState.COMPLETED);
            // 发出完成信号，唤醒等待中的 readSubAgentOutput 调用者
            CountDownLatch latch = completionLatches.get(agentId);
            if (latch != null) {
                latch.countDown();
            }
        }

        /**
         * 判断 caller 等待 target 是否会形成循环依赖。
         * 即：从 target 出发，沿等待关系图向下追踪，若最终能回到 caller，说明 target
         * 已经（直接或间接）在等待 caller，此时 caller 再等待 target 会成环。
         * <p>
         * 必须在持有 {@link #waitLock} 时调用，保证"检测 + 登记"是原子的。
         */
        private boolean wouldCreateCycle(String caller, String target) {
            String next = target;
            Set<String> visited = new HashSet<>();
            while (next != null) {
                if (next.equals(caller)) {
                    return true;
                }
                if (!visited.add(next)) {
                    // 已存在环（理论上登记阶段已阻止，这里保守兜底）
                    return true;
                }
                next = waitingOn.get(next);
            }
            return false;
        }
    }

    /**
     * 渐进式披露文档工具 —— 子智能体读取同僚成果的入口。
     * <p>
     * 每个子智能体持有独立实例，通过 {@code callerId} 明确标识调用者身份，从而可靠地实现
     * "禁止自读"与"循环等待检测"。此前使用单一共享实例 + volatile 字段识别调用者，在子智能体
     * 并行执行时无法正确区分调用者，且无法检测相互等待形成的循环依赖。
     */
    static class SubAgentDocumentTool {

        private final SubAgentDocumentStore store;
        private final String callerId;
        private final Map<String, String> nameMap;

        SubAgentDocumentTool(SubAgentDocumentStore store, String callerId, Map<String, String> nameMap) {
            this.store = store;
            this.callerId = callerId;
            this.nameMap = nameMap;
        }

        /**
         * 列出所有同僚专家及其当前状态（实时）。
         *
         * @return 同僚专家列表（不含自身），包含 ID、名称、执行状态、已完成输出次数
         */
        @Tool(description = """
                List all peer sub-agents and their current status in real-time.
                Use this tool to discover which peers are available, what they are working on,
                and whether they have completed outputs you can read via `readSubAgentOutput`.
                Note: This list excludes yourself.
                """)
        public String listAvailablePeers() {
            StringBuilder sb = new StringBuilder("当前同僚专家列表（实时状态）：\n\n");
            for (Map.Entry<String, String> entry : nameMap.entrySet()) {
                String id = entry.getKey();
                String name = entry.getValue();
                if (id.equals(this.callerId)) continue;
                SubAgentDocumentStore.AgentExecState state = store.agentStates.get(id);
                String stateDesc = state == null ? "未启动" : state.name();
                int outputCount = 0;
                List<String> stored = store.documentStore.get(id);
                if (stored != null) outputCount = stored.size();
                sb.append(String.format("- **%s**（%s）：状态=%s，已完成输出=%d次",
                        id, name, stateDesc, outputCount));
                if (stored != null && !stored.isEmpty()
                        && state == SubAgentDocumentStore.AgentExecState.COMPLETED) {
                    sb.append(" [可读取]");
                }
                sb.append("\n");
            }
            if (sb.toString().equals("当前同僚专家列表（实时状态）：\n\n")) {
                sb.append("（暂无其他同僚专家）\n");
            }
            return sb.toString();
        }

        /**
         * 读取指定子智能体的已完成输出。
         * <p>
         * 行为规则：
         * <ol>
         *   <li>若目标已有历史输出（列表非空），立即返回所有历史输出（即使目标当前正在执行新任务）</li>
         *   <li>若目标无历史输出且状态为 RUNNING：
         *       自读则立即拒绝；检测到循环等待则立即拒绝；否则阻塞等待目标完成（带超时）</li>
         *   <li>若目标无历史输出且状态为 NOT_STARTED → 返回"尚未启动"</li>
         * </ol>
         *
         * @param agentId 子智能体唯一ID（如 sub_analyst、sub_writer）
         * @return 该子智能体的历史输出文本（全部合并）
         */
        @Tool(description = """
                Read the completed output of another sub-agent. Use this tool when:
                1. You need to reference research or analysis already done by a peer sub-agent
                2. You want to avoid duplicating work that another expert has already completed
                3. You need to build upon findings from a previous sub-agent's investigation
                
                This implements progressive disclosure: sub-agents that finish earlier
                make their results available to those that run later.
                If the target sub-agent is still running, this tool will wait for it to complete.
                If waiting would create a circular dependency (deadlock), this tool refuses
                immediately instead of blocking. Note: You CANNOT read your own output.
                """)
        public String readSubAgentOutput(
                @ToolParam(description = "The unique ID of the sub-agent whose output you want to read (e.g. sub_analyst, sub_writer)") String agentId) {

            String displayName = nameMap.getOrDefault(agentId, agentId);

            // ===== 第一层：优先返回已有历史输出（即使目标正在执行第二次调用，历史输出仍可读） =====
            List<String> outputs = store.documentStore.get(agentId);
            if (outputs != null && !outputs.isEmpty()) {
                return formatOutputs(agentId, displayName, outputs);
            }

            // ===== 第二层：无历史输出，根据状态机判断 =====
            SubAgentDocumentStore.AgentExecState state = store.agentStates.get(agentId);
            if (state == null || state == SubAgentDocumentStore.AgentExecState.NOT_STARTED) {
                return String.format("子智能体 '%s'（%s）尚未启动。可用的子智能体 ID：%s",
                        agentId, displayName, nameMap.keySet());
            }

            if (state == SubAgentDocumentStore.AgentExecState.RUNNING) {
                // 自读检测：每个子智能体持有独立工具实例，callerId 即其自身身份
                if (callerId.equals(agentId)) {
                    return String.format("不能读取自己的输出。你当前正在执行的子智能体 ID 为 '%s'（%s），"
                            + "请参考其他已完成同行的成果。可用的同僚 ID：%s",
                            agentId, displayName, nameMap.keySet().stream()
                                    .filter(k -> !k.equals(agentId)).collect(Collectors.toList()));
                }

                // 循环等待检测 + 登记等待（原子操作，避免并发登记时漏检导致死锁）
                boolean cycle;
                synchronized (store.waitLock) {
                    cycle = store.wouldCreateCycle(callerId, agentId);
                    if (!cycle) {
                        store.waitingOn.put(callerId, agentId);
                    }
                }
                if (cycle) {
                    return String.format("检测到循环依赖：你正在等待 '%s'（%s）的输出，"
                            + "而对方也在（直接或间接）等待你的输出。为避免死锁，本次读取已拒绝。"
                            + "请基于当前已有信息继续推进，稍后可再次尝试。",
                            agentId, displayName);
                }

                CountDownLatch latch = store.completionLatches.get(agentId);
                if (latch == null) {
                    // 理论上 RUNNING 状态必有 latch，此处仅作兜底
                    synchronized (store.waitLock) {
                        store.waitingOn.remove(callerId, agentId);
                    }
                    return String.format("子智能体 '%s'（%s）正在执行中，请稍后重试。", agentId, displayName);
                }

                try {
                    log.debug("渐进式披露：等待子智能体 [{}]（{}）完成...", agentId, displayName);
                    boolean completed = latch.await(120, TimeUnit.SECONDS);
                    if (!completed) {
                        return String.format("等待子智能体 '%s'（%s）超时（120 秒）。"
                                + "请基于当前已有信息继续推进，稍后可再次尝试。", agentId, displayName);
                    }
                    outputs = store.documentStore.get(agentId);
                    if (outputs != null && !outputs.isEmpty()) {
                        return formatOutputs(agentId, displayName, outputs);
                    }
                    return String.format("子智能体 '%s'（%s）已完成，但未产生文本输出（可能仅执行了工具操作）。",
                            agentId, displayName);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return String.format("等待子智能体 '%s'（%s）时被中断。", agentId, displayName);
                } finally {
                    synchronized (store.waitLock) {
                        store.waitingOn.remove(callerId, agentId);
                    }
                }
            }

            // COMPLETED 状态但无输出（理论上不会出现，作为兜底）
            return String.format("子智能体 '%s'（%s）已完成但未产生文本输出。", agentId, displayName);
        }

        /**
         * 格式化单次输出
         */
        private String formatSingleOutput(String agentId, String displayName, int index, int total, String output) {
            String header = total > 1
                    ? String.format("=== 子智能体 '%s'（%s）的输出 [第 %d/%d 次调用] ===", agentId, displayName, index, total)
                    : String.format("=== 子智能体 '%s'（%s）的输出 ===", agentId, displayName);
            return String.format("""
                    %s
                    
                    %s
                    
                    === 输出结束 ===
                    """, header, output);
        }

        /**
         * 格式化历史输出列表（多次调用时合并展示）
         */
        private String formatOutputs(String agentId, String displayName, List<String> outputs) {
            if (outputs.size() == 1) {
                return formatSingleOutput(agentId, displayName, 1, 1, outputs.getFirst());
            }
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("子智能体 '%s'（%s）共有 %d 次历史输出：\n\n", agentId, displayName, outputs.size()));
            for (int i = 0; i < outputs.size(); i++) {
                sb.append(formatSingleOutput(agentId, displayName, i + 1, outputs.size(), outputs.get(i)));
                if (i < outputs.size() - 1) {
                    sb.append("\n\n");
                }
            }
            return sb.toString();
        }
    }
}
