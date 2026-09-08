package cn.iocoder.yudao.module.ai.core.chat.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.ai.common.enums.*;
import cn.iocoder.yudao.module.ai.common.model.entity.InitTask;
import cn.iocoder.yudao.module.ai.common.model.entity.LoadingTaskResult;
import cn.iocoder.yudao.module.ai.common.service.TaskHistoryService;
import cn.iocoder.yudao.module.ai.common.task.SmartTaskScheduler;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiChatDialogueDO;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiChatTopicDO;
import cn.iocoder.yudao.module.ai.core.chat.enums.AgentType;
import cn.iocoder.yudao.module.ai.core.chat.enums.AiApiType;
import cn.iocoder.yudao.module.ai.core.chat.enums.AiModelType;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ExportChatHistoryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.RegenerateMessageDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.SendMessageDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ToolPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.*;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.*;
import cn.iocoder.yudao.module.ai.core.chat.service.*;
import cn.iocoder.yudao.module.ai.core.chat.stream.AiStreamProcessor;
import cn.iocoder.yudao.module.ai.core.chat.stream.StreamContext;
import cn.iocoder.yudao.module.ai.core.chat.utils.*;
import cn.iocoder.yudao.module.ai.core.rag.handle.PromptHandlerContext;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.core.type.TypeReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.ai.common.constans.ErrorCodeConstants.UNSUPPORTED_API_TYPE;

@Service
@Slf4j
@DataPermission(enable = false)
public class AiChatServiceImpl implements AiChatService {

    Map<Long, MessageData> msgMap = new ConcurrentHashMap<>();

    private final AiModelService aiModelService;
    private final AiAgentService aiAgentService;
    private final AiChatTopicService aiChatTopicService;
    private final AiChatDialogueService aiChatDialogueService;
    private final PromptHandlerContext promptHandlerContext;
    private final List<AiStreamProcessor> streamProcessors;
    private final AiToolService aiToolService;
    private final ChatContentUtil chatContentUtil;
    private final TaskHistoryService taskHistoryService;
    private final SmartTaskScheduler smartTaskScheduler;
    private final FileService fileService;
    private final AiChatRegenerateService aiChatRegenerateService;

    public AiChatServiceImpl(AiModelService aiModelService, AiAgentService aiAgentService,
                             AiChatTopicService aiChatTopicService, AiChatDialogueService aiChatDialogueService, PromptHandlerContext promptHandlerContext,
                             List<AiStreamProcessor> streamProcessors,
                             AiToolService aiToolService, ChatContentUtil chatContentUtil,
                             TaskHistoryService taskHistoryService, SmartTaskScheduler smartTaskScheduler, FileService fileService,
                             AiChatRegenerateService aiChatRegenerateService) {
        this.aiModelService = aiModelService;
        this.aiAgentService = aiAgentService;
        this.aiChatTopicService = aiChatTopicService;
        this.aiChatDialogueService = aiChatDialogueService;
        this.promptHandlerContext = promptHandlerContext;
        this.streamProcessors = streamProcessors;
        this.aiToolService = aiToolService;
        this.chatContentUtil = chatContentUtil;
        this.taskHistoryService = taskHistoryService;
        this.smartTaskScheduler = smartTaskScheduler;
        this.fileService = fileService;
        this.aiChatRegenerateService = aiChatRegenerateService;
    }

    @Override
    public SseEmitter conversation(Long uuid, SendMessageDTO messageDto) {
        return doConversation(uuid, messageDto, false);
    }

    @Override
    public SseEmitter conversationWithValidatedAgentPermission(Long uuid, SendMessageDTO messageDto) {
        return doConversation(uuid, messageDto, true);
    }

