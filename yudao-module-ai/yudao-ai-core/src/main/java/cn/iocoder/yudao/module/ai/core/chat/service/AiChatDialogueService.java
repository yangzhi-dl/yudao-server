package cn.iocoder.yudao.module.ai.core.chat.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiChatDialogueDO;
import cn.iocoder.yudao.module.ai.core.chat.enums.AiModelType;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ChatDialoguePageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ChatDialogueStarPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ChatDialogue;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.DialogueHistory;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ExportChatDialogue;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.HourlyStats;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.ChatDialogueVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.ChatStarDialogueVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: Aitenry
 * @Date: 2023/01/22 00:00
 * @Version: v1.0.0
 * @Description: TODO
 **/
public interface AiChatDialogueService {

    PageResult<ChatDialogueVO> chatDialoguePageQuery(ChatDialoguePageQueryDTO dialoguePageQueryDto);

    void insertDialogue(AiChatDialogueDO aiChatDialogueDO, String content);

    default void insertDialogue(AiChatDialogueDO aiChatDialogueDO) {
        insertDialogue(aiChatDialogueDO, null);
    }

    /**
     * 查询对话（含内容子表），用于重新生成时读取用户问题
     */
    ChatDialogue getDialogueById(Long id);

    /**
     * 判断对话是否属于当前用户的指定话题。
     *
     * @param dialogueId 对话编号
     * @param topicId 话题编号
     * @return 是否属于当前用户的指定话题
     */
    boolean isDialogueInTopic(Long dialogueId, Long topicId);

    /**
     * 查询指定用户消息的回答（assistant 行，last_id = 用户消息 id）
     */
    AiChatDialogueDO getAssistantDialogueByLastId(Long lastId);

    /**
     * 原地更新 assistant 对话的内容与元数据（重新生成使用，不改变行 id 与消息链）
     */
    void updateDialogueContent(Long dialogueId, String content, String metadata, String tools);

    Boolean switchStar(Long id, Boolean status);

    /**
     * 加载对话历史（excludeDialogueId 非空时排除被重生成的旧回答，避免模型看到自己的旧答案）
     */
    DialogueHistory loadingDialogueHistory(Long topicId, Integer windowContent, AiModelType modelType, Long excludeDialogueId);

    /**
     * 加载对话历史：beforeDialogueId 非空时窗口以该消息为截止点（不包含其后的后续对话），
     * 用于重新生成中间消息时避免模型"看到未来"
     */
    DialogueHistory loadingDialogueHistory(Long topicId, Integer windowContent, AiModelType modelType,
                                           Long excludeDialogueId, Long beforeDialogueId);

    default DialogueHistory loadingDialogueHistory(Long topicId, Integer windowContent, AiModelType modelType) {
        return loadingDialogueHistory(topicId, windowContent, modelType, null, null);
    }

    default DialogueHistory loadingDialogueHistory(Long topicId, Integer windowContent) {
        return loadingDialogueHistory(topicId, windowContent, null, null, null);
    }

    Boolean delDialogue(Long lastId);

    Boolean updateDeletedByTopicIds(List<Long> topicIds);

    Boolean exchangeFeedback(Long id, Integer status);

    List<HourlyStats> selectHourlyStats(LocalDateTime startTime, LocalDateTime endTime);

    List<HourlyStats> selectHourlyStatsYesterday(LocalDateTime startTime, LocalDateTime endTime);

    PageResult<ChatStarDialogueVO> chatDialogueStarPageQuery(ChatDialogueStarPageQueryDTO dto);

    Boolean clearAllDialogueByIgnoreTopicIds(List<Long> ignoreTopicIds);

    List<ExportChatDialogue> getChatDialogueByTopicId(Long topicId);

}
