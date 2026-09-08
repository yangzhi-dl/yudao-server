package cn.iocoder.yudao.module.ai.core.workflow.engine.node;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.AiContent;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ModelChatOptions;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatModelService;
import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowGraphNode;
import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowRunContext;
import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowVariableResolver;
import cn.iocoder.yudao.module.ai.core.workflow.engine.handler.WorkflowNodeHandlerAdapter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 大模型节点：调用 yudao 模型管理中的模型，支持流式输出
 *
 * @author yudao
 */
@Slf4j
@Component
public class LlmNodeHandler implements WorkflowNodeHandler, WorkflowNodeHandlerAdapter {

    private static final String DEFAULT_OUTPUT = "answer";

    private final AiChatModelService chatModelService;

    public LlmNodeHandler(AiChatModelService chatModelService) {
        this.chatModelService = chatModelService;
    }

    @Override
    public String[] types() {
        return new String[]{"llmNode", "llm"};
    }

    @Override
    public Map<String, Object> execute(WorkflowGraphNode node, WorkflowRunContext ctx, Map<String, Object> memory) {
        JSONObject data = node.getData();
        Long modelId = data.getLong("llmId");
        if (modelId == null) {
            throw new IllegalArgumentException("LLM 节点 [%s] 未配置模型".formatted(
                    node.getName() == null ? node.getId() : node.getName()));
        }
        String promptTemplate = data.getString("userPrompt");
        if (!StringUtils.hasText(promptTemplate)) {
            throw new IllegalArgumentException("LLM 节点 [%s] 未配置提示词".formatted(
                    node.getName() == null ? node.getId() : node.getName()));
        }
        String systemPromptTemplate = data.getString("systemPrompt");

        String userPrompt = WorkflowVariableResolver.render(promptTemplate, memory);
        String systemPrompt = WorkflowVariableResolver.render(systemPromptTemplate, memory);

        // 节点提示词注入钩子：执行器已把下游知识库/判断信息生成的「模板」写入 injectedPromptTemplate，
        // 将用户的系统提示词置于模板中间（前缀 + 用户系统提示词 + 后缀），用户留空时仅保留模板前后缀。
        String injectedTemplate = data.getString("injectedPromptTemplate");
        if (StringUtils.hasText(injectedTemplate)) {
            systemPrompt = String.format(injectedTemplate, systemPrompt == null ? "" : systemPrompt);
        }

        ModelChatOptions.ModelChatOptionsBuilder optionsBuilder = ModelChatOptions.builder();
        if (data.getDouble("temperature") != null) {
            optionsBuilder.temperature(data.getDouble("temperature"));
        }
        if (data.getDouble("topP") != null) {
            optionsBuilder.topP(data.getDouble("topP"));
        }
        if (data.getInteger("topK") != null) {
            optionsBuilder.topK(data.getInteger("topK"));
        }

        ChatModel chatModel = TenantUtils.executeIgnore(() ->
                chatModelService.getChatModel(modelId, optionsBuilder.build()));
        // 结构化输出判定 + 字段清单：outputFormat=json 时，把 outputDefs 的每个字段名作为 JSON 键，逐个写入内存
        boolean structured = "json".equalsIgnoreCase(data.getString("outputFormat"));
        // 保留完整的字段定义（name + description），供构建 JSON Schema 时约束模型输出
        List<JSONObject> fieldDefs = new ArrayList<>();
        List<String> fieldNames = new ArrayList<>();
        if (structured) {
            JSONArray outputDefs = data.getJSONArray("outputDefs");
            if (outputDefs != null) {
                for (int i = 0; i < outputDefs.size(); i++) {
                    JSONObject o = outputDefs.getJSONObject(i);
                    String name = o == null ? null : o.getString("name");
                    if (name != null && !name.isBlank()) {
                        fieldDefs.add(o);
                        fieldNames.add(name);
                    }
                }
            }
            if (fieldNames.isEmpty()) {
                JSONObject fallback = new JSONObject();
                fallback.put("name", "answer");
                fieldDefs.add(fallback);
                fieldNames.add("answer");
            }
        }

        List<Message> messages = new ArrayList<>();
        if (structured) {
            // 结构化输出：参考 AiModelToolServiceImpl.generateTaxonomy/generateNewJSON 的 JSON Schema
            // 提示方式，按 outputDefs（字段名 + 字段说明）构建 JSON Schema 并强制模型严格遵循，
            // 要求每个字段都填充合理内容，避免模型输出空值或与字段定义不符的 JSON。
            String jsonSchema = buildJsonSchema(fieldDefs);
            String instruction = """
                    你必须只返回一个合法的 JSON 对象，不要有 Markdown 代码块，不要附带任何说明文字，也不要包含任何解释。
                    数据结构必须与下面的 JSON Schema 完全一致（字段名、类型、嵌套层级）：
                    ```json
                    %s
                    ```
                    根据用户的问题和上下文，为每个字段填充合理的内容，确保所有字段都有值，不要遗漏任何字段。
                    """.formatted(jsonSchema);
            messages.add(new SystemMessage(StringUtils.hasText(systemPrompt)
                    ? systemPrompt + "\n" + instruction
                    : instruction));
        } else if (StringUtils.hasText(systemPrompt)) {
            messages.addFirst(new SystemMessage(systemPrompt));
        }
        messages.add(new UserMessage(userPrompt));

        StringBuilder answer = new StringBuilder();
        StringBuffer fullThinking = new StringBuffer();
        StringBuffer fullContent = new StringBuffer();
        AtomicReference<String> lastSentThinking = new AtomicReference<>("");
        AtomicReference<String> lastSentContent = new AtomicReference<>("");
        // 以"可取消订阅"替代 blockLast()：blockLast 会把执行线程阻塞到模型回复结束，
        // 停止时无法中断在途流式请求；改为订阅并登记到上下文，停止时 dispose 立即中断。
        AtomicReference<Throwable> streamError = new AtomicReference<>();
        CountDownLatch streamDone = new CountDownLatch(1);
        reactor.core.Disposable subscription = chatModel.stream(new Prompt(messages))
                .doOnNext(response -> {
                    // 推理内容：兼容 DeepSeek 专用消息与通用 metadata 中的 reasoning/reasoning_content 键
                    String reasoning = extractReasoning(response);
                    if (reasoning != null && !reasoning.isEmpty()) {
                        fullThinking.append(reasoning);
                        emitOutputDelta(ctx, node, fullThinking, fullContent, lastSentThinking, lastSentContent, false);
                    }
                    String delta = extractText(response);
                    if (delta != null && !delta.isEmpty()) {
                        fullContent.append(delta);
                        answer.append(delta);
                        emitOutputDelta(ctx, node, fullThinking, fullContent, lastSentThinking, lastSentContent, false);
                    }
                })
                .doFinally(signal -> streamDone.countDown())
                .subscribe(null, streamError::set, streamDone::countDown);
        ctx.trackStream(subscription);
        try {
            streamDone.await();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        ctx.untrackStream(subscription);

        // 流式调用真实异常：向上抛出，交由工作流判定为失败（区分于停止）
        Throwable streamEx = streamError.get();
        if (!ctx.getStopped().get() && streamEx != null) {
            throw new RuntimeException("[LLM节点] 流式调用失败: "
                    + (streamEx.getMessage() == null ? "未知错误" : streamEx.getMessage()), streamEx);
        }
        // 被停止：不再补发思考完成标记、不再解析结构化 JSON，直接返回当前已累积内容
        if (ctx.getStopped().get()) {
            return Map.of(outputNameOf(node), answer.toString());
        }

        // 流结束后：若存在思考内容，补发一次"思考完成"标记，避免纯思考流一直显示"思考中..."
        if (fullThinking.length() > 0) {
            emitOutputDelta(ctx, node, fullThinking, fullContent, lastSentThinking, lastSentContent, true);
        }

        if (structured) {
            // 解析 JSON，把每个字段作为独立输出写入内存（节点ID.字段名）
            JSONObject parsed = parseJsonObject(answer.toString());
            Map<String, Object> outputs = new LinkedHashMap<>();
            for (String field : fieldNames) {
                Object value = parsed == null ? null : parsed.get(field);
                outputs.put(field, value == null ? "" : value);
            }
            return outputs;
        }
        return Map.of(outputNameOf(node), answer.toString());
    }

    /**
     * 根据大模型节点的 outputDefs（字段名 + description）生成 JSON 字段说明文本，
     * 供下游知识库/判断节点的 {@code buildPredecessorSystemPrompt} 在注入提示词中描述输出字段。
     */
    public static String buildOutputFieldsDescription(JSONObject llmData) {
        JSONArray outputDefs = llmData == null ? null : llmData.getJSONArray("outputDefs");
        if (outputDefs == null || outputDefs.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < outputDefs.size(); i++) {
            JSONObject o = outputDefs.getJSONObject(i);
            String name = o == null ? null : o.getString("name");
            if (name == null || name.isBlank()) {
                continue;
            }
            String desc = o == null ? null : o.getString("description");
            if (sb.length() > 0) {
                sb.append("；");
            }
            sb.append("字段").append(name);
            if (StringUtils.hasText(desc)) {
                sb.append("：").append(desc);
            }
        }
        return sb.toString();
    }

    /**
     * 参考 {@code AiModelToolServiceImpl.generateTaxonomy/generateNewJSON} 中
     * {@code BeanOutputConverter} 生成的 JSON Schema 结构，根据节点 outputDefs
     * （字段名 + 字段说明）动态构建 JSON Schema，用于结构化输出提示词约束模型，
     * 返回字段齐备、命名与说明完全一致的 JSON 对象。
     */
    private static String buildJsonSchema(List<JSONObject> fieldDefs) {
        JSONObject schema = new JSONObject();
        schema.put("type", "object");
        JSONObject properties = new JSONObject();
        JSONArray required = new JSONArray();
        for (JSONObject def : fieldDefs) {
            String name = def == null ? null : def.getString("name");
            if (name == null || name.isBlank()) {
                continue;
            }
            JSONObject prop = new JSONObject();
            prop.put("type", "string");
            String desc = def == null ? null : def.getString("description");
            if (StringUtils.hasText(desc)) {
                prop.put("description", desc);
            }
            properties.put(name, prop);
            required.add(name);
        }
        schema.put("properties", properties);
        schema.put("required", required);
        return schema.toJSONString();
    }

    /**
     * 容错解析模型返回的 JSON 对象：参考 BeanOutputConverter 的默认文本清洗，
     * 自动剥离 thinking 标签（Qwen/DeepSeek 等，避免标签内 JSON 干扰取块）、Markdown 代码块与 JSON 外的前后缀文本。
     */
    private static JSONObject parseJsonObject(String text) {
        if (text == null) {
            return null;
        }
        String s = removeThinkingTags(text.trim());
        if (s.startsWith("```")) {
            int nl = s.indexOf('\n');
            if (nl >= 0) {
                s = s.substring(nl + 1);
            }
            int end = s.lastIndexOf("```");
            if (end >= 0) {
                s = s.substring(0, end).trim();
            }
        }
        int start = s.indexOf('{');
        int last = s.lastIndexOf('}');
        if (start >= 0 && last > start) {
            s = s.substring(start, last + 1);
        }
        try {
            return JSON.parseObject(s);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 剥离模型返回文本中的 thinking 标签：Qwen 风格的 {@code  thinking... response}、
     * XML 风格的 {@code <thinking>...</thinking>} 与 Markdown 风格的 {@code ```thinking ... ```}。
     */
    private static String removeThinkingTags(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String s = text.replaceAll("(?s) thinking.*? response\\s*", "");
        s = s.replaceAll("(?s)<thinking>.*?</thinking>\\s*", "");
        return s.replaceAll("(?s)```thinking.*?```\\s*", "");
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

    /**
     * 按前端约定的 {@code output} 事件（aiContent 增量结构，与 Agent 节点一致）推送一轮增量。
     * thinking/content 为纯增量，thinkingComplete 用于标记思考是否结束。
     */
    private static void emitOutputDelta(WorkflowRunContext ctx, WorkflowGraphNode node,
                                        StringBuffer thinking, StringBuffer content,
                                        AtomicReference<String> lastThinking,
                                        AtomicReference<String> lastContent,
                                        boolean thinkingComplete) {
        String fullT = thinking.toString();
        String fullC = content.toString();
        String tPrev = lastThinking.get();
        String cPrev = lastContent.get();
        String thinkingChunk = (tPrev != null && fullT.startsWith(tPrev))
                ? fullT.substring(tPrev.length())
                : fullT;
        String contentChunk = (cPrev != null && fullC.startsWith(cPrev))
                ? fullC.substring(cPrev.length())
                : fullC;
        lastThinking.set(fullT);
        lastContent.set(fullC);
        if (thinkingChunk.isEmpty() && contentChunk.isEmpty() && !thinkingComplete) {
            return;
        }
        AiContent delta = AiContent.builder()
                .index(0)
                .thinking(thinkingChunk.isEmpty() ? null : new StringBuffer(thinkingChunk))
                .content(contentChunk.isEmpty() ? null : new StringBuffer(contentChunk))
                .thinkingComplete(thinkingComplete ? Boolean.TRUE : null)
                .build();
        ctx.sendEvent("output", Map.of(
                "nodeId", node.getId(),
                "aiContent", List.of(delta)));
    }

    private static String extractText(ChatResponse response) {
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            return null;
        }
        String text = response.getResult().getOutput().getText();
        return text == null ? "" : text;
    }

    /**
     * 提取模型返回的推理内容增量。
     * 兼容两种来源：DeepSeek 专用消息的 {@link DeepSeekAssistantMessage#getReasoningContent()}，
     * 以及通用 OpenAI 兼容响应的 metadata 键（OpenAI 协议同时可能用 {@code reasoning} 或
     * {@code reasoning_content}）。不同 provider 的元数据键名不一，统一兜底读取。
     */
    private static String extractReasoning(ChatResponse response) {
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            return null;
        }
        Message output = response.getResult().getOutput();
        if (output instanceof DeepSeekAssistantMessage deepSeekAssistantMessage) {
            return deepSeekAssistantMessage.getReasoningContent();
        }
        Map<String, Object> metadata = output.getMetadata();
        if (metadata == null) {
            return "";
        }
        Object reasoning = metadata.get("reasoningContent");
        return reasoning == null ? "" : reasoning.toString();
    }
}