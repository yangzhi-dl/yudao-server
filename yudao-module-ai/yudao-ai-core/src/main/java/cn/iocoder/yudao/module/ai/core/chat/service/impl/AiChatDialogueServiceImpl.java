package cn.iocoder.yudao.module.ai.core.chat.service.impl;

import cn.iocoder.yudao.module.ai.core.chat.model.entity.AiAgentContent;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.AiAgentMessage;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiChatDialogueContentDO;
import cn.iocoder.yudao.module.ai.core.chat.dal.mysql.AiChatDialogueContentMapper;
import cn.iocoder.yudao.module.ai.core.chat.dal.mysql.AiChatDialogueMapper;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiChatDialogueDO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ChatDialoguePageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ChatDialogueStarPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.*;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.ChatDialogueVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.ChatStarDialogueVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.DocMetadataVO;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatDialogueService;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatRegenerateService;
import cn.iocoder.yudao.module.ai.core.chat.utils.ContentDispatcher;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.core.rag.utils.DocMetadataUtil;
import cn.iocoder.yudao.module.ai.core.chat.enums.AiModelType;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.FileInfo;
import cn.iocoder.yudao.module.ai.common.model.entity.DeletedStatus;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.content.Media;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeTypeUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Service
@Slf4j
public class AiChatDialogueServiceImpl implements AiChatDialogueService {

    private final DocMetadataUtil docMetadataUtil;
    private final AiChatDialogueMapper aiChatDialogueMapper;
    private final AiChatDialogueContentMapper aiChatDialogueContentMapper;
    private final FileService fileService;
    private final AiChatRegenerateService aiChatRegenerateService;

    public AiChatDialogueServiceImpl(DocMetadataUtil docMetadataUtil, AiChatDialogueMapper aiChatDialogueMapper,
                                     AiChatDialogueContentMapper aiChatDialogueContentMapper, FileService fileService,
                                     AiChatRegenerateService aiChatRegenerateService) {
        this.docMetadataUtil = docMetadataUtil;
        this.aiChatDialogueMapper = aiChatDialogueMapper;
        this.aiChatDialogueContentMapper = aiChatDialogueContentMapper;
        this.fileService = fileService;
        this.aiChatRegenerateService = aiChatRegenerateService;
    }

    @Override
    public PageResult<ChatDialogueVO> chatDialoguePageQuery(ChatDialoguePageQueryDTO pageQueryDto) {
        int page = pageQueryDto.getPage();
        int pageSize = pageQueryDto.getPageSize();
        AiChatDialogueDO aiChatDialogueDO = AiChatDialogueDO.builder()
                .topicId(pageQueryDto.getTopicId()).deleted(false).build();
        String loginUserId = String.valueOf(getLoginUserId());
        aiChatDialogueDO.setCreator(loginUserId);
        BeanUtils.copyProperties(pageQueryDto, aiChatDialogueDO);
        Page<ChatDialogue> chatTopicVos = aiChatDialogueMapper.pageQuery(new Page<>(page, pageSize), aiChatDialogueDO);
        Long total = chatTopicVos.getTotal();
        List<ChatDialogue> result = chatTopicVos.getRecords();
        fillContent(result);
        result = this.rebuildDialogueChain(result);
        List<ChatDialogueVO> records = new ArrayList<>();
        result.forEach(r -> {
            ChatDialogueVO chatDialogueVo = new ChatDialogueVO();
            BeanUtils.copyProperties(r, chatDialogueVo);
            List<Long> ids = r.getFileIds();
            if (Objects.equals(r.getSender(), "user")) {
                chatDialogueVo.setUserContent(JSONObject.parseObject(r.getContent(), UserContent.class));
            } else {
                ContextType contextType = new ContextType();
                new ContentDispatcher().dispatchContent(r.getContent(), contextType);
                chatDialogueVo.setAiContent(contextType.getAiContent());
                chatDialogueVo.setAiAgentMessage(contextType.getAiAgentMessage());
            }
            if (Objects.nonNull(ids) && !ids.isEmpty()) {
                List<FileDO> objects = fileService.getFileByIds(ids);
                List<FileInfo> fileInfos = new ArrayList<>();
                objects.forEach(object -> {
                    String previewUrl = fileService.presignGetUrl(object.getPath(), 600);
                    fileInfos.add(FileInfo.builder()
                            .id(object.getId()).fileSize(object.getSize())
                            .url(previewUrl).filename(object.getName()).build());
                });
                chatDialogueVo.setFileInfos(fileInfos);
            }
            List<DocMetadataVO> docMetadata = docMetadataUtil.getDocMetadata(r.getMetadata());
            chatDialogueVo.setDocMetadata(docMetadata);
            chatDialogueVo.setTools(JSONArray.parseArray(r.getTools(), ChatTool.class));
            records.add(chatDialogueVo);
        });
        return new PageResult<>(records, total);
    }

