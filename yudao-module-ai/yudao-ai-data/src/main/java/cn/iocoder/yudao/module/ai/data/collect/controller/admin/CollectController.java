package cn.iocoder.yudao.module.ai.data.collect.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.data.collect.controller.admin.vo.DataSourceConfigSaveReqVO;
import cn.iocoder.yudao.module.ai.data.collect.dal.dataobject.CollectResult;
import cn.iocoder.yudao.module.ai.data.collect.dal.dataobject.CollectTask;
import cn.iocoder.yudao.module.ai.data.collect.dal.dataobject.DataSourceConfig;
import cn.iocoder.yudao.module.ai.data.collect.service.CollectService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 数据采集 Controller。
 */
@RestController
@RequestMapping("/ai-data/collect")
public class CollectController {

    private final CollectService collectService;

    public CollectController(CollectService collectService) {
        this.collectService = collectService;
    }

    // ========== 数据源配置 ==========

    @PostMapping("/source/create")
    public CommonResult<Long> createDataSourceConfig(@RequestBody @Validated DataSourceConfigSaveReqVO req) {
        return success(collectService.createDataSourceConfig(req));
    }

    @PutMapping("/source/update")
    public CommonResult<Boolean> updateDataSourceConfig(@RequestBody @Validated DataSourceConfigSaveReqVO req) {
        collectService.updateDataSourceConfig(req);
        return success(true);
    }

    @DeleteMapping("/source/delete")
    public CommonResult<Boolean> deleteDataSourceConfig(@RequestParam("id") Long id) {
        collectService.deleteDataSourceConfig(id);
        return success(true);
    }

    @GetMapping("/source/get")
    public CommonResult<DataSourceConfig> getDataSourceConfig(@RequestParam("id") Long id) {
        return success(collectService.getDataSourceConfig(id));
    }

    @GetMapping("/source/page")
    public CommonResult<PageResult<DataSourceConfig>> getDataSourceConfigPage(
            @RequestParam(value = "sourceType", required = false) Integer sourceType,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);
        return success(collectService.getDataSourceConfigPage(pageParam, sourceType, name));
    }

    // ========== 采集任务与结果 ==========

    @PostMapping("/trigger")
    public CommonResult<Long> triggerCollect(@RequestParam("sourceConfigId") Long sourceConfigId) {
        return success(collectService.triggerCollect(sourceConfigId));
    }

    @GetMapping("/task/get")
    public CommonResult<CollectTask> getTask(@RequestParam("id") Long id) {
        return success(collectService.getTask(id));
    }

    @GetMapping("/task/page")
    public CommonResult<PageResult<CollectTask>> getTaskPage(
            @RequestParam(value = "sourceConfigId", required = false) Long sourceConfigId,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);
        return success(collectService.getTaskPage(pageParam, sourceConfigId));
    }

    @GetMapping("/result/page")
    public CommonResult<PageResult<CollectResult>> getResultPage(
            @RequestParam(value = "taskId", required = false) Long taskId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);
        return success(collectService.getResultPage(pageParam, taskId, status));
    }

}
