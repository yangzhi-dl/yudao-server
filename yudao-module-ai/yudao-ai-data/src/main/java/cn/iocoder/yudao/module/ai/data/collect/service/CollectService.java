package cn.iocoder.yudao.module.ai.data.collect.service;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.data.collect.controller.admin.vo.DataSourceConfigSaveReqVO;
import cn.iocoder.yudao.module.ai.data.collect.dal.dataobject.CollectResult;
import cn.iocoder.yudao.module.ai.data.collect.dal.dataobject.CollectTask;
import cn.iocoder.yudao.module.ai.data.collect.dal.dataobject.DataSourceConfig;

/**
 * 数据采集 Service。
 */
public interface CollectService {

    /**
     * 新增数据源配置。
     */
    Long createDataSourceConfig(DataSourceConfigSaveReqVO req);

    /**
     * 更新数据源配置。
     */
    void updateDataSourceConfig(DataSourceConfigSaveReqVO req);

    /**
     * 删除数据源配置。
     */
    void deleteDataSourceConfig(Long id);

    /**
     * 查询数据源配置。
     */
    DataSourceConfig getDataSourceConfig(Long id);

    /**
     * 分页查询数据源配置。
     */
    PageResult<DataSourceConfig> getDataSourceConfigPage(PageParam pageParam, Integer sourceType, String name);

    /**
     * 触发一次采集任务，返回任务 ID。
     */
    Long triggerCollect(Long sourceConfigId);

    /**
     * 查询采集任务。
     */
    CollectTask getTask(Long id);

    /**
     * 分页查询采集任务。
     */
    PageResult<CollectTask> getTaskPage(PageParam pageParam, Long sourceConfigId);

    /**
     * 分页查询采集结果。
     */
    PageResult<CollectResult> getResultPage(PageParam pageParam, Long taskId, Integer status);

}
