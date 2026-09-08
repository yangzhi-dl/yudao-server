package cn.iocoder.yudao.module.bpm.service.acl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.acl.vo.BpmAclPermissionApprovalCreateReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.acl.vo.BpmAclPermissionApprovalPageReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.acl.BpmAclPermissionApprovalDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.acl.BpmAclPermissionApprovalMapper;
import cn.iocoder.yudao.module.bpm.enums.acl.BpmAclPermissionApprovalOperationTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import cn.iocoder.yudao.module.system.controller.admin.acl.vo.AclPermissionGrantReqVO;
import cn.iocoder.yudao.module.system.controller.admin.acl.vo.AclPermissionRevokeReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.system.enums.acl.PrincipalType;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import cn.iocoder.yudao.module.system.service.acl.AclPermissionService;
import cn.iocoder.yudao.module.system.service.acl.IPrincipalNameResolver;
import cn.iocoder.yudao.module.system.service.tenant.TenantService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.ACL_APPROVAL_NOT_EXISTS;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.ACL_APPROVAL_OPERATION_TYPE_INVALID;

/**
 * ACL 授权审批 Service 实现
 *
 * @author IIMS
 */
@Slf4j
@Service
@Validated
public class BpmAclPermissionApprovalServiceImpl implements BpmAclPermissionApprovalService {

    /**
     * ACL 授权审批对应的流程定义 KEY
     */
    public static final String PROCESS_KEY = "acl_permission_auth";

