package cn.iocoder.yudao.module.analytics.controller.admin.aiusage;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.analytics.controller.admin.aiusage.vo.AnalyticsAiUsageSummaryRespVO;
import cn.iocoder.yudao.module.analytics.service.aiusage.AnalyticsAiUsageService;
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

@Tag(name = "管理后台 - AI 使用分析")
@RestController
@RequestMapping("/analytics/ai-usage")
@Validated
public class AnalyticsAiUsageController {

    @Resource
    private AnalyticsAiUsageService analyticsAiUsageService;

    @GetMapping("/summary")
    @Operation(summary = "获得 AI 使用汇总统计")
    @Parameter(name = "days", description = "趋势天数", example = "30")
    @PreAuthorize("@ss.hasPermission('analytics:ai-usage:query')")
    public CommonResult<AnalyticsAiUsageSummaryRespVO> getSummary(
            @RequestParam(value = "days", defaultValue = "30") @Min(1) @Max(90) Integer days) {
        return success(analyticsAiUsageService.getSummary(days));
    }

}