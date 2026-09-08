package cn.iocoder.yudao.module.ai.core.chat.service.impl;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiChatDialogueContentDO;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiChatDialogueDO;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiChatRegenerateDO;
import cn.iocoder.yudao.module.ai.core.chat.dal.mysql.AiChatDialogueContentMapper;
import cn.iocoder.yudao.module.ai.core.chat.dal.mysql.AiChatDialogueMapper;
import cn.iocoder.yudao.module.ai.core.chat.dal.mysql.AiChatRegenerateMapper;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ChatTool;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ContextType;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.DocMetadataVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.RegenerateVersionVO;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatRegenerateService;
import cn.iocoder.yudao.module.ai.core.chat.utils.ContentDispatcher;
import cn.iocoder.yudao.module.ai.core.rag.utils.DocMetadataUtil;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.ai.common.constans.ErrorCodeConstants.REGENERATE_VERSION_NOT_EXISTS;

/**
 * AI 回答重新生成版本 Service 实现
 * <p>
 * 版本表存储全部回答版本（含当前生效版本）；主行始终保存当前版本内容作为消息链锚点。
 *
 * @author yudao
 */
@Service
@Slf4j
public class AiChatRegenerateServiceImpl implements AiChatRegenerateService {

    private final AiChatRegenerateMapper aiChatRegenerateMapper;
    private final AiChatDialogueMapper aiChatDialogueMapper;
    private final AiChatDialogueContentMapper aiChatDialogueContentMapper;
    private final DocMetadataUtil docMetadataUtil;

