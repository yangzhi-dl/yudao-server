package cn.iocoder.yudao.module.ai.common.service;

import cn.iocoder.yudao.module.ai.common.enums.TaskHistoryType;
import cn.iocoder.yudao.module.ai.common.model.dto.QueryTaskHistoryDTO;
import cn.iocoder.yudao.module.ai.common.model.entity.InitTask;
import cn.iocoder.yudao.module.ai.common.model.entity.LoadingTaskResult;
import cn.iocoder.yudao.module.ai.common.model.entity.RecordTaskUnprocessed;
import cn.iocoder.yudao.module.ai.common.model.entity.UpdateTaskStatus;
import cn.iocoder.yudao.module.ai.common.model.vo.TaskHistoryDetailVO;
import cn.iocoder.yudao.module.ai.common.model.vo.TaskHistoryVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import tools.jackson.core.type.TypeReference;

public interface TaskHistoryService {

    Long initTask(InitTask initTask);

    Long existTask(Long objectId, TaskHistoryType taskType);

    Boolean updateTaskStatus(UpdateTaskStatus updateTaskStatus);

    Boolean initTaskResult(LoadingTaskResult loadingTaskResult);

    Boolean recordTaskUnprocessed(RecordTaskUnprocessed recordTaskUnprocessed);

    <T> T loadingUnprocessed(Long taskId, TypeReference<T> typeRef);

    <T> T loadingResult(Long taskId, TypeReference<T> typeRef);

    Page<TaskHistoryVO> loadingPageResult(QueryTaskHistoryDTO dto);

    TaskHistoryDetailVO getTaskDetail(Long taskId, TaskHistoryType taskType);

    default TaskHistoryDetailVO getTaskDetail(Long taskId) {
        return getTaskDetail(taskId, null);
    }

    Boolean delete(Long id, TaskHistoryType taskType);

    default Boolean delete(Long id) {
        return delete(id, null);
    }
}