    /**
     * 发送消息的统一实现。
     *
     * @param agentPermissionValidated 上层服务是否已经完成智能体执行授权校验
     */
    private SseEmitter doConversation(Long uuid, SendMessageDTO messageDto,
                                      boolean agentPermissionValidated) {
        // 检查 MessageData 是否存在
        if (messageDto == null) {
            log.error("MessageData not found for UUID: {}", uuid);
            return chatContentUtil.createErrorEmitter();
        }
        MessageData messageData = MessageData.builder().sse(new SseEmitter(0L))
                .fileIds(messageDto.getFileIds()).wikiIds(messageDto.getWikiIds()).apiType(messageDto.getApiType())
                .question(messageDto.getQuestion()).topicId(messageDto.getTopicId()).tools(new ArrayList<>())
                .lastId(messageDto.getLastId()).selectedTools(messageDto.getSelectedTools())
                .tenantId(TenantContextHolder.getTenantId())
                .userId(String.valueOf(getLoginUserId()))
                .endpointId(messageDto.getEndpointId()).modelType(messageDto.getModelType()).build();
        // 提取必要信息
        SseEmitter emitter = messageData.getSse();
        smartTaskScheduler.submit(() -> {
            String question = messageData.getQuestion();
            Long topicId = messageData.getTopicId();
            if (Objects.isNull(topicId)) {
                String title = chatContentUtil.limitLengthWithEllipsis(question);
                AiChatTopicDO aiChatTopic = AiChatTopicDO.builder().title(title)
                        .deleted(false).build();
                aiChatTopicService.insertTopic(aiChatTopic);
                topicId = aiChatTopic.getId();
                messageData.setTopicId(topicId);
            }
            msgMap.put(uuid, messageData);
            List<Long> fileIds = messageData.getFileIds();
            List<Long> wikiIds = messageData.getWikiIds();
            String _uuid = String.valueOf(uuid);
            try {
                String creator = String.valueOf(getLoginUserId());
                // 构建并插入用户对话记录
                Long lastId = chatContentUtil.saveUserDialogue(messageData);
                messageData.setLastId(lastId);

                // 发送开始事件
                chatContentUtil.sendStartEvent(emitter, _uuid, topicId, lastId);

                // 加载历史消息或文档内容，携带最新的用户问题，由构建Prompt时加载
                AiModelType modelType = messageData.getModelType();
                DialogueHistory dialogueHistory = aiChatDialogueService.loadingDialogueHistory(topicId, 6, modelType);
                List<Message> messages = dialogueHistory.getMessages();
                if (Objects.isNull(fileIds) || fileIds.isEmpty()) {
                    fileIds = dialogueHistory.getFileIds();
                }
                List<String> fileContext = new ArrayList<>();
                List<FileInfo> fileInfos = new ArrayList<>();
                List<FileDO> objects = fileService.getFileByIds(fileIds);
                if (Objects.nonNull(objects) && !objects.isEmpty()) {
                    // 确定需要处理的文件列表
                    List<FileDO> filesToProcess = objects;

                    // VISION模型且存在图片文件时，只处理非图片文件
                    if (Objects.equals(modelType, AiModelType.VISION)) {
                        filesToProcess = objects.stream()
                                .filter(object -> !object.getType().contains("image"))
                                .toList();
                    }

                    // 统一处理逻辑
                    if (!filesToProcess.isEmpty()) {
                        chatContentUtil.sendStatusUpdate(emitter, _uuid, "正在提取用户上传文件信息...", 2, 1);
                        filesToProcess.forEach(object -> {
                            ModelUseInfo filePrompt = promptHandlerContext.handleFile(object);
                            List<String> context = filePrompt.getContext();
                            fileContext.addAll(context);
                            fileInfos.add(FileInfo.builder()
                                    .id(object.getId())
                                    .fileType(object.getType())
                                    .fileContext(context)
                                    .filename(object.getName())
                                    .build());
                        });
                        chatContentUtil.sendStatusUpdate(emitter, _uuid, "已完成信息的提取...", 2, 2);
                    }
                }

                Long endpointId = messageData.getEndpointId();

                // 策略模式：根据 API 类型选择对应的流式处理器
                AiStreamProcessor processor = streamProcessors.stream()
                        .filter(p -> p.supports(messageData.getApiType()))
                        .findFirst()
                        .orElseThrow(() -> exception(UNSUPPORTED_API_TYPE));
                processor.processStream(StreamContext.builder()
                        .uuid(uuid)._uuid(_uuid).creator(creator)
                        .endpointId(endpointId).wikiIds(wikiIds)
                        .messages(messages).messageData(messageData)
                        .emitter(emitter).msgMap(msgMap)
                        .fileInfos(fileInfos).fileContext(fileContext)
                        .question(question)
                        .agentPermissionValidated(agentPermissionValidated)
                        .build());

                // 设置断开连接和超时回调
                chatContentUtil.setupEmitterCallbacks(msgMap, emitter, uuid);

            } catch (Exception e) {
                log.error("[Start] >> ", e);
                List<AiContent> aiContent = new ArrayList<>();
                StringBuffer content = new StringBuffer(
                        e instanceof ServiceException ? e.getMessage() : "服务器繁忙，请稍后重试！"
                );
                aiContent.add(AiContent.builder().content(content).build());
                messageData.setAiContent(aiContent);
                try {
                    emitter.send(SseEmitter.event().name("output")
                            .id(String.valueOf(uuid)).data(SendOutputData.builder()
                                    .topicId(topicId).aiContent(aiContent).build()));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                this.stopConversation(uuid);
            }
        }, TaskPriority.HIGH, "chat");
        return emitter;
    }

    @Override
    public List<SelectEndpointVO> selectEndpointList() {
        List<SelectAgentVO> selectAgents = aiAgentService.selectAgentList();
        List<SelectModelVO> selectModels = aiModelService.selectModelList(true);
        List<SelectEndpointVO> endpoints = new ArrayList<>();
        selectAgents.stream().sorted(Comparator.comparingInt(agent -> {
                    if (agent.getType() == AgentType.REACT_AGENT) return 0;
                    if (agent.getType() == AgentType.MULTI_AGENT) return 1;
                    return 2;
                }))
                .forEach(agent -> {
                    AiApiType aiApiType = switch (agent.getType()) {
                        case MULTI_AGENT -> AiApiType.MULTI_AGENT;
                        case REACT_AGENT -> AiApiType.AGENT;
                    };
                    endpoints.add(SelectEndpointVO.builder()
                            .id(agent.getId()).description(agent.getDescription()).apiType(aiApiType)
                            .toolIds(agent.getTools()).name(agent.getName())
                            .modelType(agent.getModelType()).build());
                });
        selectModels.forEach(model -> endpoints.add(SelectEndpointVO.builder()
                .id(model.getId()).description(model.getDescription()).apiType(model.getType())
                .name(Objects.isNull(model.getRename()) ? model.getName() : model.getRename())
                .modelType(model.getModelType()).build()));
        return endpoints;
    }

    @Override
    public PageResult<SelectToolVO> selectToolList(ToolPageQueryDTO dto) {
        return aiToolService.selectPageQuery(dto);
    }

    @Override
    public Boolean clearHistory() {
        List<Long> archiveTopicId = aiChatTopicService.getArchiveTopicId();
        if (aiChatTopicService.clearAllTopicByIgnoreIds(archiveTopicId)) {
            aiChatDialogueService.clearAllDialogueByIgnoreTopicIds(archiveTopicId);
        }
        return true;
    }

    @Override
    public ExportChatRecordVO exportHistory(ExportChatHistoryDTO dto) {
        List<TopicRange> topicRanges = aiChatTopicService.selectTopicsByCreateTimeRange(dto.getStartTime(), dto.getEndTime());

        // 创建导出目录
        String exportDir = createExportDirectory();
        List<String> generatedFiles = new ArrayList<>();
        Long loginUserId = getLoginUserId();
        Long taskId = taskHistoryService.initTask(InitTask.builder()
                .taskName("导出对话历史记录")
                .taskType(TaskHistoryType.CONVERSATION_EXPORT)
                .objectId(loginUserId)
                .scheduleType(TaskScheduleType.IMMEDIATELY)
                .build());
        for (TopicRange topicRange : topicRanges) {
            List<ExportChatDialogue> chatDialogues = aiChatDialogueService.getChatDialogueByTopicId(topicRange.getId());
            String markdownContent = MarkdownUtil.generateMarkdownForTopic(topicRange, chatDialogues);
            LocalDateTime createTime = topicRange.getCreateTime();
            long timestamp = createTime.atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
            String fileName = sanitizeFileName(topicRange.getTitle()) + "-" + timestamp + ".md";
            String filePath = exportDir + File.separator + fileName;
            // 写入文件
            try {
                Files.writeString(Paths.get(filePath), markdownContent);
                generatedFiles.add(filePath);
                log.info("导出文件成功: {}", filePath);
            } catch (IOException e) {
                log.error("写入文件失败: {}", filePath, e);
            }
        }

        File zipFile = zipExportedFiles(generatedFiles, exportDir);
        if (Objects.nonNull(zipFile) && zipFile.exists()) {
            try {
                FileDO file = fileService.createFile(Files.readAllBytes(zipFile.toPath()));
                LocalDateTime now = LocalDateTime.now();
                ExportChatDialogueResult build = ExportChatDialogueResult.builder()
                        .fileId(file.getId()).exportTime(now).build();
                taskHistoryService.initTaskResult(
                        LoadingTaskResult.builder()
                                .id(taskId)
                                .status(TaskHistoryStatus.SUCCESS)
                                .result(JSONObject.toJSONString(build))
                                .build());
                return ExportChatRecordVO.builder()
                        .url(file.getUrl())
                        .exportTime(now).build(); // 返回文件访问URL
            } catch (Exception e) {
                log.error("导出文件失败", e);
                taskHistoryService.initTaskResult(
                        LoadingTaskResult.builder()
                                .id(taskId)
                                .status(TaskHistoryStatus.FAILURE)
                                .build());
            } finally {
                Boolean deleted = zipFile.delete() && new File(exportDir).delete();
                log.debug("导出记录清除临时文件内容状态：{}", deleted);
            }
        }

        return null;
    }

    @Override
    public ExportChatRecordVO exportChatRecord() {
        Long taskId = taskHistoryService.existTask(getLoginUserId(), TaskHistoryType.CONVERSATION_EXPORT);
        if (Objects.isNull(taskId)) {
            return null;
        }
        ExportChatDialogueResult result = taskHistoryService.loadingResult(taskId, new TypeReference<>() {});
        String previewUrl = fileService.presignGetUrl(result.getFileId(), 600);
        return ExportChatRecordVO.builder().url(previewUrl).exportTime(result.getExportTime()).build();
    }

    /**
     * 创建导出目录
     */
    private String createExportDirectory() {
        String exportDir = System.getProperty("user.dir") + File.separator + "export_history_" + System.currentTimeMillis();
        File dir = new File(exportDir);
        if (!dir.exists()) {
            boolean mkdirs = dir.mkdirs();
            log.debug("导出记录构建临时目录状态：{}", mkdirs);
        }
        return exportDir;
    }

    /**
     * 清理文件名中的非法字符
     */
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    /**
     * 将多个文件打包成ZIP
     */
    private File zipExportedFiles(List<String> files, String exportDir) {
        String zipPath = exportDir + ".zip";
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath))) {
            for (String filePath : files) {
                File file = new File(filePath);
                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) >= 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }

            // 删除原始文件夹
            File dir = new File(exportDir);
            deleteDirectory(dir);

            return new File(zipPath);
        } catch (IOException e) {
            log.error("打包ZIP失败", e);
            return null;
        }
    }

    /**
     * 递归删除目录
     */
    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        boolean delete = file.delete();
                        log.debug("清除 {} 临时文件内容状态：{}", file.getName(), delete);
                    }
                }
            }
            boolean delete = directory.delete();
            log.debug("清除 {} 临时文件夹状态：{}", directory.getName(), delete);
        }
    }

    @Override
    public SseEmitter regenerate(Long uuid, Long lastId, RegenerateMessageDTO dto) {
        // 1. 校验并读取用户消息（问题 + 文件），作为重生成的锚点
        ChatDialogue userDialogue = aiChatDialogueService.getDialogueById(lastId);
        if (Objects.isNull(userDialogue) || !Objects.equals(userDialogue.getSender(), "user")
                || Objects.isNull(userDialogue.getContent())) {
            log.error("[Regenerate] 用户消息不存在或内容为空，lastId: {}", lastId);
            return chatContentUtil.createErrorEmitter();
        }
        // 2. 定位被重生成的 assistant 消息（其 lastId 指向该用户消息）
        AiChatDialogueDO assistantDialogue = aiChatDialogueService.getAssistantDialogueByLastId(lastId);
        if (Objects.isNull(assistantDialogue)
                || !Objects.equals(assistantDialogue.getCreator(), String.valueOf(getLoginUserId()))) {
            log.error("[Regenerate] assistant 消息不存在或无权操作，lastId: {}", lastId);
            return chatContentUtil.createErrorEmitter();
        }
        UserContent userContent = JSONObject.parseObject(userDialogue.getContent(), UserContent.class);
        MessageData messageData = MessageData.builder()
                .sse(new SseEmitter(0L))
                .fileIds(userDialogue.getFileIds())
                .wikiIds(dto.getWikiIds())
                .apiType(dto.getApiType())
                .modelType(dto.getModelType())
                .question(userContent.getQuestion())
                .topicId(assistantDialogue.getTopicId())
                .lastId(lastId)
                .selectedTools(dto.getSelectedTools())
                .tenantId(TenantContextHolder.getTenantId())
                .userId(String.valueOf(getLoginUserId()))
                .endpointId(dto.getEndpointId())
                .regenerateDialogueId(assistantDialogue.getId())
                .build();
        SseEmitter emitter = messageData.getSse();
        smartTaskScheduler.submit(() -> {
            String _uuid = String.valueOf(uuid);
            msgMap.put(uuid, messageData);
            try {
                String creator = String.valueOf(getLoginUserId());
                // 加载历史消息：窗口以被重生成的 assistant 消息为截止点（不包含其后的后续对话），
                // 并排除该旧回答本身，避免模型看到自己的旧答案与"未来"内容
                AiModelType modelType = messageData.getModelType();
                DialogueHistory dialogueHistory = aiChatDialogueService.loadingDialogueHistory(
                        messageData.getTopicId(), 6, modelType,
                        assistantDialogue.getId(), assistantDialogue.getId());
                List<Message> messages = dialogueHistory.getMessages();
                List<Long> fileIds = messageData.getFileIds();
                if (Objects.isNull(fileIds) || fileIds.isEmpty()) {
                    fileIds = dialogueHistory.getFileIds();
                }
                List<String> fileContext = new ArrayList<>();
                List<FileInfo> fileInfos = new ArrayList<>();
                List<FileDO> objects = fileService.getFileByIds(fileIds);
                if (Objects.nonNull(objects) && !objects.isEmpty()) {
                    List<FileDO> filesToProcess = objects;
                    if (Objects.equals(modelType, AiModelType.VISION)) {
                        filesToProcess = objects.stream()
                                .filter(object -> !object.getType().contains("image"))
                                .toList();
                    }
                    if (!filesToProcess.isEmpty()) {
                        chatContentUtil.sendStatusUpdate(emitter, _uuid, "正在提取用户上传文件信息...", 2, 1);
                        filesToProcess.forEach(object -> {
                            ModelUseInfo filePrompt = promptHandlerContext.handleFile(object);
                            List<String> context = filePrompt.getContext();
                            fileContext.addAll(context);
                            fileInfos.add(FileInfo.builder()
                                    .id(object.getId())
                                    .fileType(object.getType())
                                    .fileContext(context)
                                    .filename(object.getName())
                                    .build());
                        });
                        chatContentUtil.sendStatusUpdate(emitter, _uuid, "已完成信息的提取...", 2, 2);
                    }
                }
                // 策略模式：根据 API 类型选择对应的流式处理器
                AiStreamProcessor processor = streamProcessors.stream()
                        .filter(p -> p.supports(messageData.getApiType()))
                        .findFirst()
                        .orElseThrow(() -> exception(UNSUPPORTED_API_TYPE));
                processor.processStream(StreamContext.builder()
                        .uuid(uuid)._uuid(_uuid).creator(creator)
                        .endpointId(messageData.getEndpointId()).wikiIds(messageData.getWikiIds())
                        .messages(messages).messageData(messageData)
                        .emitter(emitter).msgMap(msgMap)
                        .fileInfos(fileInfos).fileContext(fileContext)
                        .question(messageData.getQuestion())
                        .build());
                // 设置断开连接和超时回调
                chatContentUtil.setupEmitterCallbacks(msgMap, emitter, uuid);
            } catch (Exception e) {
                log.error("[Regenerate] >> ", e);
                List<AiContent> aiContent = new ArrayList<>();
                StringBuffer content = new StringBuffer(
                        e instanceof ServiceException ? e.getMessage() : "服务器繁忙，请稍后重试！"
                );
                aiContent.add(AiContent.builder().content(content).build());
                messageData.setAiContent(aiContent);
                try {
                    emitter.send(SseEmitter.event().name("output")
                            .id(_uuid).data(SendOutputData.builder()
                                    .topicId(messageData.getTopicId()).aiContent(aiContent).build()));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                this.stopConversation(uuid);
            }
        }, TaskPriority.HIGH, "chat");
        return emitter;
    }

    @Override
    public List<RegenerateVersionVO> regenerateList(Long dialogueId) {
        return aiChatRegenerateService.listVersions(dialogueId);
    }

    @Override
    public List<RegenerateVersionVO> regenerateListByTopic(Long topicId) {
        return aiChatRegenerateService.listVersionsByTopic(topicId);
    }

    @Override
    public List<RegenerateVersionVO> switchRegenerateVersion(Long dialogueId, Long versionId) {
        return aiChatRegenerateService.switchVersion(dialogueId, versionId);
    }

    @Override
    public Boolean stopConversation(Long uuid) {
        MessageData messageData = msgMap.get(uuid);
        if (Objects.isNull(messageData)) { return false; }
        // 先取消底层 AI 模型流式调用，再保存已累积内容并关闭 SSE 连接
        messageData.cancelAllStreams();
        chatContentUtil.saveAiDialogueAndComplete(msgMap, uuid);
        return true;
    }

}
