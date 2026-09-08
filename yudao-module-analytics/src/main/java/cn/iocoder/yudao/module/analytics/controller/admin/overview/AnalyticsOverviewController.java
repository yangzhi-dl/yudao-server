package cn.iocoder.yudao.module.analytics.controller.admin.overview;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.analytics.controller.admin.overview.vo.AnalyticsOverviewSummaryRespVO;
import cn.iocoder.yudao.module.analytics.controller.admin.overview.vo.AnalyticsOverviewTenantStatsRespVO;
import cn.iocoder.yudao.module.analytics.service.overview.AnalyticsOverviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 平台分析总览")
@RestController
@RequestMapping("/analytics/overview")
@Validated
public class AnalyticsOverviewController {

    @Resource
    private AnalyticsOverviewService analyticsOverviewService;

    @GetMapping("/summary")
    @Operation(summary = "获得平台汇总统计")
    @PreAuthorize("@ss.hasPermission('analytics:overview:query')")
    public CommonResult<AnalyticsOverviewSummaryRespVO> getSummary() {
        return success(analyticsOverviewService.getSummary());
    }

    @GetMapping("/tenant-stats")
    @Operation(summary = "获得各租户统计明细")
    @PreAuthorize("@ss.hasPermission('analytics:overview:query')")
    public CommonResult<List<AnalyticsOverviewTenantStatsRespVO>> getTenantStats() {
        return success(analyticsOverviewService.getTenantStats());
    }

}