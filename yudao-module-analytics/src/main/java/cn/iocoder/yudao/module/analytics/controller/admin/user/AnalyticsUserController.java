package cn.iocoder.yudao.module.analytics.controller.admin.user;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.analytics.controller.admin.user.vo.AnalyticsUserSummaryRespVO;
import cn.iocoder.yudao.module.analytics.service.user.AnalyticsUserService;
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

@Tag(name = "管理后台 - 用户与登录分析")
@RestController
@RequestMapping("/analytics/user")
@Validated
public class AnalyticsUserController {

    @Resource
    private AnalyticsUserService analyticsUserService;

    @GetMapping("/summary")
    @Operation(summary = "获得用户与登录汇总统计")
    @Parameter(name = "days", description = "趋势天数", example = "30")
    @PreAuthorize("@ss.hasPermission('analytics:user:query')")
    public CommonResult<AnalyticsUserSummaryRespVO> getSummary(
            @RequestParam(value = "days", defaultValue = "30") @Min(1) @Max(90) Integer days) {
        return success(analyticsUserService.getSummary(days));
    }

}