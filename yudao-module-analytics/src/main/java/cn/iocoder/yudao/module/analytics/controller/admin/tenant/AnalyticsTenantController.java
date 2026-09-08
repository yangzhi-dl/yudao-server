package cn.iocoder.yudao.module.analytics.controller.admin.tenant;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.analytics.controller.admin.tenant.vo.AnalyticsTenantSummaryRespVO;
import cn.iocoder.yudao.module.analytics.service.tenant.AnalyticsTenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 租户运营分析")
@RestController
@RequestMapping("/analytics/tenant")
@Validated
public class AnalyticsTenantController {

    @Resource
    private AnalyticsTenantService analyticsTenantService;

    @GetMapping("/summary")
    @Operation(summary = "获得租户运营汇总统计")
    @Parameter(name = "days", description = "趋势天数", example = "30")
    @PreAuthorize("@ss.hasPermission('analytics:tenant:query')")
    public CommonResult<AnalyticsTenantSummaryRespVO> getSummary(
            @RequestParam(value = "days", defaultValue = "30") @Min(1) @Max(90) Integer days) {
        return success(analyticsTenantService.getSummary(days));
    }

}