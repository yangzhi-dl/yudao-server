package cn.iocoder.yudao.module.ai.data.collect.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.data.collect.controller.admin.vo.DataSourceConfigSaveReqVO;
import cn.iocoder.yudao.module.ai.data.collect.dal.dataobject.CollectResult;
import cn.iocoder.yudao.module.ai.data.collect.dal.dataobject.CollectTask;
import cn.iocoder.yudao.module.ai.data.collect.dal.dataobject.DataSourceConfig;
import cn.iocoder.yudao.module.ai.data.collect.dal.mysql.CollectResultMapper;
import cn.iocoder.yudao.module.ai.data.collect.dal.mysql.CollectTaskMapper;
import cn.iocoder.yudao.module.ai.data.collect.dal.mysql.AiDataSourceConfigMapper;
import cn.iocoder.yudao.module.ai.data.collect.enums.CollectTaskStatus;
import cn.iocoder.yudao.module.ai.data.collect.enums.DataSourceType;
import cn.iocoder.yudao.module.ai.data.collect.service.CollectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.data.constans.DataErrorCodeConstants.COLLECT_TASK_NOT_FOUND;
import static cn.iocoder.yudao.module.ai.data.constans.DataErrorCodeConstants.DATA_SOURCE_DISABLED;
import static cn.iocoder.yudao.module.ai.data.constans.DataErrorCodeConstants.DATA_SOURCE_NOT_FOUND;
import static cn.iocoder.yudao.module.ai.data.constans.DataErrorCodeConstants.DATA_SOURCE_TYPE_INVALID;

/**
 * 数据采集 Service 实现。
 */
@Slf4j
@Service
public class CollectServiceImpl implements CollectService {

    private final AiDataSourceConfigMapper dataSourceConfigMapper;
    private final CollectTaskMapper collectTaskMapper;
    private final CollectResultMapper collectResultMapper;
    private final CollectPipeline collectPipeline;
    private final Executor dataCollectExecutor;

    public CollectServiceImpl(AiDataSourceConfigMapper dataSourceConfigMapper,
                              CollectTaskMapper collectTaskMapper,
                              CollectResultMapper collectResultMapper,
                              CollectPipeline collectPipeline,
                              @Qualifier("dataCollectExecutor") Executor dataCollectExecutor) {
        this.dataSourceConfigMapper = dataSourceConfigMapper;
        this.collectTaskMapper = collectTaskMapper;
        this.collectResultMapper = collectResultMapper;
        this.collectPipeline = collectPipeline;
        this.dataCollectExecutor = dataCollectExecutor;
    }

    @Override
    public Long createDataSourceConfig(DataSourceConfigSaveReqVO req) {
        validateSourceType(req.getSourceType());
        DataSourceConfig config = DataSourceConfig.builder()
                .sourceType(req.getSourceType())
                .name(req.getName())
                .config(req.getConfig())
                .enabled(req.getEnabled() != null ? req.getEnabled() : Boolean.TRUE)
                .remark(req.getRemark())
                .build();
        dataSourceConfigMapper.insert(config);
        return config.getId();
    }

    @Override
    public void updateDataSourceConfig(DataSourceConfigSaveReqVO req) {
        DataSourceConfig config = validateSourceConfig(req.getId());
        if (req.getSourceType() != null) {
            validateSourceType(req.getSourceType());
            config.setSourceType(req.getSourceType());
        }
        if (req.getName() != null) {
            config.setName(req.getName());
        }
        config.setConfig(req.getConfig());
        if (req.getEnabled() != null) {
            config.setEnabled(req.getEnabled());
        }
        if (req.getRemark() != null) {
            config.setRemark(req.getRemark());
        }
        dataSourceConfigMapper.updateById(config);
    }

    @Override
    public void deleteDataSourceConfig(Long id) {
        validateSourceConfig(id);
        dataSourceConfigMapper.deleteById(id);
    }

    @Override
    public DataSourceConfig getDataSourceConfig(Long id) {
        return validateSourceConfig(id);
    }

    @Override
    public PageResult<DataSourceConfig> getDataSourceConfigPage(PageParam pageParam, Integer sourceType, String name) {
        return dataSourceConfigMapper.selectPage(pageParam, sourceType, name);
    }

    @Override
    public Long triggerCollect(Long sourceConfigId) {
        DataSourceConfig config = validateSourceConfig(sourceConfigId);
        if (Boolean.FALSE.equals(config.getEnabled())) {
            throw exception(DATA_SOURCE_DISABLED);
        }
        validateSourceType(config.getSourceType());

        CollectTask task = CollectTask.builder()
                .sourceConfigId(sourceConfigId)
                .status(CollectTaskStatus.RUNNING.getValue())
                .totalCount(0)
                .successCount(0)
                .failCount(0)
                .build();
        collectTaskMapper.insert(task);

        Long taskId = task.getId();
        dataCollectExecutor.execute(() -> {
            try {
                collectPipeline.run(taskId);
            } catch (Exception e) {
                log.error("采集任务执行异常，taskId: {}", taskId, e);
                task.setStatus(CollectTaskStatus.FAIL.getValue());
                task.setRemark("采集异常: " + e.getMessage());
                collectTaskMapper.updateById(task);
            }
        });
        return taskId;
    }

    @Override
    public CollectTask getTask(Long id) {
        CollectTask task = collectTaskMapper.selectById(id);
        if (task == null) {
            throw exception(COLLECT_TASK_NOT_FOUND);
        }
        return task;
    }

    @Override
    public PageResult<CollectTask> getTaskPage(PageParam pageParam, Long sourceConfigId) {
        return collectTaskMapper.selectPage(pageParam, sourceConfigId);
    }

    @Override
    public PageResult<CollectResult> getResultPage(PageParam pageParam, Long taskId, Integer status) {
        return collectResultMapper.selectPage(pageParam, taskId, status);
    }

    private void validateSourceType(Integer sourceType) {
        if (DataSourceType.fromValue(sourceType) == null) {
            throw exception(DATA_SOURCE_TYPE_INVALID);
        }
    }

    private DataSourceConfig validateSourceConfig(Long id) {
        DataSourceConfig config = dataSourceConfigMapper.selectById(id);
        if (config == null) {
            throw exception(DATA_SOURCE_NOT_FOUND);
        }
        return config;
    }

}