    /**
     * 按 lastId 重建对话链（处理时间错乱/并发插入场景）
     * @return 按对话逻辑升序排列的列表（第一条为链头，最后一条为最新回复）
     */
    private List<ChatDialogue> rebuildDialogueChain(List<ChatDialogue> rawList) {
        if (rawList == null || rawList.isEmpty()) {
            return new ArrayList<>();
        }

        // 按创建时间升序排序（作为基线顺序）
        rawList.sort(Comparator.comparing(ChatDialogue::getCreateTime));

        Map<Long, ChatDialogue> idMap = rawList.stream()
                .collect(Collectors.toMap(ChatDialogue::getId, Function.identity(), (a, b) -> a));

        // 找出所有可能的链头（lastId为空或不在当前结果集中）
        List<ChatDialogue> heads = rawList.stream()
                .filter(msg -> msg.getLastId() == null || !idMap.containsKey(msg.getLastId()))
                .sorted(Comparator.comparing(ChatDialogue::getCreateTime))
                .toList();

        if (heads.isEmpty()) {
            // 极端情况：所有消息都有lastId且都在idMap中 -> 形成环，按时间升序返回
            return rawList;
        }

        // 构建 next 映射（lastId -> 当前消息）
        Map<Long, ChatDialogue> nextMap = new HashMap<>();
        for (ChatDialogue msg : rawList) {
            if (msg.getLastId() != null && idMap.containsKey(msg.getLastId())) {
                nextMap.put(msg.getLastId(), msg);
            }
        }

        // 从最早的链头开始，按 next 顺序遍历
        List<ChatDialogue> result = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        ChatDialogue current = heads.getFirst();  // 取时间最早的链头作为起始
        while (current != null && !visited.contains(current.getId())) {
            result.add(current);
            visited.add(current.getId());
            current = nextMap.get(current.getId());
        }

        // 处理剩余的未访问消息（可能是其他独立链或分支）
        List<ChatDialogue> remaining = rawList.stream()
                .filter(msg -> !visited.contains(msg.getId()))
                .collect(Collectors.toList());
        if (!remaining.isEmpty()) {
            // 对剩余消息递归重建（独立链）或按时间追加
            result.addAll(rebuildDialogueChain(remaining));  // 递归处理其他链
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertDialogue(AiChatDialogueDO aiChatDialogueDO, String content) {
        aiChatDialogueMapper.insert(aiChatDialogueDO);
        if (Objects.nonNull(content)) {
            aiChatDialogueContentMapper.batchInsert(Collections.singletonList(
                    AiChatDialogueContentDO.builder()
                            .dialogueId(aiChatDialogueDO.getId())
                            .content(content)
                            .build()));
        }
    }

    @Override
    public ChatDialogue getDialogueById(Long id) {
        AiChatDialogueDO dialogueDO = aiChatDialogueMapper.selectById(id);
        if (Objects.isNull(dialogueDO) || Boolean.TRUE.equals(dialogueDO.getDeleted())) {
            return null;
        }
        ChatDialogue chatDialogue = new ChatDialogue();
        BeanUtils.copyProperties(dialogueDO, chatDialogue);
        List<ChatDialogue> dialogues = Collections.singletonList(chatDialogue);
        fillContent(dialogues);
        return dialogues.getFirst();
    }

    @Override
    public boolean isDialogueInTopic(Long dialogueId, Long topicId) {
        Long count = aiChatDialogueMapper.selectCount(new LambdaQueryWrapperX<AiChatDialogueDO>()
                .eq(AiChatDialogueDO::getId, dialogueId)
                .eq(AiChatDialogueDO::getTopicId, topicId)
                .eq(AiChatDialogueDO::getCreator, String.valueOf(getLoginUserId()))
                .eq(AiChatDialogueDO::getDeleted, false));
        return count > 0;
    }

    @Override
    public AiChatDialogueDO getAssistantDialogueByLastId(Long lastId) {
        return aiChatDialogueMapper.selectOne(new LambdaQueryWrapperX<AiChatDialogueDO>()
                .eq(AiChatDialogueDO::getLastId, lastId)
                .eq(AiChatDialogueDO::getSender, "assistant")
                .eq(AiChatDialogueDO::getDeleted, false));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDialogueContent(Long dialogueId, String content, String metadata, String tools) {
        AiChatDialogueDO update = AiChatDialogueDO.builder()
                .id(dialogueId)
                .metadata(metadata)
                .tools(tools)
                .build();
        aiChatDialogueMapper.updateById(update);
        AiChatDialogueContentDO contentDO = aiChatDialogueContentMapper.selectByDialogueId(dialogueId);
        if (Objects.nonNull(contentDO)) {
            contentDO.setContent(content);
            aiChatDialogueContentMapper.updateById(contentDO);
        } else {
            aiChatDialogueContentMapper.insert(AiChatDialogueContentDO.builder()
                    .dialogueId(dialogueId)
                    .content(content)
                    .build());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean switchStar(Long id, Boolean status) {
        AiChatDialogueDO aiChatDialogueDO = AiChatDialogueDO.builder().id(id).isStar(status).build();
        return aiChatDialogueMapper.updateById(aiChatDialogueDO) > 0;
    }

    @Override
    public DialogueHistory loadingDialogueHistory(Long topicId, Integer windowContent, AiModelType modelType, Long excludeDialogueId) {
        return loadingDialogueHistory(topicId, windowContent, modelType, excludeDialogueId, null);
    }

    @Override
    public DialogueHistory loadingDialogueHistory(Long topicId, Integer windowContent, AiModelType modelType,
                                                  Long excludeDialogueId, Long beforeDialogueId) {
        // beforeDialogueId 非空时，窗口以该消息为截止点（不包含其后的后续对话），
        // 用于重新生成中间消息时避免模型"看到未来"
        List<ChatDialogue> dialogueHistory = Objects.isNull(beforeDialogueId)
                ? aiChatDialogueMapper.getHistoryByTopicId(topicId, windowContent * 2)
                : aiChatDialogueMapper.getHistoryByTopicIdBefore(topicId, windowContent * 2, beforeDialogueId);
        fillContent(dialogueHistory);
        Collections.reverse(dialogueHistory);
        List<Message> messages = new ArrayList<>(dialogueHistory.size());
        List<Long> fileIds = new ArrayList<>();
        for (ChatDialogue chatDialogue : dialogueHistory) {
            // 重新生成时排除被重生成的旧回答，避免模型看到自己的旧答案
            if (Objects.nonNull(excludeDialogueId) && Objects.equals(chatDialogue.getId(), excludeDialogueId)) {
                continue;
            }
            MessageType senderType = MessageType.valueOf(chatDialogue.getSender().toUpperCase());
            String content = chatDialogue.getContent();
            if (MessageType.USER.equals(senderType)) {
                List<Media> mediaList = Objects.equals(modelType, AiModelType.VISION) ?
                        getMediaFromChatDialogue(chatDialogue) : new ArrayList<>();
                UserContent userContent = JSONObject.parseObject(content, UserContent.class);
                messages.add(UserMessage.builder().text(userContent.getQuestion()).media(mediaList).build());
            } else if (MessageType.ASSISTANT.equals(senderType)) {
                ContextType contextType = new ContextType();
                new ContentDispatcher().dispatchContent(content, contextType);
                List<AiContent> aiContents = contextType.getAiContent();
                AiAgentMessage aiAgentMessage = contextType.getAiAgentMessage();
                if (Objects.nonNull(aiContents) && !aiContents.isEmpty()) {
                    for (AiContent aiContent : aiContents) {
                        List<ChatTool> tools = aiContent.getTools();
                        List<AssistantMessage.ToolCall> toolCalls = new ArrayList<>();
                        List<ToolResponseMessage.ToolResponse> toolResponses = new ArrayList<>();

                        if (Objects.nonNull(tools) && !tools.isEmpty()) {
                            for (ChatTool tool : tools) {
                                toolCalls.add(new AssistantMessage.ToolCall(
                                        tool.getId(), tool.getType(), tool.getName(), tool.getArguments()));
                                toolResponses.add(new ToolResponseMessage.ToolResponse(
                                        tool.getId(), tool.getName(), tool.getResponseData()));
                            }
                        }

                        String text = aiContent.getContent() != null
                                ? aiContent.getContent().toString() : "";
                        messages.add(AssistantMessage.builder()
                                .content(text)
                                .toolCalls(toolCalls)
                                .build());

                        if (!toolResponses.isEmpty()) {
                            messages.add(ToolResponseMessage.builder().responses(toolResponses).build());
                        }
                    }
                } else if (Objects.nonNull(aiAgentMessage) && Objects.nonNull(aiAgentMessage.getAgentContentList())
                        && !aiAgentMessage.getAgentContentList().isEmpty()) {
                    List<AiAgentContent> agentContentList = aiAgentMessage.getAgentContentList();
                    AiAgentContent aiAgentContent = agentContentList.getLast();
                    List<AiContent> _aiContents = aiAgentContent.getAiContent();
                    if (Objects.nonNull(_aiContents) && !_aiContents.isEmpty()) {
                        for (AiContent aiContent : _aiContents) {
                            List<ChatTool> tools = aiContent.getTools();
                            List<AssistantMessage.ToolCall> toolCalls = new ArrayList<>();
                            List<ToolResponseMessage.ToolResponse> toolResponses = new ArrayList<>();

                            if (Objects.nonNull(tools) && !tools.isEmpty()) {
                                for (ChatTool tool : tools) {
                                    toolCalls.add(new AssistantMessage.ToolCall(
                                            tool.getId(), tool.getType(), tool.getName(), tool.getArguments()));
                                    toolResponses.add(new ToolResponseMessage.ToolResponse(
                                            tool.getId(), tool.getName(), tool.getResponseData()));
                                }
                            }

                            String text = aiContent.getContent() != null
                                    ? aiContent.getContent().toString() : "";
                            messages.add(AssistantMessage.builder()
                                    .content(text)
                                    .toolCalls(toolCalls)
                                    .build());

                            if (!toolResponses.isEmpty()) {
                                messages.add(ToolResponseMessage.builder().responses(toolResponses).build());
                            }
                        }
                    }
                } else {
                    messages.add(new AssistantMessage(""));
                }
            }
            List<Long> fileIdsByDialogue = chatDialogue.getFileIds();
            if (fileIdsByDialogue != null && !fileIdsByDialogue.isEmpty()) {
                fileIds = fileIdsByDialogue;
            }
        }

        return DialogueHistory.builder().messages(messages).fileIds(fileIds).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delDialogue(Long lastId) {
        // 级联软删重新生成版本表（dialogue_id 指向被删的 assistant 行，last_id 指向用户消息）
        List<AiChatDialogueDO> assistantDialogues = aiChatDialogueMapper.selectList(
                new LambdaQueryWrapperX<AiChatDialogueDO>()
                        .eq(AiChatDialogueDO::getLastId, lastId)
                        .eq(AiChatDialogueDO::getSender, "assistant")
                        .eq(AiChatDialogueDO::getDeleted, false));
        List<Long> assistantIds = assistantDialogues.stream()
                .map(AiChatDialogueDO::getId).toList();
        if (!assistantIds.isEmpty()) {
            aiChatRegenerateService.deleteByDialogueIds(assistantIds);
        }
        aiChatRegenerateService.deleteByLastId(lastId);
        AiChatDialogueDO aiChatDialogueDO = AiChatDialogueDO.builder().lastId(lastId).build();
        return aiChatDialogueMapper.del(aiChatDialogueDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateDeletedByTopicIds(List<Long> topicIds) {
        if (!topicIds.isEmpty()) {
            DeletedStatus deletedStatus = DeletedStatus.builder()
                    .deleted(true).ids(topicIds).build();
            aiChatRegenerateService.deleteByTopicIds(topicIds);
            return aiChatDialogueMapper.updateDeletedByTopicIds(deletedStatus);
        }
        return false;
    }

    @Override
    public Boolean exchangeFeedback(Long id, Integer status) {
        AiChatDialogueDO aiChatDialogueDO = AiChatDialogueDO.builder().id(id).feedbackStatus(status).build();
        return aiChatDialogueMapper.updateById(aiChatDialogueDO) > 0;
    }

    @Override
    public List<HourlyStats> selectHourlyStats(LocalDateTime startTime, LocalDateTime endTime) {
        return aiChatDialogueMapper.selectHourlyStats(startTime, endTime);
    }

    @Override
    public List<HourlyStats> selectHourlyStatsYesterday(LocalDateTime startTime, LocalDateTime endTime) {
        return aiChatDialogueMapper.selectHourlyStatsYesterday(startTime, endTime);
    }

    @Override
    public PageResult<ChatStarDialogueVO> chatDialogueStarPageQuery(ChatDialogueStarPageQueryDTO dto) {
        AiChatDialogueDO aiChatDialogueDO = AiChatDialogueDO.builder()
                .isStar(true).build();
        String loginUserId = String.valueOf(getLoginUserId());
        aiChatDialogueDO.setCreator(loginUserId);
        Page<ChatDialogue> chatTopicVos = aiChatDialogueMapper.pageQuery(new Page<>(dto.getPage(), dto.getPageSize()), aiChatDialogueDO);
        Long total = chatTopicVos.getTotal();
        List<ChatDialogue> result = chatTopicVos.getRecords();
        fillContent(result);
        result = this.rebuildDialogueChain(result);
        List<ChatStarDialogueVO> records = new ArrayList<>();
        result.forEach(r -> {
            ChatStarDialogueVO chatDialogueVo = new ChatStarDialogueVO();
            BeanUtils.copyProperties(r, chatDialogueVo);
            List<Long> ids = r.getFileIds();
            ContextType contextType = new ContextType();
            new ContentDispatcher().dispatchContent(r.getContent(), contextType);
            chatDialogueVo.setAiContent(contextType.getAiContent());
            chatDialogueVo.setAiAgentMessage(contextType.getAiAgentMessage());
            if (Objects.nonNull(ids) && !ids.isEmpty()) {
                List<FileDO> objects = fileService.getFileByIds(ids);
                List<FileInfo> fileInfos = new ArrayList<>();
                objects.forEach(object -> {
                    String previewUrl = fileService.presignGetUrl(object.getPath(), 600);
                    fileInfos.add(FileInfo.builder()
                            .id(object.getId()).fileSize(object.getSize())
                            .url(previewUrl).filename(object.getName()).build());
                });
                chatDialogueVo.setFileInfos(fileInfos);
            }
            List<DocMetadataVO> docMetadata = docMetadataUtil.getDocMetadata(r.getMetadata());
            chatDialogueVo.setDocMetadata(docMetadata);
            chatDialogueVo.setTools(JSONArray.parseArray(r.getTools(), ChatTool.class));
            records.add(chatDialogueVo);
        });
        return new PageResult<>(records, total);
    }

    @Override
    public Boolean clearAllDialogueByIgnoreTopicIds(List<Long> ignoreTopicIds) {
        String loginUserId = String.valueOf(getLoginUserId());
        aiChatRegenerateService.deleteByCreator(loginUserId, ignoreTopicIds);
        return aiChatDialogueMapper.clearAllDialogueByIgnoreTopicIds(loginUserId, ignoreTopicIds);
    }

    @Override
    public List<ExportChatDialogue> getChatDialogueByTopicId(Long topicId) {
        String loginUserId = String.valueOf(getLoginUserId());
        List<ChatDialogue> chatDialogue = aiChatDialogueMapper.getChatDialogueByTopicId(topicId, loginUserId);
        fillContent(chatDialogue);
        chatDialogue = this.rebuildDialogueChain(chatDialogue);
        List<ExportChatDialogue> records = new ArrayList<>();
        chatDialogue.forEach(r -> {
            ExportChatDialogue exportChatDialogue = new ExportChatDialogue();
            BeanUtils.copyProperties(r, exportChatDialogue);
            if (Objects.equals(r.getSender(), "user")) {
                exportChatDialogue.setUserContent(JSONObject.parseObject(r.getContent(), UserContent.class));
            } else {
                ContextType contextType = new ContextType();
                new ContentDispatcher().dispatchContent(r.getContent(), contextType);
                exportChatDialogue.setAiContent(contextType.getAiContent());
                exportChatDialogue.setAiAgentMessage(contextType.getAiAgentMessage());
            }
            records.add(exportChatDialogue);
        });
        return records;
    }

    /**
     * 根据 ChatDialogue 获取 Media 列表
     */
    private List<Media> getMediaFromChatDialogue(ChatDialogue chatDialogue) {
        List<Long> fileIds = chatDialogue.getFileIds();
        if (fileIds == null || fileIds.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            List<FileDO> files = fileService.getFileByIds(fileIds);
            List<Media> mediaList = new ArrayList<>(files.size());

            for (FileDO item : files) {
                if (item.getType().contains("image")) {
                    try {
                        byte[] fileContent = fileService.getFileContent(item.getConfigId(), item.getPath());
                        mediaList.add(new Media(MimeTypeUtils.ALL, new InputStreamResource(new ByteArrayInputStream(fileContent))));
                    } catch (IOException e) {
                        // 可以记录日志，继续处理其他文件
                        log.warn("Failed to load input stream for file ID: {}", item.getId(), e);
                    }
                }
            }

            return mediaList;
        } catch (Exception e) {
            log.error("Error fetching storage items for file IDs: {}", fileIds, e);
            return Collections.emptyList();
        }
    }

    /**
     * 批量回填对话内容
     */
    private void fillContent(List<ChatDialogue> dialogues) {
        if (dialogues == null || dialogues.isEmpty()) {
            return;
        }
        List<Long> ids = dialogues.stream()
                .map(ChatDialogue::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return;
        }
        Map<Long, String> contentMap = aiChatDialogueContentMapper.selectByDialogueIds(ids)
                .stream()
                .collect(Collectors.toMap(AiChatDialogueContentDO::getDialogueId, AiChatDialogueContentDO::getContent,
                        (a, b) -> a));
        dialogues.forEach(d -> d.setContent(contentMap.get(d.getId())));
    }

}
