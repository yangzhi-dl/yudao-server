package cn.iocoder.yudao.module.ai.common.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.common.dal.dataobject.TaskHistoryDO;
import cn.iocoder.yudao.module.ai.common.dal.dataobject.UsageRecordDO;
import cn.iocoder.yudao.module.ai.common.enums.TaskHistoryStatus;
import cn.iocoder.yudao.module.ai.common.enums.TaskHistoryType;
import cn.iocoder.yudao.module.ai.common.model.dto.QueryTaskHistoryDTO;
import cn.iocoder.yudao.module.ai.common.model.vo.TaskHistoryDetailVO;
import cn.iocoder.yudao.module.ai.common.model.vo.TaskHistoryVO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 任务历史 Mapper
 *
 * @author yudao
 */
@Mapper
public interface TaskHistoryMapper extends BaseMapperX<TaskHistoryDO> {

    /**
     * 查询已存在的任务
     */
    default Long existTask(Long objectId, TaskHistoryType taskType, String creator) {
        TaskHistoryDO entity = selectOne(new LambdaQueryWrapperX<TaskHistoryDO>()
                .eq(TaskHistoryDO::getObjectId, objectId).eq(TaskHistoryDO::getCreator, creator)
                .eq(TaskHistoryDO::getTaskType, taskType));
        return entity != null ? entity.getId() : null;
    }

    /**
     * 按 ID 和创建者查询任务是否存在
     */
    default Long existTaskById(Long id, String creator) {
        TaskHistoryDO entity = selectOne(new LambdaQueryWrapperX<TaskHistoryDO>()
                .eq(TaskHistoryDO::getId, id).eq(TaskHistoryDO::getCreator, creator));
        return entity != null ? entity.getId() : null;
    }

    /**
     * 分页查询任务列表
     */
    default Page<TaskHistoryVO> pageQuery(QueryTaskHistoryDTO dto) {
        LambdaQueryWrapperX<TaskHistoryDO> wrapper = new LambdaQueryWrapperX<TaskHistoryDO>()
                .eqIfPresent(TaskHistoryDO::getTaskType, dto.getTaskType())
                .likeIfPresent(TaskHistoryDO::getTaskName, dto.getTaskName())
                .eqIfPresent(TaskHistoryDO::getStatus, dto.getStatus())
                .orderByDesc(TaskHistoryDO::getCreateTime);

        Page<TaskHistoryDO> page = selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(dto.getPage(), dto.getPageSize()), wrapper);

        Page<TaskHistoryVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(page.getRecords().stream().map(doObj -> {
            TaskHistoryVO vo = new TaskHistoryVO();
            vo.setId(doObj.getId());
            vo.setTaskName(doObj.getTaskName());
            vo.setTaskType(doObj.getTaskType());
            vo.setStatus(doObj.getStatus());
            return vo;
        }).toList());
        return resultPage;
    }

    /**
     * 获取任务详情
     */
    default TaskHistoryDetailVO getTaskDetail(Long taskId, TaskHistoryType taskType) {
        LambdaQueryWrapperX<TaskHistoryDO> wrapper = new LambdaQueryWrapperX<TaskHistoryDO>()
                .eq(TaskHistoryDO::getId, taskId)
                .eqIfPresent(TaskHistoryDO::getTaskType, taskType);
        TaskHistoryDO entity = selectOne(wrapper);
        if (entity == null) {
            return null;
        }
        TaskHistoryDetailVO vo = new TaskHistoryDetailVO();
        vo.setId(entity.getId());
        vo.setTaskName(entity.getTaskName());
        vo.setStatus(entity.getStatus());
        return vo;
    }

    /**
     * 按状态更新
     */
    default int updateStatusById(Long id, TaskHistoryStatus status) {
        return update(new LambdaUpdateWrapper<TaskHistoryDO>()
                .eq(TaskHistoryDO::getId, id)
                .set(TaskHistoryDO::getStatus, status));
    }

}
