package cn.iocoder.yudao.module.ai.common.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.common.dal.dataobject.TaskHistoryDataDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 任务历史数据 Mapper
 *
 * @author yudao
 */
@Mapper
public interface TaskHistoryDataMapper extends BaseMapperX<TaskHistoryDataDO> {

    /**
     * 查询未处理数据
     */
    default String loadingUnprocessedById(Long taskId) {
        TaskHistoryDataDO entity = selectOne(new LambdaQueryWrapperX<TaskHistoryDataDO>()
                .eq(TaskHistoryDataDO::getTaskId, taskId));
        return entity != null ? entity.getUnprocessed() : null;
    }

    /**
     * 查询结果数据
     */
    default String loadingResultById(Long taskId) {
        TaskHistoryDataDO entity = selectOne(new LambdaQueryWrapperX<TaskHistoryDataDO>()
                .eq(TaskHistoryDataDO::getTaskId, taskId));
        return entity != null ? entity.getResult() : null;
    }

    /**
     * 按任务 ID 更新数据
     */
    default Boolean updateByTaskId(TaskHistoryDataDO data) {
        LambdaUpdateWrapper<TaskHistoryDataDO> wrapper = new LambdaUpdateWrapper<TaskHistoryDataDO>()
                .eq(TaskHistoryDataDO::getTaskId, data.getTaskId());
        if (data.getUnprocessed() != null) {
            wrapper.set(TaskHistoryDataDO::getUnprocessed, data.getUnprocessed());
        }
        if (data.getResult() != null) {
            wrapper.set(TaskHistoryDataDO::getResult, data.getResult());
        }
        return update(wrapper) > 0;
    }

}