    public AiChatRegenerateServiceImpl(AiChatRegenerateMapper aiChatRegenerateMapper,
                                       AiChatDialogueMapper aiChatDialogueMapper,
                                       AiChatDialogueContentMapper aiChatDialogueContentMapper,
                                       DocMetadataUtil docMetadataUtil) {
        this.aiChatRegenerateMapper = aiChatRegenerateMapper;
        this.aiChatDialogueMapper = aiChatDialogueMapper;
        this.aiChatDialogueContentMapper = aiChatDialogueContentMapper;
        this.docMetadataUtil = docMetadataUtil;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRegenerateVersion(Long dialogueId, String content, String metadata, String tools) {
        AiChatDialogueDO dialogue = aiChatDialogueMapper.selectById(dialogueId);
        if (Objects.isNull(dialogue) || Boolean.TRUE.equals(dialogue.getDeleted())) {
            return;
        }
        List<AiChatRegenerateDO> exists = aiChatRegenerateMapper.selectListByDialogueId(dialogueId);
        int maxVersion = exists.stream()
                .mapToInt(r -> Objects.isNull(r.getVersion()) ? 1 : r.getVersion())
                .max().orElse(0);
        // 首次重生成：把主行当前内容补为 version=1（原始回答，作为历史版本）
        if (exists.isEmpty()) {
            AiChatDialogueContentDO oldContentDO = aiChatDialogueContentMapper.selectByDialogueId(dialogueId);
            String oldContent = Objects.isNull(oldContentDO) ? null : oldContentDO.getContent();
            if (StringUtils.isNotBlank(oldContent) && !"[]".equals(oldContent.trim())) {
                insertVersion(dialogue, oldContent, dialogue.getMetadata(), dialogue.getTools(), 1, false);
                maxVersion = 1;
            }
        }
        // 新回答登记为最新版本（is_current = 1），其余版本取消当前标记
        AiChatRegenerateDO newVersion = insertVersion(dialogue, content, metadata, tools, maxVersion + 1, true);
        aiChatRegenerateMapper.updateCurrentFlag(dialogueId, newVersion.getId());
        log.info("AI 回答新版本已登记，dialogueId: {}, version: {}", dialogueId, newVersion.getVersion());
    }

    @Override
    public List<RegenerateVersionVO> listVersions(Long dialogueId) {
        AiChatDialogueDO dialogue = aiChatDialogueMapper.selectById(dialogueId);
        if (Objects.isNull(dialogue) || !isOwner(dialogue.getCreator())) {
            return new ArrayList<>();
        }
        List<RegenerateVersionVO> result = new ArrayList<>();
        boolean hasCurrent = false;
        for (AiChatRegenerateDO regenerate : aiChatRegenerateMapper.selectListByDialogueId(dialogueId)) {
            boolean isCurrent = Boolean.TRUE.equals(regenerate.getIsCurrent());
            if (isCurrent) {
                hasCurrent = true;
            }
            result.add(buildVO(regenerate.getId(), regenerate.getDialogueId(), isCurrent,
                    regenerate.getContent(), regenerate.getMetadata(), regenerate.getTools(),
                    regenerate.getCreateTime()));
        }
        // 兼容旧数据：版本表无当前版本标记时，主行当前内容作为最新版本追加
        if (!hasCurrent) {
            AiChatDialogueContentDO contentDO = aiChatDialogueContentMapper.selectByDialogueId(dialogueId);
            String content = Objects.isNull(contentDO) ? null : contentDO.getContent();
            if (StringUtils.isNotBlank(content)) {
                result.add(buildVO(dialogue.getId(), dialogue.getId(), true, content,
                        dialogue.getMetadata(), dialogue.getTools(), dialogue.getCreateTime()));
            }
        }
        return result;
    }

    @Override
    public List<RegenerateVersionVO> listVersionsByTopic(Long topicId) {
        List<RegenerateVersionVO> result = new ArrayList<>();
        String loginUserId = String.valueOf(getLoginUserId());
        // 该话题下所有 assistant 主行（用于兼容旧数据补当前版）
        List<AiChatDialogueDO> dialogues = aiChatDialogueMapper.selectList(
                new LambdaQueryWrapperX<AiChatDialogueDO>()
                        .eq(AiChatDialogueDO::getTopicId, topicId)
                        .eq(AiChatDialogueDO::getSender, "assistant")
                        .eq(AiChatDialogueDO::getDeleted, false)
                        .eq(AiChatDialogueDO::getCreator, loginUserId));
        Map<Long, String> contentMap = Map.of();
        if (!dialogues.isEmpty()) {
            List<Long> ids = dialogues.stream().map(AiChatDialogueDO::getId).toList();
            contentMap = aiChatDialogueContentMapper.selectByDialogueIds(ids).stream()
                    .collect(Collectors.toMap(AiChatDialogueContentDO::getDialogueId,
                            AiChatDialogueContentDO::getContent, (a, b) -> a));
        }
        // 版本行按 dialogueId 分组（组内按 version 升序）
        Map<Long, List<AiChatRegenerateDO>> versionGroups = aiChatRegenerateMapper.selectListByTopicId(topicId)
                .stream()
                .collect(Collectors.groupingBy(AiChatRegenerateDO::getDialogueId));
        for (AiChatDialogueDO dialogue : dialogues) {
            List<AiChatRegenerateDO> versions = versionGroups.getOrDefault(dialogue.getId(), List.of());
            boolean hasCurrent = false;
            for (AiChatRegenerateDO regenerate : versions) {
                boolean isCurrent = Boolean.TRUE.equals(regenerate.getIsCurrent());
                if (isCurrent) {
                    hasCurrent = true;
                }
                result.add(buildVO(regenerate.getId(), regenerate.getDialogueId(), isCurrent,
                        regenerate.getContent(), regenerate.getMetadata(), regenerate.getTools(),
                        regenerate.getCreateTime()));
            }
            // 兼容旧数据：有版本历史但无当前标记时，主行内容补为当前版（追加在组尾）
            if (!hasCurrent && !versions.isEmpty()) {
                String content = contentMap.get(dialogue.getId());
                if (StringUtils.isNotBlank(content)) {
                    result.add(buildVO(dialogue.getId(), dialogue.getId(), true, content,
                            dialogue.getMetadata(), dialogue.getTools(), dialogue.getCreateTime()));
                }
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<RegenerateVersionVO> switchVersion(Long dialogueId, Long versionId) {
        AiChatRegenerateDO version = aiChatRegenerateMapper.selectById(versionId);
        AiChatDialogueDO dialogue = aiChatDialogueMapper.selectById(dialogueId);
        if (Objects.isNull(version) || Objects.isNull(dialogue)
                || !dialogueId.equals(version.getDialogueId())
                || !isOwner(dialogue.getCreator())) {
            throw exception(REGENERATE_VERSION_NOT_EXISTS);
        }
        // 兼容旧数据：版本表无当前标记时，先把主行当前内容补为最新版本行，避免切换后丢失
        List<AiChatRegenerateDO> exists = aiChatRegenerateMapper.selectListByDialogueId(dialogueId);
        boolean hasCurrent = exists.stream().anyMatch(r -> Boolean.TRUE.equals(r.getIsCurrent()));
        if (!hasCurrent) {
            AiChatDialogueContentDO currentContentDO = aiChatDialogueContentMapper.selectByDialogueId(dialogueId);
            String currentContent = Objects.isNull(currentContentDO) ? null : currentContentDO.getContent();
            if (StringUtils.isNotBlank(currentContent)) {
                int maxVersion = exists.stream()
                        .mapToInt(r -> Objects.isNull(r.getVersion()) ? 1 : r.getVersion())
                        .max().orElse(0);
                insertVersion(dialogue, currentContent, dialogue.getMetadata(), dialogue.getTools(),
                        maxVersion + 1, false);
            }
        }
        // 1) 主行 <- 目标版本内容（消息链锚点同步，上下文/导出/收藏/反馈基于当前版本）
        AiChatDialogueDO dialogueUpdate = AiChatDialogueDO.builder()
                .id(dialogueId)
                .metadata(version.getMetadata())
                .tools(version.getTools())
                .fileIds(version.getFileIds())
                .build();
        aiChatDialogueMapper.updateById(dialogueUpdate);
        AiChatDialogueContentDO contentDO = aiChatDialogueContentMapper.selectByDialogueId(dialogueId);
        if (Objects.nonNull(contentDO)) {
            contentDO.setContent(version.getContent());
            aiChatDialogueContentMapper.updateById(contentDO);
        } else if (StringUtils.isNotBlank(version.getContent())) {
            aiChatDialogueContentMapper.insert(AiChatDialogueContentDO.builder()
                    .dialogueId(dialogueId).content(version.getContent()).build());
        }
        // 2) 目标版本行标记为当前生效，其余版本取消标记
        aiChatRegenerateMapper.updateCurrentFlag(dialogueId, versionId);
        log.info("AI 回答版本已切换，dialogueId: {}, versionId: {}", dialogueId, versionId);
        return listVersions(dialogueId);
    }

    @Override
    public void deleteByDialogueId(Long dialogueId) {
        aiChatRegenerateMapper.updateDeletedByDialogueId(dialogueId);
    }

    @Override
    public void deleteByDialogueIds(Collection<Long> dialogueIds) {
        if (dialogueIds != null && !dialogueIds.isEmpty()) {
            aiChatRegenerateMapper.updateDeletedByDialogueIds(dialogueIds);
        }
    }

    @Override
    public void deleteByLastId(Long lastId) {
        aiChatRegenerateMapper.updateDeletedByLastId(lastId);
    }

    @Override
    public void deleteByTopicIds(Collection<Long> topicIds) {
        if (topicIds != null && !topicIds.isEmpty()) {
            aiChatRegenerateMapper.updateDeletedByTopicIds(topicIds);
        }
    }

    @Override
    public void deleteByCreator(String creator, Collection<Long> ignoreTopicIds) {
        aiChatRegenerateMapper.updateDeletedByCreator(creator, ignoreTopicIds);
    }

    /**
     * 插入一个版本行
     */
    private AiChatRegenerateDO insertVersion(AiChatDialogueDO dialogue, String content, String metadata,
                                             String tools, int version, boolean isCurrent) {
        AiChatRegenerateDO regenerate = AiChatRegenerateDO.builder()
                .dialogueId(dialogue.getId())
                .topicId(dialogue.getTopicId())
                .lastId(dialogue.getLastId())
                .version(version)
                .isCurrent(isCurrent)
                .content(content)
                .metadata(metadata)
                .tools(tools)
                .fileIds(dialogue.getFileIds())
                .deleted(false)
                .build();
        regenerate.setCreator(dialogue.getCreator());
        regenerate.setUpdater(dialogue.getCreator());
        aiChatRegenerateMapper.insert(regenerate);
        return regenerate;
    }

    /**
     * 构建版本 VO（解析 content/metadata/tools）
     */
    private RegenerateVersionVO buildVO(Long id, Long dialogueId, Boolean isCurrent, String content,
                                        String metadata, String tools, java.time.LocalDateTime createTime) {
        RegenerateVersionVO.RegenerateVersionVOBuilder builder = RegenerateVersionVO.builder()
                .id(id).dialogueId(dialogueId).isCurrent(isCurrent)
                .createTime(createTime);
        // 解析回答内容
        if (StringUtils.isNotBlank(content)) {
            ContextType contextType = new ContextType();
            new ContentDispatcher().dispatchContent(content, contextType);
            builder.aiContent(contextType.getAiContent());
            builder.aiAgentMessage(contextType.getAiAgentMessage());
        }
        // 知识库引用元数据
        List<DocMetadataVO> docMetadata = docMetadataUtil.getDocMetadata(metadata);
        builder.docMetadata(docMetadata);
        // 工具调用记录
        if (StringUtils.isNotBlank(tools)) {
            builder.tools(JSONArray.parseArray(tools, ChatTool.class));
        }
        return builder.build();
    }

    /**
     * 归属校验：主行创建者必须为当前登录用户
     */
    private boolean isOwner(String creator) {
        return Objects.equals(creator, String.valueOf(getLoginUserId()));
    }

}
