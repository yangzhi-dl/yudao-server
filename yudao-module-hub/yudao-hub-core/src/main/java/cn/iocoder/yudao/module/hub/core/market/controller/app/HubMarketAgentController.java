package cn.iocoder.yudao.module.hub.core.market.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAgentDetailRespVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAgentPageReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAgentRespVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketFilterOptionsRespVO;
import cn.iocoder.yudao.module.hub.core.market.service.HubMarketAgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户前台 - Hub 智能体市场")
@RestController
@RequestMapping("/hub/market/agent")
@Validated
@TenantIgnore
public class HubMarketAgentController {

    @Resource
    private HubMarketAgentService marketAgentService;

    @GetMapping("/page")
    @Operation(summary = "获得已发布智能体分页")
    @PermitAll
    public CommonResult<PageResult<HubMarketAgentRespVO>> getAgentPage(
            @Valid HubMarketAgentPageReqVO pageReqVO) {
        return success(marketAgentService.getPublishedAgentPage(pageReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得已发布智能体详情")
    @Parameter(name = "id", description = "市场记录编号", required = true, example = "1")
    @PermitAll
    public CommonResult<HubMarketAgentDetailRespVO> getAgent(
            @RequestParam("id") @NotNull(message = "市场记录编号不能为空") Long id) {
        return success(marketAgentService.getPublishedAgent(id));
    }

    @GetMapping("/filter-options")
    @Operation(summary = "获得智能体市场公开筛选项")
    @PermitAll
    public CommonResult<HubMarketFilterOptionsRespVO> getFilterOptions() {
        return success(marketAgentService.getFilterOptions());
    }

}
