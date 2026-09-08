package cn.iocoder.yudao.module.ai.core.chat.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.ai.common.model.entity.DeletedStatus;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiChatDialogueDO;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ChatDialogue;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.HourlyStats;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 聊天对话 Mapper
 *
 * @author yudao
 */
@Mapper
public interface AiChatDialogueMapper extends BaseMapperX<AiChatDialogueDO> {

    /**
     * 分页查询（XML - 返回 ChatDialogue 带分页参数）
     */
    Page<ChatDialogue> pageQuery(@Param("page") Page<ChatDialogue> page, @Param("entity") AiChatDialogueDO aiChatDialogueDO);

    /**
     * 按主题 ID 获取历史对话（XML - 返回 ChatDialogue）
     */
    List<ChatDialogue> getHistoryByTopicId(Long topicId, Integer size);

    /**
     * 按主题 ID 获取历史对话，窗口以 beforeId 消息为截止点（不包含其后的后续对话）
     * （XML - 返回 ChatDialogue，按 create_time desc）
     */
    List<ChatDialogue> getHistoryByTopicIdBefore(@Param("topicId") Long topicId, @Param("size") Integer size,
                                                 @Param("beforeId") Long beforeId);

    /**
     * 按主题 ID 和创建者获取对话（XML - 返回 ChatDialogue）
     */
    List<ChatDialogue> getChatDialogueByTopicId(Long topicId, String creator);

    /**
     * 获取今日每小时统计数据（XML - 复杂 JOIN + 聚合）
     */
    List<HourlyStats> selectHourlyStats(@Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    /**
     * 获取昨日每小时统计数据（XML - 复杂 JOIN + 聚合）
     */
    List<HourlyStats> selectHourlyStatsYesterday(@Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 软删除对话（按 lastId）
     */
    default Boolean del(AiChatDialogueDO aiChatDialogueDO) {
        update(new LambdaUpdateWrapper<AiChatDialogueDO>()
                .eq(AiChatDialogueDO::getId, aiChatDialogueDO.getLastId())
                .or()
                .eq(AiChatDialogueDO::getLastId, aiChatDialogueDO.getLastId())
                .set(AiChatDialogueDO::getDeleted, true));
        return true;
    }

    /**
     * 批量更新删除状态（按主题 ID 列表）
     */
    default Boolean updateDeletedByTopicIds(DeletedStatus deletedStatus) {
        update(new LambdaUpdateWrapper<AiChatDialogueDO>()
                .in(AiChatDialogueDO::getTopicId, deletedStatus.getIds())
                .set(AiChatDialogueDO::getDeleted, deletedStatus.getDeleted()));
        return true;
    }

    /**
     * 清空所有对话（排除忽略的主题 ID）
     */
    default Boolean clearAllDialogueByIgnoreTopicIds(String creator, List<Long> ignoreTopicIds) {
        LambdaUpdateWrapper<AiChatDialogueDO> wrapper = new LambdaUpdateWrapper<AiChatDialogueDO>()
                .eq(AiChatDialogueDO::getCreator, creator)
                .set(AiChatDialogueDO::getDeleted, true);
        if (ignoreTopicIds != null && !ignoreTopicIds.isEmpty()) {
            wrapper.notIn(AiChatDialogueDO::getTopicId, ignoreTopicIds);
        }
        update(wrapper);
        return true;
    }

}
