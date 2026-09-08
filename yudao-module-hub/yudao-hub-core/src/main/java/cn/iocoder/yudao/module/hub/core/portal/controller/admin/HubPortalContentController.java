package cn.iocoder.yudao.module.hub.core.portal.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.hub.core.portal.controller.admin.vo.HubPortalContentPageReqVO;
import cn.iocoder.yudao.module.hub.core.portal.controller.admin.vo.HubPortalContentRespVO;
import cn.iocoder.yudao.module.hub.core.portal.controller.admin.vo.HubPortalContentSaveReqVO;
import cn.iocoder.yudao.module.hub.core.portal.dal.dataobject.HubPortalContentDO;
import cn.iocoder.yudao.module.hub.core.portal.service.HubPortalContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
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

@Tag(name = "管理后台 - Hub 门户内容")
@RestController
@RequestMapping("/hub/portal-content")
@Validated
@TenantIgnore
public class HubPortalContentController {

    @Resource
    private HubPortalContentService portalContentService;

    @PostMapping("/create")
    @Operation(summary = "创建门户内容")
    @PreAuthorize("@ss.hasPermission('hub:portal-content:create')")
    public CommonResult<Long> createPortalContent(@Valid @RequestBody HubPortalContentSaveReqVO createReqVO) {
        return success(portalContentService.createPortalContent(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新门户内容")
    @PreAuthorize("@ss.hasPermission('hub:portal-content:update')")
    public CommonResult<Boolean> updatePortalContent(@Valid @RequestBody HubPortalContentSaveReqVO updateReqVO) {
        portalContentService.updatePortalContent(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除门户内容")
    @Parameter(name = "id", description = "内容编号", required = true)
    @PreAuthorize("@ss.hasPermission('hub:portal-content:delete')")
    public CommonResult<Boolean> deletePortalContent(@RequestParam("id") Long id) {
        portalContentService.deletePortalContent(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得门户内容")
    @Parameter(name = "id", description = "内容编号", required = true)
    @PreAuthorize("@ss.hasPermission('hub:portal-content:query')")
    public CommonResult<HubPortalContentRespVO> getPortalContent(@RequestParam("id") Long id) {
        HubPortalContentDO portalContent = portalContentService.getPortalContent(id);
        return success(BeanUtils.toBean(portalContent, HubPortalContentRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得门户内容分页")
    @PreAuthorize("@ss.hasPermission('hub:portal-content:query')")
    public CommonResult<PageResult<HubPortalContentRespVO>> getPortalContentPage(
            @Valid HubPortalContentPageReqVO pageReqVO) {
        PageResult<HubPortalContentDO> pageResult = portalContentService.getPortalContentPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, HubPortalContentRespVO.class));
    }

}
