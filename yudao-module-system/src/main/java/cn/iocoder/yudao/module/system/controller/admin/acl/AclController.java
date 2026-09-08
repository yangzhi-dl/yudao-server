package cn.iocoder.yudao.module.system.controller.admin.acl;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.acl.vo.AclPermissionRespVO;
import cn.iocoder.yudao.module.system.controller.admin.acl.vo.AclPermissionPageReqVO;
import cn.iocoder.yudao.module.system.enums.acl.PrincipalType;
import cn.iocoder.yudao.module.system.service.acl.AclPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 通用 ACL 权限管理控制器
 * <p>对任意 ResourceType 提供统一的授权/撤销/查询接口
 *
 * @author IIMS
 */
@Tag(name = "管理后台 - ACL 权限")
@RestController
@RequestMapping("/acl")
@Validated
public class AclController {

    @Resource
    private AclPermissionService aclPermissionService;

    @PostMapping("/permission/list")
    @Operation(summary = "分页查询权限列表")
    public CommonResult<PageResult<AclPermissionRespVO>> listPermissions(@RequestBody @Valid AclPermissionPageReqVO dto) {
        return success(aclPermissionService.pagePermissions(dto));
    }

    @GetMapping("/principal-type/list")
    @Operation(summary = "获取当前用户可用的授权主体类型")
    public CommonResult<List<String>> getAvailablePrincipalTypes() {
        return success(aclPermissionService.getAvailablePrincipalTypes().stream()
                .map(PrincipalType::name)
                .collect(Collectors.toList()));
    }

    @GetMapping("/permission/parent-expire-time")
    @Operation(summary = "获取父级 ACL 授权的过期时间")
    public CommonResult<LocalDateTime> getParentExpireTime(
            @RequestParam("resourceType") String resourceType,
            @RequestParam("resourceId") Long resourceId) {
        return success(aclPermissionService.getParentExpireTime(resourceType, resourceId));
    }
}
