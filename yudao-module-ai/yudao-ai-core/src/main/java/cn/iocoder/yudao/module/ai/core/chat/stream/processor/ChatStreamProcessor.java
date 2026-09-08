package cn.iocoder.yudao.module.ai.core.chat.stream.processor;

import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import cn.iocoder.yudao.module.system.service.acl.engine.AclDecisionEngine;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.ModelSettingDO;
import cn.iocoder.yudao.module.ai.core.chat.enums.AiApiType;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.*;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatModelService;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatSettingService;
import cn.iocoder.yudao.module.ai.core.chat.stream.AiStreamProcessor;
import cn.iocoder.yudao.module.ai.core.chat.stream.StreamContext;
import cn.iocoder.yudao.module.ai.core.chat.utils.ChatContentUtil;
import cn.iocoder.yudao.module.ai.core.rag.model.entity.RagMessage;
import cn.iocoder.yudao.module.ai.core.rag.service.MilvusStoreService;
import cn.iocoder.yudao.module.ai.core.rag.utils.PromptTemplateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.common.constans.ErrorCodeConstants.NO_EXECUTE_PERMISSION;

/**
 * 通用 Chat 流式处理策略 —— 支持标准 Chat（非 Agent/多智能体）的 API 类型
 *
 */
@Slf4j
@Component
@DataPermission(enable = false)
public class ChatStreamProcessor implements AiStreamProcessor {

    private final MilvusStoreService milvusStoreService;
    private final AiChatModelService aiChatModelService;
    private final AiChatSettingService settingService;
    private final ChatContentUtil chatContentUtil;
    private final AclDecisionEngine aclDecisionEngine;

    public ChatStreamProcessor(MilvusStoreService milvusStoreService,
                               AiChatModelService aiChatModelService,
                               AiChatSettingService settingService,
                               ChatContentUtil chatContentUtil,
                               AclDecisionEngine aclDecisionEngine) {
        this.milvusStoreService = milvusStoreService;
        this.aiChatModelService = aiChatModelService;
        this.settingService = settingService;
        this.chatContentUtil = chatContentUtil;
        this.aclDecisionEngine = aclDecisionEngine;
    }

    @Override
    public boolean supports(AiApiType apiType) {
        return !Objects.equals(apiType, AiApiType.AGENT)
                && !Objects.equals(apiType, AiApiType.MULTI_AGENT);
    }

    @Override
    public void processStream(StreamContext ctx) {
        try {
            List<Document> documents = null;
            if (Objects.nonNull(ctx.getWikiIds()) && !ctx.getWikiIds().isEmpty()) {
                RagMessage ragMessage = loadingWikiDoc(ctx.getWikiIds(), ctx.getFileContext(),
                        ctx.getQuestion(), ctx.getEmitter(), ctx.get_uuid());
                ctx.getMessages().addFirst(ragMessage.getSysMessage());
                ctx.getMessages().add(ragMessage.getUserMessage());
                documents = ragMessage.getDocuments();
            } else {
                PromptTemplateUtil.defineMessage(ctx.getFileContext(), ctx.getMessages());
            }

            ChatModel chatModel = aiChatModelService.getChatModel(ctx.getEndpointId());
            Flux<ChatResponse> stream = chatModel.stream(new Prompt(ctx.getMessages()));
            processStream(stream, documents, ctx.getMsgMap(), ctx.getEmitter(), ctx.getUuid(), ctx.getEndpointId());
        } catch (IOException e) {
            log.error("[ChatStream] 处理流失败：", e);
        }
    }

