package cn.iocoder.yudao.module.ai.core.chat.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiChatRegenerateDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * AI 回答重新生成版本 Mapper
 *
 * @author yudao
 */
@Mapper
public interface AiChatRegenerateMapper extends BaseMapperX<AiChatRegenerateDO> {

    /**
     * 查询指定 assistant 对话的全部版本（按版本序号升序，旧数据 version 相同时按创建时间）
     */
    default List<AiChatRegenerateDO> selectListByDialogueId(Long dialogueId) {
        return selectList(new LambdaQueryWrapperX<AiChatRegenerateDO>()
                .eq(AiChatRegenerateDO::getDialogueId, dialogueId)
                .eq(AiChatRegenerateDO::getDeleted, false)
                .orderByAsc(AiChatRegenerateDO::getVersion)
                .orderByAsc(AiChatRegenerateDO::getCreateTime)
                .orderByAsc(AiChatRegenerateDO::getId));
    }

    /**
     * 查询指定 assistant 对话列表下的全部版本
     */
    default List<AiChatRegenerateDO> selectListByDialogueIds(Collection<Long> dialogueIds) {
        return selectList(new LambdaQueryWrapperX<AiChatRegenerateDO>()
                .in(AiChatRegenerateDO::getDialogueId, dialogueIds)
                .eq(AiChatRegenerateDO::getDeleted, false)
                .orderByAsc(AiChatRegenerateDO::getVersion)
                .orderByAsc(AiChatRegenerateDO::getCreateTime));
    }

    /**
     * 查询指定话题下的全部版本
     */
    default List<AiChatRegenerateDO> selectListByTopicId(Long topicId) {
        return selectList(new LambdaQueryWrapperX<AiChatRegenerateDO>()
                .eq(AiChatRegenerateDO::getTopicId, topicId)
                .eq(AiChatRegenerateDO::getDeleted, false)
                .orderByAsc(AiChatRegenerateDO::getVersion)
                .orderByAsc(AiChatRegenerateDO::getCreateTime)
                .orderByAsc(AiChatRegenerateDO::getId));
    }

    /**
     * 更新指定 assistant 对话的当前版本标记：仅 currentVersionId 行 is_current = 1，其余置 0
     */
    default void updateCurrentFlag(Long dialogueId, Long currentVersionId) {
        update(new LambdaUpdateWrapper<AiChatRegenerateDO>()
                .eq(AiChatRegenerateDO::getDialogueId, dialogueId)
                .set(AiChatRegenerateDO::getIsCurrent, false));
        if (currentVersionId != null) {
            AiChatRegenerateDO update = new AiChatRegenerateDO();
            update.setId(currentVersionId);
            update.setIsCurrent(true);
            updateById(update);
        }
    }

    /**
     * 按 assistant 对话 ID 软删版本
     */
    default void updateDeletedByDialogueId(Long dialogueId) {
        update(new LambdaUpdateWrapper<AiChatRegenerateDO>()
                .eq(AiChatRegenerateDO::getDialogueId, dialogueId)
                .set(AiChatRegenerateDO::getDeleted, true));
    }

    /**
     * 按 assistant 对话 ID 列表批量软删版本
     */
    default void updateDeletedByDialogueIds(Collection<Long> dialogueIds) {
        update(new LambdaUpdateWrapper<AiChatRegenerateDO>()
                .in(AiChatRegenerateDO::getDialogueId, dialogueIds)
                .set(AiChatRegenerateDO::getDeleted, true));
    }

    /**
     * 按用户消息 ID（问题锚点）软删版本
     */
    default void updateDeletedByLastId(Long lastId) {
        update(new LambdaUpdateWrapper<AiChatRegenerateDO>()
                .eq(AiChatRegenerateDO::getLastId, lastId)
                .set(AiChatRegenerateDO::getDeleted, true));
    }

    /**
     * 按话题 ID 列表批量软删版本
     */
    default void updateDeletedByTopicIds(Collection<Long> topicIds) {
        update(new LambdaUpdateWrapper<AiChatRegenerateDO>()
                .in(AiChatRegenerateDO::getTopicId, topicIds)
                .set(AiChatRegenerateDO::getDeleted, true));
    }

    /**
     * 按创建者软删版本（清空全部对话时使用）
     */
    default void updateDeletedByCreator(String creator, Collection<Long> ignoreTopicIds) {
        LambdaUpdateWrapper<AiChatRegenerateDO> wrapper = new LambdaUpdateWrapper<AiChatRegenerateDO>()
                .eq(AiChatRegenerateDO::getCreator, creator)
                .set(AiChatRegenerateDO::getDeleted, true);
        if (ignoreTopicIds != null && !ignoreTopicIds.isEmpty()) {
            wrapper.notIn(AiChatRegenerateDO::getTopicId, ignoreTopicIds);
        }
        update(wrapper);
    }

}
