package cn.iocoder.yudao.module.ai.core.workflow.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.core.workflow.dal.dataobject.AiWorkflowDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI 工作流定义 Mapper
 *
 * @author yudao
 */
@Mapper
public interface AiWorkflowMapper extends BaseMapperX<AiWorkflowDO> {

    default AiWorkflowDO selectById(Long id) {
        return selectOne(new LambdaQueryWrapperX<AiWorkflowDO>()
                .eq(AiWorkflowDO::getId, id)
                .eq(AiWorkflowDO::getDeleted, false));
    }

    default Boolean delete(List<Long> ids) {
        update(new LambdaUpdateWrapper<AiWorkflowDO>()
                .in(AiWorkflowDO::getId, ids)
                .set(AiWorkflowDO::getDeleted, true));
        return true;
    }

    default Boolean increaseUsageCount(Long id) {
        update(new LambdaUpdateWrapper<AiWorkflowDO>()
                .eq(AiWorkflowDO::getId, id)
                .setSql("usage_count = usage_count + 1"));
        return true;
    }

    /**
     * 校验名称唯一（同一租户、未删除）
     */
    default AiWorkflowDO selectByName(String name, Long excludeId) {
        return selectOne(new LambdaQueryWrapperX<AiWorkflowDO>()
                .eq(AiWorkflowDO::getName, name)
                .ne(excludeId != null, AiWorkflowDO::getId, excludeId)
                .eq(AiWorkflowDO::getDeleted, false)
                .last("limit 1"));
    }
}