    private void processStream(Flux<ChatResponse> stream, List<Document> documents,
                               Map<Long, MessageData> msgMap, SseEmitter emitter, Long uuid, Long endpointId) {
        if (!aclDecisionEngine.canAccess(ResourceType.MODEL, endpointId, Permission.EXECUTE)) {
            throw exception(NO_EXECUTE_PERMISSION);
        }

        MessageData messageData = msgMap.get(uuid);
        List<AiContent> aiContent = new ArrayList<>();
        aiContent.add(AiContent.builder().content(new StringBuffer())
                .thinking(new StringBuffer()).build());
        messageData.setAiContent(aiContent);
        messageData.setDocuments(documents);
        Long topicId = messageData.getTopicId();
        messageData.setStreamDisposable(stream.subscribe(data -> {
                try {
                    Generation result = data.getResult();
                    AssistantMessage assistantMessage = Objects.isNull(result) ? new AssistantMessage("") : result.getOutput();
                    String text;
                    String reasoningChunk = null;
                    if (assistantMessage instanceof DeepSeekAssistantMessage deepSeekAssistantMessage) {
                        reasoningChunk = deepSeekAssistantMessage.getReasoningContent();
                        text = deepSeekAssistantMessage.getText();
                    } else {
                        Object reasoningContent = assistantMessage.getMetadata().get("reasoningContent");
                        if (Objects.nonNull(reasoningContent)) {
                            reasoningChunk = reasoningContent.toString();
                        }
                        text = assistantMessage.getText();
                    }
                    // 真实用量在 getUsage() 里，而不是 metadata map 的 "usage" 键（该键不存在）
                    Usage usage = data.getMetadata().getUsage();
                    if (usage != null && usage.getTotalTokens() != null) {
                        messageData.setPromptTokens(usage.getPromptTokens() != null ? usage.getPromptTokens().longValue() : null);
                        messageData.setCompletionTokens(usage.getCompletionTokens() != null ? usage.getCompletionTokens().longValue() : null);
                        messageData.setTotalTokens(usage.getTotalTokens().longValue());
                        log.info("本次请求消耗 Token -> prompt: {}, completion: {}, total: {}",
                                usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
                    }
                    if (Objects.nonNull(reasoningChunk)) {
                        aiContent.getFirst().getThinking().append(reasoningChunk);
                    }
                    String textChunk = null;
                    if (Objects.nonNull(text)) {
                        aiContent.getFirst().getContent().append(text);
                        textChunk = text;
                    }
                    emitter.send(SseEmitter.event().name("output")
                           .id(String.valueOf(uuid)).data(SendOutputData.builder()
                                    .topicId(topicId).aiContent(List.of(AiContent.builder()
                                            .thinking(reasoningChunk != null ? new StringBuffer(reasoningChunk) : null)
                                            .content(textChunk != null ? new StringBuffer(textChunk) : null)
                                            .build())).build()));
                } catch (IOException e) {
                    log.error("AI Stream 消息发送出错：", e);
                    chatContentUtil.saveAiDialogueAndComplete(msgMap, uuid);
                }
            },
            error -> {
                log.error("AI Subscribe 消息发送出错：", error);
                chatContentUtil.saveAiDialogueAndComplete(msgMap, uuid);
            },
            () -> {
                chatContentUtil.saveAiDialogueAndComplete(msgMap, uuid);
            }
        ));
    }

    private RagMessage loadingWikiDoc(List<Long> wikiIds, List<String> fileContext, String question,
                                      SseEmitter emitter, String _uuid) throws IOException {
        boolean isLong = question.length() > 130;
        int totalSteps = isLong ? 2 : 1;
        int currentStep = 1;

        String simplifiedQuestion = question;
        if (isLong) {
            chatContentUtil.sendStatusUpdate(emitter, _uuid, "正在对用户问题进行简化...", totalSteps, currentStep);
            Message sysMessage = new SystemPromptTemplate("""
            你是一名可以理解用户的问题的助手，你的作用是直接输出用户需要干什么。
            要求：你并不需要处理用户的问题，只是理解并直接输出一句话（只要指令），不能超过60字。
            """).createMessage();
            Message userMessage = new PromptTemplate(question).createMessage();

            ModelSettingDO modelSetting = settingService.getUserModelSetting();
            ChatResponse response = aiChatModelService.getChatModel(modelSetting.getLanguageModelId())
                    .call(new Prompt(List.of(sysMessage, userMessage)));
            simplifiedQuestion = PromptTemplateUtil.removeThink(Objects.requireNonNull(response.getResult()).getOutput().getText());
            log.info("简化的内容为：{}", simplifiedQuestion);
            currentStep++;
        }

        chatContentUtil.sendStatusUpdate(emitter, _uuid, "正在召回知识库的数据...", totalSteps, currentStep);

        Set<Long> restrictedResourceIds = aclDecisionEngine.restrictedIds(ResourceType.DOCUMENT, Permission.EXECUTE);
        List<Document> documents = milvusStoreService.loadDocumentByWikiReader(
                wikiIds, restrictedResourceIds.stream().toList(), simplifiedQuestion, 10);

        return PromptTemplateUtil.createDocumentMessage(question, fileContext, documents, 0.50);
    }
}
