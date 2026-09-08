package cn.iocoder.yudao.module.ai.core.chat.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.common.model.entity.DeletedStatus;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiChatTopicDO;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ArchivedStatus;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.TopicRange;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.ChatTopicVO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 聊天主题 Mapper
 *
 * @author yudao
 */
@Mapper
public interface AiChatTopicMapper extends BaseMapperX<AiChatTopicDO> {

    /**
     * 分页查询（XML - 返回 ChatTopicVO 带分页参数）
     */
    Page<ChatTopicVO> pageQuery(@Param("page") Page<ChatTopicVO> page, @Param("entity") AiChatTopicDO aiChatTopicDO);

    /**
     * 批量更新删除状态
     */
    default Boolean updateTopicDeleted(DeletedStatus deletedStatus) {
        update(new LambdaUpdateWrapper<AiChatTopicDO>()
                .in(AiChatTopicDO::getId, deletedStatus.getIds())
                .set(AiChatTopicDO::getDeleted, deletedStatus.getDeleted()));
        return true;
    }

    /**
     * 批量更新归档状态
     */
    default Boolean updateTopicArchived(ArchivedStatus archivedStatus) {
        update(new LambdaUpdateWrapper<AiChatTopicDO>()
                .in(AiChatTopicDO::getId, archivedStatus.getIds())
                .set(AiChatTopicDO::getIsArchived, archivedStatus.getIsArchived()));
        return true;
    }

    /**
     * 清空所有主题（排除忽略的 ID）
     */
    default Boolean clearAllTopicByIgnoreIds(String creator, List<Long> ignoreIds) {
        LambdaUpdateWrapper<AiChatTopicDO> wrapper = new LambdaUpdateWrapper<AiChatTopicDO>()
                .eq(AiChatTopicDO::getCreator, creator)
                .set(AiChatTopicDO::getDeleted, true);
        if (ignoreIds != null && !ignoreIds.isEmpty()) {
            wrapper.notIn(AiChatTopicDO::getId, ignoreIds);
        }
        update(wrapper);
        return true;
    }

    /**
     * 获取用户归档主题 ID
     */
    default List<Long> getArchiveTopicId(String creator) {
        return selectList(new LambdaQueryWrapperX<AiChatTopicDO>()
                .eq(AiChatTopicDO::getIsArchived, true)
                .eq(AiChatTopicDO::getCreator, creator))
                .stream().map(AiChatTopicDO::getId).toList();
    }

    /**
     * 按时间范围查询主题
     */
    default List<TopicRange> selectTopicsByCreateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapperX<AiChatTopicDO> wrapper = new LambdaQueryWrapperX<AiChatTopicDO>()
                .eq(AiChatTopicDO::getDeleted, false);
        if (startTime != null) {
            wrapper.ge(AiChatTopicDO::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(AiChatTopicDO::getCreateTime, endTime);
        }
        wrapper.orderByDesc(AiChatTopicDO::getCreateTime);
        return selectList(wrapper).stream().map(doObj -> TopicRange.builder()
                .id(doObj.getId()).title(doObj.getTitle()).createTime(doObj.getCreateTime()).build()).toList();
    }

}
