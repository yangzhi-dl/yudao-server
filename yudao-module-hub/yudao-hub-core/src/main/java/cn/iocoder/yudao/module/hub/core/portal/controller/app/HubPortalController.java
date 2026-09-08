package cn.iocoder.yudao.module.hub.core.portal.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.hub.core.portal.controller.app.vo.HubHomeRespVO;
import cn.iocoder.yudao.module.hub.core.portal.service.HubPortalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户前台 - Hub 门户")
@RestController
@RequestMapping("/hub/portal")
@Validated
@TenantIgnore
public class HubPortalController {

    @Resource
    private HubPortalService portalService;

    @GetMapping("/home")
    @Operation(summary = "获得 Hub 门户首页内容")
    @PermitAll
    public CommonResult<HubHomeRespVO> getHome() {
        return success(portalService.getHome());
    }

}
