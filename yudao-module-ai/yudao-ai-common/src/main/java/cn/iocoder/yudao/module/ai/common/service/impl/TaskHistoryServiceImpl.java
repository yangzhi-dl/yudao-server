package cn.iocoder.yudao.module.ai.common.service.impl;

import cn.iocoder.yudao.module.ai.common.dal.dataobject.TaskHistoryDO;
import cn.iocoder.yudao.module.ai.common.dal.dataobject.TaskHistoryDataDO;
import cn.iocoder.yudao.module.ai.common.dal.mysql.TaskHistoryDataMapper;
import cn.iocoder.yudao.module.ai.common.dal.mysql.TaskHistoryMapper;
import cn.iocoder.yudao.module.ai.common.enums.TaskHistoryStatus;
import cn.iocoder.yudao.module.ai.common.enums.TaskHistoryType;
import cn.iocoder.yudao.module.ai.common.model.dto.QueryTaskHistoryDTO;
import cn.iocoder.yudao.module.ai.common.model.entity.InitTask;
import cn.iocoder.yudao.module.ai.common.model.entity.LoadingTaskResult;
import cn.iocoder.yudao.module.ai.common.model.entity.RecordTaskUnprocessed;
import cn.iocoder.yudao.module.ai.common.model.entity.UpdateTaskStatus;
import cn.iocoder.yudao.module.ai.common.model.vo.TaskHistoryDetailVO;
import cn.iocoder.yudao.module.ai.common.model.vo.TaskHistoryVO;
import cn.iocoder.yudao.module.ai.common.service.TaskHistoryService;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import tools.jackson.core.type.TypeReference;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Objects;

import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 任务历史 Service 实现
 *
 * @author yudao
 */
@Slf4j
@Service
@Validated
public class TaskHistoryServiceImpl implements TaskHistoryService {

    @Resource
    private TaskHistoryMapper taskHistoryMapper;

    @Resource
    private TaskHistoryDataMapper taskHistoryDataMapper;

    @Override
    public Long initTask(InitTask initTask) {
        Long objectId = initTask.getObjectId();
        TaskHistoryType taskType = initTask.getTaskType();
        String loginUserId = String.valueOf(getLoginUserId());
        Long taskId = taskHistoryMapper.existTask(objectId, taskType, loginUserId);
        if (Objects.isNull(taskId)) {
            TaskHistoryDO build = TaskHistoryDO.builder()
                    .objectId(objectId).taskName(initTask.getTaskName())
                    .taskType(taskType).scheduleType(initTask.getScheduleType())
                    .status(TaskHistoryStatus.PENDING).build();
            taskHistoryMapper.insert(build);
            taskHistoryDataMapper.insert(TaskHistoryDataDO.builder().taskId(build.getId()).build());
            return build.getId();
        }
        return taskId;
    }

    @Override
    public Long existTask(Long objectId, TaskHistoryType taskType) {
        String loginUserId = String.valueOf(getLoginUserId());
        return taskHistoryMapper.existTask(objectId, taskType, loginUserId);
    }

    @Override
    public Boolean updateTaskStatus(UpdateTaskStatus updateTaskStatus) {
        return taskHistoryMapper.updateStatusById(updateTaskStatus.getId(), updateTaskStatus.getStatus()) > 0;
    }

    @Override
    public Boolean initTaskResult(LoadingTaskResult loadingTaskResult) {
        TaskHistoryDataDO data = TaskHistoryDataDO.builder().taskId(loadingTaskResult.getId())
                .result(loadingTaskResult.getResult()).build();
        return taskHistoryMapper.updateStatusById(loadingTaskResult.getId(), loadingTaskResult.getStatus()) > 0
                && taskHistoryDataMapper.updateByTaskId(data);
    }

    @Override
    public Boolean recordTaskUnprocessed(RecordTaskUnprocessed recordTaskUnprocessed) {
        TaskHistoryDataDO data = TaskHistoryDataDO.builder().taskId(recordTaskUnprocessed.getId())
                .unprocessed(recordTaskUnprocessed.getUnprocessed()).build();
        return taskHistoryDataMapper.updateByTaskId(data);
    }

    @Override
    public <T> T loadingUnprocessed(Long taskId, TypeReference<T> typeRef) {
        String loginUserId = String.valueOf(getLoginUserId());
        if (taskHistoryMapper.existTaskById(taskId, loginUserId) == null) {
            return null;
        }
        String json = taskHistoryDataMapper.loadingUnprocessedById(taskId);
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return JsonUtils.parseObject(json, typeRef);
        } catch (Exception e) {
            log.error("反序列化失败，type: {}", typeRef.getType(), e);
            throw new RuntimeException("解析失败", e);
        }
    }

    @Override
    public <T> T loadingResult(Long taskId, TypeReference<T> typeRef) {
        String loginUserId = String.valueOf(getLoginUserId());
        if (taskHistoryMapper.existTaskById(taskId, loginUserId) == null) {
            return null;
        }
        String json = taskHistoryDataMapper.loadingResultById(taskId);
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return JsonUtils.parseObject(json, typeRef);
        } catch (Exception e) {
            log.error("反序列化失败，type: {}", typeRef.getType(), e);
            throw new RuntimeException("解析失败", e);
        }
    }

    @Override
    public Page<TaskHistoryVO> loadingPageResult(QueryTaskHistoryDTO dto) {
        return taskHistoryMapper.pageQuery(dto);
    }

    @Override
    public TaskHistoryDetailVO getTaskDetail(Long taskId, TaskHistoryType taskType) {
        TaskHistoryDetailVO taskDetail = taskHistoryMapper.getTaskDetail(taskId, taskType);
        if (Objects.nonNull(taskDetail)) {
            taskDetail.setResult(taskHistoryDataMapper.loadingResultById(taskId));
        }
        return taskDetail;
    }

    @Override
    public Boolean delete(Long id, TaskHistoryType taskType) {
        return taskHistoryMapper.deleteById(id) > 0;
    }

}
