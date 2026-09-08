package cn.iocoder.yudao.module.hub.core.market.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubClusterDetailRespVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubClusterPageReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubClusterRespVO;
import cn.iocoder.yudao.module.hub.core.market.service.HubClusterService;
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

@Tag(name = "用户前台 - Hub 智能体集群")
@RestController
@RequestMapping("/hub/cluster")
@Validated
@TenantIgnore
public class HubClusterController {

    @Resource
    private HubClusterService hubClusterService;

    @GetMapping("/page")
    @Operation(summary = "获得可公开展示的智能体集群分页")
    @PermitAll
    public CommonResult<PageResult<HubClusterRespVO>> getClusterPage(
            @Valid HubClusterPageReqVO pageReqVO) {
        return success(hubClusterService.getPublishedClusterPage(pageReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得可公开展示的智能体集群详情")
    @Parameter(name = "id", description = "市场记录编号", required = true, example = "1")
    @PermitAll
    public CommonResult<HubClusterDetailRespVO> getCluster(
            @RequestParam("id") @NotNull(message = "市场记录编号不能为空") Long id) {
        return success(hubClusterService.getPublishedCluster(id));
    }

}
