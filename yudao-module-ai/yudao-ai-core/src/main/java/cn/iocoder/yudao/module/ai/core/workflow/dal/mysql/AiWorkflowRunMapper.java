package cn.iocoder.yudao.module.ai.core.workflow.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.core.workflow.dal.dataobject.AiWorkflowRunDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI 工作流运行记录 Mapper
 *
 * @author yudao
 */
@Mapper
public interface AiWorkflowRunMapper extends BaseMapperX<AiWorkflowRunDO> {

    default AiWorkflowRunDO selectById(Long id) {
        return selectOne(new LambdaQueryWrapperX<AiWorkflowRunDO>()
                .eq(AiWorkflowRunDO::getId, id)
                .eq(AiWorkflowRunDO::getDeleted, false));
    }

    default List<AiWorkflowRunDO> selectListByWorkflowId(Long workflowId, int limit) {
        return selectList(new LambdaQueryWrapperX<AiWorkflowRunDO>()
                .eq(AiWorkflowRunDO::getWorkflowId, workflowId)
                .eq(AiWorkflowRunDO::getDeleted, false)
                .orderByDesc(AiWorkflowRunDO::getId)
                .last("limit " + limit));
    }

    default Boolean deleteByWorkflowIds(List<Long> workflowIds) {
        update(new LambdaUpdateWrapper<AiWorkflowRunDO>()
                .in(AiWorkflowRunDO::getWorkflowId, workflowIds)
                .set(AiWorkflowRunDO::getDeleted, true));
        return true;
    }
}
