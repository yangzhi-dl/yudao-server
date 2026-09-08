package cn.iocoder.yudao.module.analytics.controller.admin.ability;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.analytics.controller.admin.ability.vo.AnalyticsAbilitySummaryRespVO;
import cn.iocoder.yudao.module.analytics.service.ability.AnalyticsAbilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - AI 能力分析")
@RestController
@RequestMapping("/analytics/ability")
@Validated
public class AnalyticsAbilityController {

    @Resource
    private AnalyticsAbilityService analyticsAbilityService;

    @GetMapping("/summary")
    @Operation(summary = "获得 AI 能力汇总统计")
    @PreAuthorize("@ss.hasPermission('analytics:ability:query')")
    public CommonResult<AnalyticsAbilitySummaryRespVO> getSummary() {
        return success(analyticsAbilityService.getSummary());
    }

}