package cn.iocoder.yudao.module.ai.core.chat.utils;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.ai.common.enums.TaskPriority;
import cn.iocoder.yudao.module.ai.common.task.SmartTaskScheduler;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiChatDialogueDO;
import cn.iocoder.yudao.module.ai.core.chat.enums.AiApiType;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiAgentDO;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.*;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiChatTokenDO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.DocMetadataVO;
import cn.iocoder.yudao.module.ai.core.chat.service.*;
import cn.iocoder.yudao.module.ai.core.rag.utils.DocMetadataUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.document.Document;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class ChatContentUtil {

    private final AiChatDialogueService aiChatDialogueService;
    private final AiChatTokenService aiChatTokenService;
    private final AiModelService aiModelService;
    private final AiAgentService aiAgentService;
    private final DocMetadataUtil docMetadataUtil;
    private final SmartTaskScheduler smartTaskScheduler;
    private final AiChatRegenerateService aiChatRegenerateService;
    private static final JTokkitTokenCountEstimator estimator = new JTokkitTokenCountEstimator();

    public ChatContentUtil(AiChatDialogueService aiChatDialogueService,
                           AiChatTokenService aiChatTokenService,
                           AiModelService aiModelService,
                           AiAgentService aiAgentService, DocMetadataUtil docMetadataUtil, SmartTaskScheduler smartTaskScheduler,
                           AiChatRegenerateService aiChatRegenerateService) {
        this.aiChatDialogueService = aiChatDialogueService;
        this.aiChatTokenService = aiChatTokenService;
        this.aiModelService = aiModelService;
        this.aiAgentService = aiAgentService;
        this.docMetadataUtil = docMetadataUtil;
        this.smartTaskScheduler = smartTaskScheduler;
        this.aiChatRegenerateService = aiChatRegenerateService;
    }

    // 提取状态更新逻辑
    public void sendStatusUpdate(SseEmitter emitter, String uuid, String task, int total, int progress) throws IOException {
        log.info("{}/{} >> {}", progress, total, task);
        emitter.send(SseEmitter.event().name("status").id(uuid)
                .data(SendStatusData.builder().task(task).total(total).progress(progress).build()));
    }

    // 辅助方法：构建用户对话记录
    public AiChatDialogueDO buildUserDialogue(MessageData messageData) {
        List<Long> fileIds = messageData.getFileIds();
        AiChatDialogueDO dialogue = AiChatDialogueDO.builder()
                .topicId(messageData.getTopicId())
                .lastId(messageData.getLastId())
                .sender("user")
                .isStar(false)
                .deleted(false)
                .build();
        dialogue.setCreator(messageData.getUserId());
        dialogue.setUpdater(messageData.getUserId());
        if (Objects.nonNull(fileIds) && !fileIds.isEmpty()) {
            dialogue.setFileIds(fileIds);
        }
        return dialogue;
    }

    // 辅助方法：保存用户对话记录（包含 content 子表）
    public Long saveUserDialogue(MessageData messageData) {
        AiChatDialogueDO dialogue = buildUserDialogue(messageData);
        String content = JSONObject.toJSONString(UserContent.builder()
                .question(messageData.getQuestion()).build());
        aiChatDialogueService.insertDialogue(dialogue, content);
        return dialogue.getId();
    }


    // 辅助方法：设置 Emitter 回调
    public void setupEmitterCallbacks(Map<Long, MessageData> msgMap, SseEmitter emitter, Long uuid) {
        emitter.onCompletion(() -> log.info("SSE 连接已关闭，UUID: {}", uuid));
        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时，UUID: {}", uuid);
            this.saveAiDialogueAndComplete(msgMap, uuid);
        });
    }

    // 辅助方法：创建错误的 SseEmitter
    public SseEmitter createErrorEmitter() {
        SseEmitter emitter = new SseEmitter(0L);
        emitter.completeWithError(new IllegalArgumentException("Invalid UUID"));
        return emitter;
    }

    // 辅助方法：发送开始事件
    public void sendStartEvent(SseEmitter emitter, String uuid, Long topicId, Long lastId) throws IOException {
        emitter.send(SseEmitter.event().name("start")
                .id(uuid).data(SendStartData.builder().topicId(topicId)
                        .createTime(LocalDateTime.now()).lastId(lastId).build()));
    }

    /**
     * 保存 AI 对话记录并完成 SSE 连接
     */
    public void saveAiDialogueAndComplete(Map<Long, MessageData> msgMap, Long uuid) {
        String _uuid = String.valueOf(uuid);
        MessageData messageData = msgMap.get(uuid);
        if (Objects.isNull(messageData)) {
            return;
        }
        // 恢复租户上下文（CompletableFuture 回调线程无上下文）
        if (messageData.getTenantId() != null) {
            TenantContextHolder.setTenantId(messageData.getTenantId());
        }
        try {
            List<Document> documents = messageData.getDocuments();
            List<ChatTool> tools = messageData.getTools();
            Long topicId = messageData.getTopicId();
            Long lastId = messageData.getLastId();
            Long endpointId = messageData.getEndpointId();
            AiApiType apiType = messageData.getApiType();
            List<AiContent> aiContent = messageData.getAiContent();
            setDefaultToolResponseIfBlank(aiContent);

            AiAgentMessage aiAgentMessage = messageData.getAiAgentMessage();
            if (Objects.nonNull(aiAgentMessage)) {
                List<AiAgentContent> agentContentList = aiAgentMessage.getAgentContentList();
                if (Objects.nonNull(agentContentList)) {
                    agentContentList.forEach(aiAgentContent -> {
                        setDefaultToolResponseIfBlank(aiAgentContent.getAiContent());
                        if (!Boolean.TRUE.equals(aiAgentContent.getIsComplete())) {
                            aiAgentContent.setIsComplete(true);
                        }
                    });
                }
            }
            SseEmitter emitter = messageData.getSse();
            try {
                // 构建 AI 对话记录
                String metadata = Objects.isNull(documents) ? null : JSONArray.toJSONString(documents);
                String toolsResult = Objects.isNull(tools) ? null : JSONArray.toJSONString(tools);
                String content = Objects.isNull(aiContent) ? Objects.isNull(aiAgentMessage) ? "[]" : JSONObject.toJSONString(aiAgentMessage) : JSONArray.toJSONString(aiContent);
                Long regenerateDialogueId = messageData.getRegenerateDialogueId();
                Long savedDialogueId;
                String savedMetadata = metadata;
                if (Objects.nonNull(regenerateDialogueId)) {
                    // 重新生成：新回答登记为最新版本（旧回答自动成为历史版本），再原地更新主行（行 id 与消息链不变）
                    aiChatRegenerateService.saveRegenerateVersion(regenerateDialogueId, content, metadata, toolsResult);
                    aiChatDialogueService.updateDialogueContent(regenerateDialogueId, content, metadata, toolsResult);
                    savedDialogueId = regenerateDialogueId;
                    log.info("AI 对话记录已重新生成并原地更新，ID: {}", regenerateDialogueId);
                } else {
                    AiChatDialogueDO aiAiChatDialogue = AiChatDialogueDO.builder()
                            .topicId(topicId).lastId(lastId).sender("assistant")
                            .isStar(false).metadata(metadata).tools(toolsResult)
                            .deleted(false).build();
                    aiAiChatDialogue.setCreator(messageData.getUserId());
                    aiAiChatDialogue.setUpdater(messageData.getUserId());
                    aiChatDialogueService.insertDialogue(aiAiChatDialogue, content);
                    savedDialogueId = aiAiChatDialogue.getId();
                    savedMetadata = aiAiChatDialogue.getMetadata();
                    log.info("AI 对话记录已保存，ID: {}", savedDialogueId);
                }

                smartTaskScheduler.submit(() -> {
                    String name;
                    if (Objects.equals(apiType, AiApiType.AGENT) || Objects.equals(apiType, AiApiType.MULTI_AGENT)){
                        AiAgentDO aiAgent = aiAgentService.selectAccessibleAgentById(endpointId);
                        name = Objects.isNull(aiAgent) ? "" : aiAgent.getName();
                        if (Objects.nonNull(aiAgent)) {
                            aiAgentService.increaseUsageCount(endpointId);
                        }
                    } else {
                        ChatApi chatApi = aiModelService.selectChatModelApiById(endpointId);
                        name = Objects.isNull(chatApi) ? "" : chatApi.getName();
                    }
                    // 优先记录模型返回的真实 Token 用量；无真实用量时回退到本地估算
                    Long totalTokens = messageData.getTotalTokens();
                    if (Objects.isNull(totalTokens)) {
                        DialogueHistory dialogueHistory = aiChatDialogueService.loadingDialogueHistory(topicId, 7);
                        List<Message> messages = dialogueHistory.getMessages();
                        totalTokens = (long) estimator.estimate(messages.toString());
                    }
                    aiChatTokenService.insert(AiChatTokenDO.builder().type(apiType)
                            .model(name)
                            .totalTokens(totalTokens)
                            .promptTokens(messageData.getPromptTokens())
                            .completionTokens(messageData.getCompletionTokens())
                            .build());
                    log.info("本次对话消耗了 {} Token(prompt={}, completion={})", totalTokens,
                            messageData.getPromptTokens(), messageData.getCompletionTokens());
                }, TaskPriority.LOW, "stats");


                // 尝试发送结束事件
                List<DocMetadataVO> list = docMetadataUtil.getDocMetadata(savedMetadata);
                emitter.send(SseEmitter.event().name("end")
                        .id(_uuid).data(SendEndData.builder().topicId(topicId)
                                .lastId(savedDialogueId)
                                .docMetadata(list).build()));
            } catch (Exception e) {
                log.error("[End] >> ", e);
            } finally {
                // 确保完成 SSE 连接
                emitter.complete();
                msgMap.remove(uuid);
            }
        } finally {
            TenantContextHolder.clear();
        }
    }

    public String limitLengthWithEllipsis(String question) {
        final int maxLength = 36;
        if (question == null) {
            return "";
        }
        if (question.length() > maxLength) {
            return question.substring(0, maxLength) + "...";
        } else {
            return question;
        }
    }

    private void setDefaultToolResponseIfBlank(List<AiContent> aiContentList) {
        if (Objects.isNull(aiContentList)) {
            return;
        }
        aiContentList.forEach(content -> {
            List<ChatTool> tools = content.getTools();
            if (Objects.nonNull(tools)) {
                tools.forEach(tool -> {
                    if (StringUtils.isBlank(tool.getResponseData())) {
                        tool.setResponseData("已终止内容输出");
                    }
                });
            }
        });
    }

}
