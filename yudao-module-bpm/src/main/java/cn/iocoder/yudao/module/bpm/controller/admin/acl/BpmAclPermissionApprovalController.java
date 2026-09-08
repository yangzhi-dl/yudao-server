package cn.iocoder.yudao.module.bpm.controller.admin.acl;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.acl.vo.BpmAclPermissionApprovalCreateReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.acl.vo.BpmAclPermissionApprovalPageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.acl.vo.BpmAclPermissionApprovalRespVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.acl.BpmAclPermissionApprovalDO;
import cn.iocoder.yudao.module.bpm.service.acl.BpmAclPermissionApprovalService;
import cn.iocoder.yudao.module.system.service.acl.IPrincipalNameResolver;
import cn.iocoder.yudao.module.system.service.tenant.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * ACL 授权审批 Controller
 * <p>
 * 将 ACL 的直接授权/撤销/设置，改为走 OA 审批：先创建审批单据并发起流程，
 * 审批通过后才真正生效。前端在需要审批的场景下，改调这里的接口。
 *
 * @author IIMS
 */
@Tag(name = "管理后台 - ACL 授权审批")
@RestController
@RequestMapping("/bpm/acl/permission-approval")
@Validated
public class BpmAclPermissionApprovalController {

    @Resource
    private BpmAclPermissionApprovalService approvalService;

    @Resource
    private IPrincipalNameResolver principalNameResolver;
    @Resource
    private TenantService tenantService;

    // 说明：不配置 @PreAuthorize，与原先 AclController#grant/revoke/set 保持一致，
    // 具体是否可授权仍由审批通过后的 AclPermissionService#validateAuthPermission 判定，避免新增权限码导致现有角色无法使用。
    @PostMapping("/create")
    @Operation(summary = "创建 ACL 授权审批并发起流程")
    public CommonResult<Long> createApproval(@Valid @RequestBody BpmAclPermissionApprovalCreateReqVO createReqVO) {
        return success(approvalService.createApproval(getLoginUserId(), createReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得 ACL 授权审批单据")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<BpmAclPermissionApprovalRespVO> getApproval(@RequestParam("id") Long id) {
        BpmAclPermissionApprovalDO approval = approvalService.getApproval(id);
        return success(convert(approval));
    }

    @GetMapping("/page")
    @Operation(summary = "获得 ACL 授权审批单据分页（我的申请）")
    public CommonResult<PageResult<BpmAclPermissionApprovalRespVO>> getApprovalPage(
            @Valid BpmAclPermissionApprovalPageReqVO pageReqVO) {
        PageResult<BpmAclPermissionApprovalDO> pageResult =
                approvalService.getApprovalPage(getLoginUserId(), pageReqVO);
        PageResult<BpmAclPermissionApprovalRespVO> result = new PageResult<>();
        result.setList(convertList(pageResult.getList()));
        result.setTotal(pageResult.getTotal());
        return success(result);
    }

    private List<BpmAclPermissionApprovalRespVO> convertList(List<BpmAclPermissionApprovalDO> list) {
        // 批量解析各主体类型的名称，避免逐条查询
        List<Long> userIds = new ArrayList<>();
        List<Long> roleIds = new ArrayList<>();
        List<Long> postIds = new ArrayList<>();
        List<Long> orgIds = new ArrayList<>();
        List<Long> tenantIds = new ArrayList<>();
        for (BpmAclPermissionApprovalDO approval : list) {
            Long principalId = approval.getPrincipalId();
            switch (approval.getPrincipalType()) {
                case "USER" -> userIds.add(principalId);
                case "ROLE" -> roleIds.add(principalId);
                case "POST" -> postIds.add(principalId);
                case "ORGANIZATION" -> orgIds.add(principalId);
                case "TENANT" -> tenantIds.add(principalId);
            }
        }
        Map<Long, String> userNames = principalNameResolver.resolveUserNames(userIds);
        Map<Long, String> roleNames = principalNameResolver.resolveRoleNames(roleIds);
        Map<Long, String> postNames = principalNameResolver.resolvePostNames(postIds);
        Map<Long, String> tenantNames = resolveTenantNames(tenantIds);
        Map<Long, IPrincipalNameResolver.OrganizationInfo> orgMap = principalNameResolver.resolveAllOrganizations();

        return list.stream().map(approval -> convert(approval, userNames, roleNames, postNames, orgMap, tenantNames))
                .toList();
    }

    private BpmAclPermissionApprovalRespVO convert(BpmAclPermissionApprovalDO approval) {
        return convertList(List.of(approval)).get(0);
    }

    private BpmAclPermissionApprovalRespVO convert(BpmAclPermissionApprovalDO approval,
                                                   Map<Long, String> userNames, Map<Long, String> roleNames,
                                                   Map<Long, String> postNames,
                                                   Map<Long, IPrincipalNameResolver.OrganizationInfo> orgMap,
                                                   Map<Long, String> tenantNames) {
        BpmAclPermissionApprovalRespVO resp = new BpmAclPermissionApprovalRespVO();
        resp.setId(approval.getId());
        resp.setUserId(approval.getUserId());
        resp.setOperationType(approval.getOperationType());
        resp.setResourceType(approval.getResourceType());
        resp.setResourceId(approval.getResourceId());
        resp.setPrincipalType(approval.getPrincipalType());
        resp.setPrincipalId(approval.getPrincipalId());
        resp.setPrincipalName(resolvePrincipalName(approval.getPrincipalType(), approval.getPrincipalId(),
                userNames, roleNames, postNames, orgMap, tenantNames));
        resp.setPermissions(JsonUtils.parseArray(approval.getPermissions(), String.class));
        resp.setExpireTime(approval.getExpireTime());
        resp.setRemark(approval.getRemark());
        resp.setStatus(approval.getStatus());
        resp.setProcessInstanceId(approval.getProcessInstanceId());
        resp.setCreateTime(approval.getCreateTime());
        return resp;
    }

    /**
     * 按主体类型解析主体名称，与已授权列表（AclPermissionService）口径保持一致
     */
    private String resolvePrincipalName(String principalType, Long principalId,
                                        Map<Long, String> userNames, Map<Long, String> roleNames,
                                        Map<Long, String> postNames,
                                        Map<Long, IPrincipalNameResolver.OrganizationInfo> orgMap,
                                        Map<Long, String> tenantNames) {
        return switch (principalType) {
            case "USER" -> userNames.getOrDefault(principalId, "未知用户");
            case "ROLE" -> roleNames.getOrDefault(principalId, "未知角色");
            case "POST" -> postNames.getOrDefault(principalId, "未知岗位");
            case "ORGANIZATION" -> {
                IPrincipalNameResolver.OrganizationInfo org = orgMap.get(principalId);
                yield org != null ? org.name() : "未知组织";
            }
            case "TENANT" -> {
                String name = tenantNames.get(principalId);
                yield name != null ? name : "未知租户";
            }
            default -> principalId == null ? "" : String.valueOf(principalId);
        };
    }

    private Map<Long, String> resolveTenantNames(List<Long> tenantIds) {
        Map<Long, String> names = new HashMap<>();
        for (Long tenantId : tenantIds.stream().distinct().toList()) {
            var tenant = tenantService.getTenant(tenantId);
            names.put(tenantId, tenant != null ? tenant.getName() : null);
        }
        return names;
    }

}