    private static final DateTimeFormatter EXPIRE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND);

    @Resource
    private BpmAclPermissionApprovalMapper approvalMapper;
    @Resource
    private BpmProcessInstanceApi processInstanceApi;
    @Resource
    private AclPermissionService aclPermissionService;
    @Resource
    private IPrincipalNameResolver principalNameResolver;
    @Resource
    private TenantService tenantService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createApproval(Long userId, BpmAclPermissionApprovalCreateReqVO createReqVO) {
        // 1. 插入 ACL 授权审批单据
        BpmAclPermissionApprovalDO approval = BpmAclPermissionApprovalDO.builder()
                .userId(userId)
                .operationType(createReqVO.getOperationType())
                .resourceType(createReqVO.getResourceType())
                .resourceId(createReqVO.getResourceId())
                .principalType(createReqVO.getPrincipalType())
                .principalId(createReqVO.getPrincipalId())
                .permissions(JsonUtils.toJsonString(createReqVO.getPermissions()))
                .expireTime(parseExpireTime(createReqVO.getExpireTime()))
                .remark(createReqVO.getRemark())
                .status(BpmTaskStatusEnum.RUNNING.getStatus())
                .build();
        approvalMapper.insert(approval);

        // 2. 发起 BPM 流程，把审批单要展示的字段放入流程变量
        Map<String, Object> processInstanceVariables =
                buildProcessVariables(approval, createReqVO.getResourceTitle());
        String processInstanceId = processInstanceApi.createProcessInstance(userId,
                new BpmProcessInstanceCreateReqDTO()
                        .setProcessDefinitionKey(PROCESS_KEY)
                        .setVariables(processInstanceVariables)
                        .setBusinessKey(String.valueOf(approval.getId()))
                        .setStartUserSelectAssignees(createReqVO.getStartUserSelectAssignees()));

        // 3. 将工作流的编号，更新到审批单据中
        approvalMapper.updateById(new BpmAclPermissionApprovalDO()
                .setId(approval.getId()).setProcessInstanceId(processInstanceId));
        return approval.getId();
    }

    /**
     * 组装发起流程时注入审批表单展示的流程变量
     */
    private Map<String, Object> buildProcessVariables(BpmAclPermissionApprovalDO approval, String resourceTitle) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("operationType", approval.getOperationType());
        variables.put("operationTypeDesc", BpmAclPermissionApprovalOperationTypeEnum
                .valueOfType(approval.getOperationType()).getDescription());
        variables.put("resourceType", approval.getResourceType());
        variables.put("resourceTypeDesc", resourceTypeDesc(approval.getResourceType()));
        variables.put("resourceId", approval.getResourceId());
        variables.put("resourceName", resourceTitle != null && !resourceTitle.isEmpty() ? resourceTitle
                : String.valueOf(approval.getResourceId()));
        variables.put("principalType", approval.getPrincipalType());
        variables.put("principalTypeDesc", principalTypeDesc(approval.getPrincipalType()));
        variables.put("principalId", approval.getPrincipalId());
        variables.put("principalName", resolvePrincipalName(approval.getPrincipalType(), approval.getPrincipalId()));
        List<String> permissions = JsonUtils.parseArray(approval.getPermissions(), String.class);
        variables.put("permissions", permissions);
        variables.put("permissionsDesc", permissions.stream().map(this::permissionDesc).toList());
        if (approval.getExpireTime() != null) {
            variables.put("expireTime", approval.getExpireTime().format(EXPIRE_TIME_FORMATTER));
        }
        variables.put("expireTimeDesc",
                approval.getExpireTime() != null
                        ? approval.getExpireTime().format(EXPIRE_TIME_FORMATTER) : "永久");
        variables.put("applyReason", approval.getRemark());
        return variables;
    }

    /**
     * 权限码的中文描述，供审批表单只读展示（如 READ → Permission#description）
     */
    private String permissionDesc(String permission) {
        for (Permission item : Permission.values()) {
            if (item.name().equals(permission)) {
                return item.getDescription();
            }
        }
        return permission;
    }

    /**
     * 资源类型的中文描述，供审批表单只读展示（如 FILE → ResourceType#description）
     */
    private String resourceTypeDesc(String resourceType) {
        if (resourceType == null) {
            return "";
        }
        for (ResourceType item : ResourceType.values()) {
            if (item.name().equalsIgnoreCase(resourceType)) {
                return item.getDescription();
            }
        }
        return resourceType;
    }

    /**
     * 主体类型的中文描述，供审批表单只读展示（如 ORGANIZATION → PrincipalType#description）
     */
    private String principalTypeDesc(String principalType) {
        if (principalType == null) {
            return "";
        }
        for (PrincipalType item : PrincipalType.values()) {
            if (item.name().equals(principalType)) {
                return item.getDescription();
            }
        }
        return principalType;
    }

    /**
     * 按主体类型解析主体名称，与授权记录列表（BpmAclPermissionApprovalController）口径保持一致
     */
    private String resolvePrincipalName(String principalType, Long principalId) {
        if (principalType == null || principalId == null) {
            return String.valueOf(principalId);
        }
        return switch (principalType) {
            case "USER" -> principalNameResolver.resolveUserNames(List.of(principalId))
                    .getOrDefault(principalId, "未知用户");
            case "ROLE" -> principalNameResolver.resolveRoleNames(List.of(principalId))
                    .getOrDefault(principalId, "未知角色");
            case "POST" -> principalNameResolver.resolvePostNames(List.of(principalId))
                    .getOrDefault(principalId, "未知岗位");
            case "ORGANIZATION" -> {
                IPrincipalNameResolver.OrganizationInfo org =
                        principalNameResolver.resolveAllOrganizations().get(principalId);
                yield org != null ? org.name() : "未知组织";
            }
            case "TENANT" -> {
                TenantDO tenant = tenantService.getTenant(principalId);
                yield tenant != null ? tenant.getName() : "未知租户";
            }
            default -> String.valueOf(principalId);
        };
    }

    @Override
    public BpmAclPermissionApprovalDO getApproval(Long id) {
        return approvalMapper.selectById(id);
    }

    @Override
    public PageResult<BpmAclPermissionApprovalDO> getApprovalPage(Long userId,
                                                                   BpmAclPermissionApprovalPageReqVO pageReqVO) {
        return approvalMapper.selectPage(userId, pageReqVO);
    }

    @Override
    public void applyPermissions(BpmAclPermissionApprovalDO approval) {
        BpmAclPermissionApprovalOperationTypeEnum operationType =
                BpmAclPermissionApprovalOperationTypeEnum.valueOfType(approval.getOperationType());
        if (operationType == null) {
            throw exception(ACL_APPROVAL_OPERATION_TYPE_INVALID);
        }
        List<String> permissions = JsonUtils.parseArray(approval.getPermissions(), String.class);
        String resourceType = approval.getResourceType();
        String principalType = approval.getPrincipalType();
        Long resourceId = approval.getResourceId();
        Long principalId = approval.getPrincipalId();
        switch (operationType) {
            case GRANT -> aclPermissionService.grantPermission(AclPermissionGrantReqVO.builder()
                    .resourceType(resourceType).resourceId(resourceId)
                    .principalType(principalType).principalId(principalId)
                    .permissions(permissions)
                    .expireTime(approval.getExpireTime() != null
                            ? approval.getExpireTime().format(EXPIRE_TIME_FORMATTER) : null)
                    .remark(approval.getRemark())
                    .build());
            case SET -> aclPermissionService.setPermission(AclPermissionGrantReqVO.builder()
                    .resourceType(resourceType).resourceId(resourceId)
                    .principalType(principalType).principalId(principalId)
                    .permissions(permissions)
                    .expireTime(approval.getExpireTime() != null
                            ? approval.getExpireTime().format(EXPIRE_TIME_FORMATTER) : null)
                    .remark(approval.getRemark())
                    .build());
            case REVOKE -> aclPermissionService.revokePermission(AclPermissionRevokeReqVO.builder()
                    .resourceType(resourceType).resourceId(resourceId)
                    .principalType(principalType).principalId(principalId)
                    .permissions(permissions)
                    .build());
        }
        log.info("[applyPermissions][单据({}) 执行操作({})完成]", approval.getId(), operationType.getType());
    }

    @Override
    public void updateApprovalStatus(Long id, Integer status) {
        validateApprovalExists(id);
        approvalMapper.updateById(new BpmAclPermissionApprovalDO().setId(id).setStatus(status));
    }

    private void validateApprovalExists(Long id) {
        if (approvalMapper.selectById(id) == null) {
            throw exception(ACL_APPROVAL_NOT_EXISTS);
        }
    }

    private LocalDateTime parseExpireTime(String expireTime) {
        if (expireTime == null || expireTime.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(expireTime, EXPIRE_TIME_FORMATTER);
    }

}