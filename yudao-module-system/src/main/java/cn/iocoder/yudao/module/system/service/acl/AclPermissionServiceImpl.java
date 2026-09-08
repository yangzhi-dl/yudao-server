package cn.iocoder.yudao.module.system.service.acl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.system.controller.admin.acl.vo.*;
import cn.iocoder.yudao.module.system.dal.dataobject.acl.AclPermissionDO;
import cn.iocoder.yudao.module.system.dal.dataobject.acl.AclPermissionLineageDO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.module.system.dal.mysql.acl.AclPermissionLineageMapper;
import cn.iocoder.yudao.module.system.dal.mysql.acl.AclPermissionMapper;
import cn.iocoder.yudao.module.system.enums.acl.AclPrincipalTypeOwnerType;
import cn.iocoder.yudao.module.system.service.acl.engine.AclDecisionEngine;
import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.system.enums.acl.PrincipalType;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import cn.iocoder.yudao.module.system.service.tenant.TenantService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.ACL_AUTH_PERMISSION_NOT_ALLOWED;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.ACL_EXPIRE_TIME_BEFORE_NOW;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.ACL_EXPIRE_TIME_EXCEED_PARENT;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.ACL_PERMISSION_NOT_OWNED;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.ACL_PRINCIPAL_TYPE_NOT_ALLOWED;

/**
 * ACL 权限管理服务实现
 *
 * @author IIMS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AclPermissionServiceImpl implements AclPermissionService {

    private final AclService aclService;
    private final AclDecisionEngine aclDecisionEngine;
    private final IPrincipalNameResolver principalNameResolver;
    private final TenantService tenantService;
    private final AclPrincipalTypeConfigService aclPrincipalTypeConfigService;
    private final AclPermissionLineageMapper aclPermissionLineageMapper;
    private final AclPermissionMapper aclPermissionMapper;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public Set<PrincipalType> getAvailablePrincipalTypes() {
        UserContext ctx = getUserContext();
        // 角色可用主体取并集；租户管理员在角色维度默认拥有全部主体，仅受套餐维度限制
        Set<PrincipalType> roleTypes = ctx.isTenantAdmin || ctx.isSuperAdmin ? allPrincipalTypes()
                : aclPrincipalTypeConfigService.getPrincipalTypes(AclPrincipalTypeOwnerType.ROLE, ctx.roleIds);
        // 套餐可用主体（系统租户无套餐，直接放行，由角色维度兜底）
        Set<PrincipalType> packageTypes = getPackagePrincipalTypes();
        // 角色与套餐取交集
        roleTypes.retainAll(packageTypes);
        return roleTypes;
    }

    /**
     * 获取当前用户所属租户套餐允许使用的授权主体集合
     */
    private Set<PrincipalType> getPackagePrincipalTypes() {
        Long tenantId = TenantContextHolder.getRequiredTenantId();
        TenantDO tenant = tenantService.getTenant(tenantId);
        if (tenant == null || Objects.equals(tenant.getPackageId(), TenantDO.PACKAGE_ID_SYSTEM)) {
            return allPrincipalTypes();
        }
        return aclPrincipalTypeConfigService.getPrincipalTypes(
                AclPrincipalTypeOwnerType.PACKAGE, tenant.getPackageId());
    }

    private Set<PrincipalType> allPrincipalTypes() {
        return Arrays.stream(PrincipalType.values()).collect(Collectors.toSet());
    }

    /**
     * 校验当前用户是否允许使用指定授权主体
     */
    private void validatePrincipalTypeAllowed(PrincipalType principalType) {
        if (!getAvailablePrincipalTypes().contains(principalType)) {
            throw exception(ACL_PRINCIPAL_TYPE_NOT_ALLOWED, principalType.getDescription());
        }
    }

    /**
     * 校验当前用户是否拥有指定资源的 ACL 授权权限（AUTH）
     * <p>通用 ACL 管理接口统一下发此校验，防止任意用户对任意资源进行授权/撤销。
     * 校验逻辑复用 {@link #aclEntryGrantsAccess} 的主体匹配能力，但**对租户管理员不豁免**：
     * 租管也必须通过 ACL 主体匹配真正持有该资源的 AUTH 权限，从而保证授权列表在租户间严格隔离。
     * 可放行场景：超管、资源创建者，以及被显式授予 AUTH 权限的 USER / ROLE / ORGANIZATION / POST / TENANT 主体。
     * 创建者通过 {@link AclDecisionEngine#isCreator} 判定（内部按资源元数据 Provider 解析，对未知资源类型返回 false）。
     *
     * @param resourceType 资源类型
     * @param resourceId   资源ID
     */
    private void validateAuthPermission(ResourceType resourceType, Long resourceId) {
        boolean granted = getAuthPermissionGrant(resourceType, resourceId);
        if (!granted) {
            throw exception(ACL_AUTH_PERMISSION_NOT_ALLOWED);
        }
    }

    /**
     * 判断当前用户是否拥有指定资源的 AUTH 权限（不豁免租户管理员）
     * <p>放行场景：超管、资源创建者，以及被显式授予 AUTH 权限的 USER / ROLE / ORGANIZATION / POST / TENANT 主体。
     * 创建者豁免与决策引擎 {@code CreatorAccessPolicy} 口径一致（创建者拥有资源的全部权限，含 AUTH），
     * 否则创建者将无法管理自己创建资源的 ACL；租户管理员不豁免，需通过 ACL 主体匹配真正持有 AUTH，
     * 保证授权列表在租户间严格隔离。
     *
     * @return true=拥有 AUTH 权限，可进行 ACL 管理
     */
    private boolean getAuthPermissionGrant(ResourceType resourceType, Long resourceId) {
        UserContext ctx = getUserContext();
        // 超管直接放行（全局唯一例外）
        if (ctx.isSuperAdmin) {
            return true;
        }
        // 资源创建者豁免：决策引擎视创建者拥有资源的全部权限（含 AUTH），
        // 故 ACL 管理对创建者同样放行，避免创建者无法管理自己创建的资源。
        if (aclDecisionEngine.isCreator(resourceType, resourceId)) {
            return true;
        }
        // 复用主体匹配：租户管理员不豁免，
        // 普通用户 / 租管都需通过 ACL 主体（USER/ROLE/ORGANIZATION/POST/TENANT）真正持有 AUTH 权限。
        List<AclPermissionDO> aclList = aclService.listResourcePermissions(resourceType, resourceId);
        if (CollectionUtils.isEmpty(aclList)) {
            return false;
        }
        return aclList.stream().anyMatch(acl -> aclEntryGrantsAccess(acl, ctx, Permission.AUTH));
    }

    // ==================== 通用增删查 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantPermission(AclPermissionGrantReqVO dto) {
        Long grantBy = SecurityFrameworkUtils.getLoginUserId();
        PrincipalType principalType = PrincipalType.valueOf(dto.getPrincipalType().toUpperCase());
        ResourceType resourceType = ResourceType.valueOf(dto.getResourceType().toUpperCase());
        validateAuthPermission(resourceType, dto.getResourceId());
        validatePrincipalTypeAllowed(principalType);

        // 解析授权来源（父链路节点）：用于级联撤销与过期时间继承
        UserContext ctx = getUserContext();
        AclPermissionLineageDO parentNode = resolveParentNode(resourceType, dto.getResourceId(), ctx);
        Long parentId = parentNode != null ? parentNode.getId() : null;
        // 过期时间继承父节点：未指定时继承父级，指定时不得超过父级
        LocalDateTime expireTime = resolveExpireTime(parseExpireTime(dto.getExpireTime()),
                parentNode != null ? parentNode.getExpireTime() : null);

        // 一次加载该资源 ACL，供「授权人必须持有被授予权限」校验复用
        List<AclPermissionDO> aclList = ctx.isSuperAdmin ? List.of()
                : aclService.listResourcePermissions(resourceType, dto.getResourceId());
        // 授权前物化表旧记录（叠加授权可能覆盖过期时间/掩码，用于判断是否减少需级联收回下游）
        AclPermissionDO oldRecord = findMaterializedPermission(resourceType, dto.getResourceId(),
                principalType, dto.getPrincipalId());
        LocalDateTime oldExpire = oldRecord != null ? oldRecord.getExpireTime() : null;

        for (String perm : dto.getPermissions()) {
            Permission permission = Permission.valueOf(perm.toUpperCase());
            // 创建时校验：授权人必须真正持有被授予的权限
            validateGrantPermission(resourceType, dto.getResourceId(), aclList, ctx, permission);

            AclPermissionGrantCreateReqVO request = AclPermissionGrantCreateReqVO.builder()
                    .resourceType(resourceType)
                    .resourceId(dto.getResourceId())
                    .principalType(principalType)
                    .principalId(dto.getPrincipalId())
                    .permission(permission)
                    .grantBy(grantBy)
                    .expireTime(expireTime)
                    .remark(dto.getRemark())
                    .build();
            aclService.grantPermission(request);

            // 记录授权链路，供下游级联撤销
            recordGrantLineage(resourceType, dto.getResourceId(), principalType, dto.getPrincipalId(),
                    permission, grantBy, expireTime, parentId);
        }

        // 过期时间变化级联：同步该授权自身链路节点；仅当减少时下钻缩短下游子授权（授权表与授权码/链路一并处理）
        cascadeExpireTimeChange(resourceType, dto.getResourceId(), principalType, dto.getPrincipalId(),
                oldExpire, expireTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPermission(AclPermissionGrantReqVO dto) {
        Long grantBy = SecurityFrameworkUtils.getLoginUserId();
        ResourceType resourceType = ResourceType.valueOf(dto.getResourceType().toUpperCase());
        PrincipalType principalType = PrincipalType.valueOf(dto.getPrincipalType().toUpperCase());
        validateAuthPermission(resourceType, dto.getResourceId());
        validatePrincipalTypeAllowed(principalType);
        // 编辑前物化表旧记录：用于判断权限掩码/过期时间是否减少（减少需级联收回/缩短下游子授权）
        AclPermissionDO oldRecord = findMaterializedPermission(resourceType, dto.getResourceId(),
                principalType, dto.getPrincipalId());
        LocalDateTime oldExpire = oldRecord != null ? oldRecord.getExpireTime() : null;
        int oldMask = oldRecord != null && oldRecord.getPermissionMask() != null ? oldRecord.getPermissionMask() : 0;
        // 过期时间：不得早于当前时间，且不得超过父级授权（与 grantPermission 口径一致）
        UserContext ctx = getUserContext();
        AclPermissionLineageDO parentNode = resolveParentNode(resourceType, dto.getResourceId(), ctx);
        LocalDateTime expireTime = resolveExpireTime(parseExpireTime(dto.getExpireTime()),
                parentNode != null ? parentNode.getExpireTime() : null);
        // 计算组合权限掩码
        int combinedMask = 0;
        for (String perm : dto.getPermissions()) {
            combinedMask |= Permission.valueOf(perm.toUpperCase()).getMask();
        }
        aclService.setPermission(resourceType, dto.getResourceId(),
                principalType, dto.getPrincipalId(), combinedMask, grantBy, expireTime, dto.getRemark());
        // 权限掩码（授权码）收缩级联：仅当减少时收回下游子授权中超过新掩码的权限位
        cascadePermissionShrink(resourceType, dto.getResourceId(), principalType, dto.getPrincipalId(),
                oldMask, combinedMask);
        // 过期时间变化级联：同步该授权自身链路节点；仅当减少时下钻缩短下游子授权（授权表与链路一并处理）
        cascadeExpireTimeChange(resourceType, dto.getResourceId(), principalType, dto.getPrincipalId(),
                oldExpire, expireTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokePermission(AclPermissionRevokeReqVO dto) {
        PrincipalType principalType = PrincipalType.valueOf(dto.getPrincipalType().toUpperCase());
        ResourceType resourceType = ResourceType.valueOf(dto.getResourceType().toUpperCase());
        validateAuthPermission(resourceType, dto.getResourceId());

        for (String perm : dto.getPermissions()) {
            Permission permission = Permission.valueOf(perm.toUpperCase());
            revokeWithCascade(resourceType, dto.getResourceId(), principalType, dto.getPrincipalId(), permission);
        }
    }

    /**
     * 撤销主体自身权限，并级联撤销其下游授权链
     * <p>注意：下游链路节点的 parentId 指向父的 AUTH 来源节点（{@link #resolveParentNode} 只查 AUTH 位），
     * 因此级联回收必须从该主体【全部】链路节点出发下钻（而非仅被撤销权限位的节点），
     * 否则覆盖不到任何下游。详见 {@link #cascadeRevokePermissionBit}。
     */
    private void revokeWithCascade(ResourceType resourceType, Long resourceId, PrincipalType principalType,
                                   Long principalId, Permission permission) {
        // 1. 撤销被撤销主体自身的权限
        AclPermissionRevokeCreateReqVO request = AclPermissionRevokeCreateReqVO.builder()
                .resourceType(resourceType)
                .resourceId(resourceId)
                .principalType(principalType)
                .principalId(principalId)
                .permission(permission)
                .build();
        aclService.revokePermission(request);
        // 2. 级联收回下游子授权中同一权限位的链路节点与物化授权记录
        cascadeRevokePermissionBit(resourceType, resourceId, principalType, principalId, permission.getMask());
    }

    @Override
    public PageResult<AclPermissionRespVO> pagePermissions(AclPermissionPageReqVO dto) {
        ResourceType resourceType = ResourceType.valueOf(dto.getResourceType());
        validateAuthPermission(resourceType, dto.getResourceId());
        PageResult<AclPermissionDO> pageResult = aclService.pageResourcePermissions(dto);
        if (pageResult.getTotal() == 0 || CollectionUtils.isEmpty(pageResult.getList())) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }
        List<AclPermissionDO> aclList = pageResult.getList();
        // 主体（用户/角色/组织/岗位/租户）可能跨租户，解析名称时需绕过租户插件，避免返回“未知”
        List<AclPermissionRespVO> vos = buildPermissionVOs(aclList, resourceType, dto.getResourceId());
        return new PageResult<>(vos, pageResult.getTotal());
    }

    private List<AclPermissionRespVO> buildPermissionVOs(List<AclPermissionDO> aclList,
                                                         ResourceType resourceType, Long resourceId) {
        // 批量解析主体名称
        List<Long> userIds = new ArrayList<>();
        List<Long> roleIds = new ArrayList<>();
        List<Long> postIds = new ArrayList<>();
        List<Long> tenantIds = new ArrayList<>();

        for (AclPermissionDO acl : aclList) {
            switch (acl.getPrincipalType()) {
                case USER -> userIds.add(acl.getPrincipalId());
                case ROLE -> roleIds.add(acl.getPrincipalId());
                case POST -> postIds.add(acl.getPrincipalId());
                case TENANT -> tenantIds.add(acl.getPrincipalId());
            }
        }

        Map<Long, String> userNames = principalNameResolver.resolveUserNames(userIds);
        Map<Long, String> roleNames = principalNameResolver.resolveRoleNames(roleIds);
        Map<Long, String> postNames = principalNameResolver.resolvePostNames(postIds);
        Map<Long, String> tenantNames = resolveTenantNames(tenantIds);
        Map<Long, IPrincipalNameResolver.OrganizationInfo> orgMap = principalNameResolver.resolveAllOrganizations();

        // 授权者名称
        Map<Long, String> grantByNames = principalNameResolver.resolveUserNames(
                aclList.stream().map(AclPermissionDO::getGrantBy)
                        .filter(Objects::nonNull).distinct().collect(Collectors.toList()));

        // 组装
        return aclList.stream().map(acl -> {
            String principalName = switch (acl.getPrincipalType()) {
                case USER -> userNames.getOrDefault(acl.getPrincipalId(), "未知用户");
                case ROLE -> roleNames.getOrDefault(acl.getPrincipalId(), "未知角色");
                case ORGANIZATION -> {
                    IPrincipalNameResolver.OrganizationInfo org = orgMap.get(acl.getPrincipalId());
                    yield org != null ? org.name() : "未知组织";
                }
                case POST -> postNames.getOrDefault(acl.getPrincipalId(), "未知岗位");
                case TENANT -> tenantNames.getOrDefault(acl.getPrincipalId(), "未知租户");
            };

            List<String> permNames = new ArrayList<>();
            for (Permission perm : Permission.values()) {
                if (Permission.hasPermission(acl.getPermissionMask(), perm)) {
                    permNames.add(perm.name());
                }
            }

            return AclPermissionRespVO.builder()
                    .id(acl.getId())
                    .resourceType(resourceType.name())
                    .resourceId(resourceId)
                    .principalType(acl.getPrincipalType().name())
                    .principalTypeDesc(acl.getPrincipalType().getDescription())
                    .principalId(acl.getPrincipalId())
                    .principalName(principalName)
                    .permissionMask(acl.getPermissionMask())
                    .permissions(permNames)
                    .grantTime(acl.getGrantTime())
                    .expireTime(acl.getExpireTime())
                    .grantByName(grantByNames.getOrDefault(acl.getGrantBy(), ""))
                    .remark(acl.getRemark())
                    .build();
        }).collect(Collectors.toList());
    }

    // ==================== 内部方法 ====================

    /**
     * 获取当前用户对指定资源的「父级 ACL 授权」过期时间
     * <p>父级即授权人赖以授权的 AUTH 来源链路节点（与 {@link #grantPermission} 口径一致）；
     * 超管、无父级来源（根授权）或父级已过期时返回 null，表示无父级限制。
     */
    @Override
    public LocalDateTime getParentExpireTime(String resourceType, Long resourceId) {
        ResourceType type = ResourceType.valueOf(resourceType.toUpperCase());
        validateAuthPermission(type, resourceId);
        UserContext ctx = getUserContext();
        AclPermissionLineageDO parentNode = resolveParentNode(type, resourceId, ctx);
        return parentNode != null ? parentNode.getExpireTime() : null;
    }

    /**
     * 获取当前用户的 ACL 上下文（角色、组织、岗位）
     */
    private UserContext getUserContext() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        Long tenantId = TenantContextHolder.getRequiredTenantId();
        IPrincipalNameResolver.UserInfo user = principalNameResolver.getUserById(userId);
        List<Long> roleIds = parseRoleIds(user.role());
        Long orgId = user.organization();
        List<Long> postIds = principalNameResolver.getUserPostIds(userId);
        boolean isSuperAdmin = principalNameResolver.isSuperAdmin(userId);
        boolean isTenantAdmin = principalNameResolver.isTenantAdmin(userId);
        return new UserContext(userId, tenantId, roleIds, orgId, postIds, isSuperAdmin, isTenantAdmin);
    }

    /**
     * 判断单条 ACL 记录是否对用户授予了指定权限
     */
    private boolean aclEntryGrantsAccess(AclPermissionDO acl, UserContext ctx, Permission required) {
        if (!Permission.hasPermission(acl.getPermissionMask(), required)) {
            return false;
        }
        if (acl.getExpireTime() != null && acl.getExpireTime().isBefore(LocalDateTime.now())) {
            return false;
        }
        return aclEntryMatchesPrincipal(acl, ctx);
    }

    /**
     * 判断单条 ACL 记录的主体是否匹配当前用户
     */
    private boolean aclEntryMatchesPrincipal(AclPermissionDO acl, UserContext ctx) {
        return principalMatches(acl.getPrincipalType(), acl.getPrincipalId(), ctx);
    }

    /**
     * 判断指定授权主体是否匹配当前用户
     */
    private boolean principalMatches(PrincipalType principalType, Long principalId, UserContext ctx) {
        return switch (principalType) {
            case USER -> Objects.equals(principalId, ctx.userId);
            case ROLE -> ctx.roleIds.contains(principalId);
            case ORGANIZATION -> Objects.equals(principalId, ctx.orgId);
            case POST -> ctx.postIds.contains(principalId);
            case TENANT -> Objects.equals(principalId, ctx.tenantId);
        };
    }

    /**
     * 解析授权来源（父链路节点）
     * <p>授权人的授权能力来自其持有的 AUTH 权限，故父节点取授权人 AUTH 来源链路节点。
     * 超管或找不到来源（兼容旧数据）时返回 null，按根授权处理。
     */
    private AclPermissionLineageDO resolveParentNode(ResourceType resourceType, Long resourceId, UserContext ctx) {
        if (ctx.isSuperAdmin) {
            return null;
        }
        List<AclPermissionLineageDO> authNodes = aclPermissionLineageMapper.selectActiveByPermission(
                resourceType, resourceId, Permission.AUTH.getMask());
        LocalDateTime now = LocalDateTime.now();
        return authNodes.stream()
                .filter(node -> node.getExpireTime() == null || node.getExpireTime().isAfter(now))
                .filter(node -> principalMatches(node.getPrincipalType(), node.getPrincipalId(), ctx)).min(Comparator
                        .comparingInt((AclPermissionLineageDO node) -> principalPriority(node.getPrincipalType()))
                        .thenComparing(AclPermissionLineageDO::getCreateTime, Comparator.reverseOrder()))
                .orElse(null);
    }

    /**
     * 授权主体匹配优先级：越具体优先级越高
     */
    private int principalPriority(PrincipalType type) {
        return switch (type) {
            case USER -> 0;
            case ROLE -> 1;
            case ORGANIZATION -> 2;
            case POST -> 3;
            case TENANT -> 4;
        };
    }

    /**
     * 创建时校验：授权人必须真正持有被授予的权限
     * <p>超管与资源创建者可直接放行：与决策引擎 {@code CreatorAccessPolicy} 口径一致（创建者拥有资源的全部权限），
     * 否则创建者无法向他人授予自己资源上的权限（含 AUTH）。其余主体必须通过 ACL 主体匹配真正持有所授予的权限。
     */
    private void validateGrantPermission(ResourceType resourceType, Long resourceId,
                                         List<AclPermissionDO> aclList, UserContext ctx, Permission permission) {
        if (ctx.isSuperAdmin) {
            return;
        }
        if (aclDecisionEngine.isCreator(resourceType, resourceId)) {
            return;
        }
        boolean granted = aclList.stream().anyMatch(acl -> aclEntryGrantsAccess(acl, ctx, permission));
        if (!granted) {
            throw exception(ACL_PERMISSION_NOT_OWNED, permission.getDescription());
        }
    }

    /**
     * 计算过期时间：不得早于当前时间；未指定时继承父级，指定时校验不得超过父级
     */
    private LocalDateTime resolveExpireTime(LocalDateTime requestExpire, LocalDateTime parentExpire) {
        LocalDateTime now = LocalDateTime.now();
        if (requestExpire != null && requestExpire.isBefore(now)) {
            throw exception(ACL_EXPIRE_TIME_BEFORE_NOW);
        }
        if (requestExpire == null) {
            return parentExpire;
        }
        if (parentExpire != null && requestExpire.isAfter(parentExpire)) {
            throw exception(ACL_EXPIRE_TIME_EXCEED_PARENT);
        }
        return requestExpire;
    }

    /**
     * 授权过期时间变化后的级联处理：
     * <p>1. 该授权自身的链路（lineage）节点过期时间始终同步为新值，保证血缘与物化授权表一致
     * （后续下游授权继承、{@link #getParentExpireTime} 均以链路节点为准）；
     * <p>2. 仅当过期时间【减少】时，递归下钻所有下游子授权链：凡超过新父级过期时间的
     * 链路节点（授权码）与物化授权记录（授权表）一并缩短为新父级过期时间；
     * 过期时间增加或不变时不处理下游（子授权有效期不受影响）。
     *
     * @param oldExpire 变更前物化表过期时间（null 表示新增授权）
     * @param newExpire 变更后过期时间（null 表示永久/无限制）
     */
    private void cascadeExpireTimeChange(ResourceType resourceType, Long resourceId,
                                         PrincipalType principalType, Long principalId,
                                         LocalDateTime oldExpire, LocalDateTime newExpire) {
        // 1. 同步该授权自身的链路节点过期时间（无论增减，保持血缘与物化一致）
        List<AclPermissionLineageDO> selfNodes = aclPermissionLineageMapper.selectActiveByPrincipal(
                resourceType, resourceId, principalType, principalId);
        for (AclPermissionLineageDO node : selfNodes) {
            if (!Objects.equals(node.getExpireTime(), newExpire)) {
                node.setExpireTime(newExpire);
                aclPermissionLineageMapper.updateById(node);
            }
        }
        // 2. 仅过期时间减少才级联下游（增加/不变无需处理）
        if (newExpire == null || oldExpire == null || !newExpire.isBefore(oldExpire)) {
            return;
        }
        // 3. BFS 下钻：缩短所有超过新父级时间的下游链路节点与物化授权记录
        Set<Long> parentIds = selfNodes.stream().map(AclPermissionLineageDO::getId).collect(Collectors.toSet());
        while (!parentIds.isEmpty()) {
            List<AclPermissionLineageDO> children = aclPermissionLineageMapper.selectActiveByParentIds(parentIds);
            if (CollectionUtils.isEmpty(children)) {
                break;
            }
            for (AclPermissionLineageDO child : children) {
                if (child.getExpireTime() == null || child.getExpireTime().isAfter(newExpire)) {
                    child.setExpireTime(newExpire);
                    aclPermissionLineageMapper.updateById(child);
                    shrinkMaterializedExpireTime(child, newExpire);
                }
            }
            parentIds = children.stream().map(AclPermissionLineageDO::getId).collect(Collectors.toSet());
        }
    }

    /**
     * 将物化授权记录（AclPermissionDO）的过期时间缩短为新父级时间（只缩短不延长）
     */
    private void shrinkMaterializedExpireTime(AclPermissionLineageDO node, LocalDateTime newExpire) {
        Long tenantId = node.getTenantId() != null ? node.getTenantId() : TenantContextHolder.getTenantId();
        AclPermissionDO record = aclPermissionMapper.selectByTenantResourcePrincipal(tenantId,
                node.getResourceType(), node.getResourceId(), node.getPrincipalType(), node.getPrincipalId());
        if (record == null || (record.getExpireTime() != null && !record.getExpireTime().isAfter(newExpire))) {
            return;
        }
        record.setExpireTime(newExpire);
        aclPermissionMapper.updateById(record);
    }

    /**
     * 权限掩码（授权码）收缩级联：父授权权限【减少】时，下游子授权中超过新掩码的权限位一并收回。
     * <p>仅处理减少（oldMask 中不在 newMask 的位）；增加或不变时不处理下游（子授权不自动获得父级新增权限）。
     */
    private void cascadePermissionShrink(ResourceType resourceType, Long resourceId,
                                         PrincipalType principalType, Long principalId,
                                         int oldMask, int newMask) {
        int removedMask = oldMask & ~newMask;
        if (removedMask == 0) {
            return;
        }
        for (Permission permission : Permission.values()) {
            if ((removedMask & permission.getMask()) != 0) {
                cascadeRevokePermissionBit(resourceType, resourceId, principalType, principalId, permission.getMask());
            }
        }
    }

    /**
     * 级联收回指定权限位：从该主体的【全部】链路节点出发下钻其下游子树（visited 防重），
     * 收回子树中持有指定权限位的节点（授权码）；并从对应物化授权记录（授权表 AclPermissionDO）
     * 中移除该权限位——若该主体仍有其它有效来源（非本次失效的同权限位节点）则保留该位，掩码归零则删除记录。
     * <p>注意：下游节点的 parentId 指向父的 AUTH 来源节点，而非父的对应权限位节点，
     * 因此必须从该主体全部节点（含 AUTH 节点）出发才能覆盖到下游子授权链。
     */
    private void cascadeRevokePermissionBit(ResourceType resourceType, Long resourceId,
                                            PrincipalType principalType, Long principalId,
                                            int permissionBit) {
        // 1. 收集被收回的权限位节点：自身 + 全部下游子树中 permissionMask = permissionBit 的节点
        List<AclPermissionLineageDO> selfNodes = aclPermissionLineageMapper.selectActiveByPrincipal(
                resourceType, resourceId, principalType, principalId);
        Set<Long> parentIds = selfNodes.stream().map(AclPermissionLineageDO::getId).collect(Collectors.toSet());
        Set<Long> visited = new HashSet<>(parentIds);
        List<AclPermissionLineageDO> toInvalidate = new ArrayList<>();
        for (AclPermissionLineageDO node : selfNodes) {
            if (Objects.equals(node.getPermissionMask(), permissionBit)) {
                toInvalidate.add(node);
            }
        }
        while (!parentIds.isEmpty()) {
            List<AclPermissionLineageDO> children = aclPermissionLineageMapper.selectActiveByParentIds(parentIds);
            if (CollectionUtils.isEmpty(children)) {
                break;
            }
            Set<Long> nextParentIds = new HashSet<>();
            for (AclPermissionLineageDO child : children) {
                if (!visited.add(child.getId())) {
                    continue;
                }
                if (Objects.equals(child.getPermissionMask(), permissionBit)) {
                    toInvalidate.add(child);
                }
                nextParentIds.add(child.getId());
            }
            parentIds = nextParentIds;
        }
        if (toInvalidate.isEmpty()) {
            return;
        }
        Set<Long> invalidateIds = toInvalidate.stream().map(AclPermissionLineageDO::getId).collect(Collectors.toSet());
        // 2. 物化授权表：对每个受影响主体，移除其已无有效来源的该权限位
        Map<String, List<AclPermissionLineageDO>> byPrincipal = toInvalidate.stream().collect(Collectors.groupingBy(node ->
                node.getTenantId() + "|" + node.getResourceType() + "|" + node.getResourceId()
                        + "|" + node.getPrincipalType() + "|" + node.getPrincipalId()));
        for (List<AclPermissionLineageDO> nodes : byPrincipal.values()) {
            AclPermissionLineageDO sample = nodes.getFirst();
            AclPermissionDO record = aclPermissionMapper.selectByTenantResourcePrincipal(
                    sample.getTenantId() != null ? sample.getTenantId() : TenantContextHolder.getTenantId(),
                    sample.getResourceType(), sample.getResourceId(), sample.getPrincipalType(), sample.getPrincipalId());
            if (record == null || record.getPermissionMask() == null || record.getPermissionMask() == 0
                    || (record.getPermissionMask() & permissionBit) == 0) {
                continue; // 无记录、掩码为空或已无该位（如该主体自身掩码已被覆盖/撤销）
            }
            // 该主体是否还有其它有效来源（不在本次失效集合中的同权限位节点）
            List<AclPermissionLineageDO> others = aclPermissionLineageMapper.selectActive(
                    sample.getResourceType(), sample.getResourceId(),
                    sample.getPrincipalType(), sample.getPrincipalId(), permissionBit);
            boolean hasOtherSource = others.stream().anyMatch(o -> !invalidateIds.contains(o.getId()));
            if (!hasOtherSource) {
                record.setPermissionMask(Permission.removePermission(record.getPermissionMask(), permissionFromMask(permissionBit)));
            }
            if (record.getPermissionMask() == 0) {
                aclPermissionMapper.deleteById(record.getId());
            } else {
                aclPermissionMapper.updateById(record);
            }
        }
        // 3. 逻辑删除被收回的链路节点（授权码）
        aclPermissionLineageMapper.deleteByIds(invalidateIds);
    }

    /**
     * 查询物化授权表（AclPermissionDO）中该主体当前的授权记录（含掩码与过期时间）
     */
    private AclPermissionDO findMaterializedPermission(ResourceType resourceType, Long resourceId,
                                                       PrincipalType principalType, Long principalId) {
        return aclPermissionMapper.selectByTenantResourcePrincipal(
                TenantContextHolder.getTenantId(), resourceType, resourceId, principalType, principalId);
    }

    /**
     * 解析过期时间字符串
     */
    private LocalDateTime parseExpireTime(String expireTime) {
        if (expireTime == null || expireTime.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(expireTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 记录一条授权链路
     */
    private void recordGrantLineage(ResourceType resourceType, Long resourceId, PrincipalType principalType,
                                    Long principalId, Permission permission, Long grantBy,
                                    LocalDateTime expireTime, Long parentId) {
        AclPermissionLineageDO node = AclPermissionLineageDO.builder()
                .tenantId(TenantContextHolder.getTenantId())
                .resourceType(resourceType)
                .resourceId(resourceId)
                .principalType(principalType)
                .principalId(principalId)
                .permissionMask(permission.getMask())
                .grantBy(grantBy)
                .parentId(parentId)
                .expireTime(expireTime)
                .build();
        aclPermissionLineageMapper.insert(node);
    }

    /**
     * 根据掩码位反查权限枚举
     */
    private Permission permissionFromMask(int mask) {
        for (Permission permission : Permission.values()) {
            if (permission.getMask() == mask) {
                return permission;
            }
        }
        throw new IllegalArgumentException("未知权限掩码: " + mask);
    }

    private Map<Long, String> resolveTenantNames(List<Long> tenantIds) {
        if (CollectionUtils.isEmpty(tenantIds)) {
            return Collections.emptyMap();
        }
        Map<Long, String> names = new HashMap<>();
        for (Long tenantId : tenantIds.stream().distinct().toList()) {
            TenantDO tenant = tenantService.getTenant(tenantId);
            names.put(tenantId, tenant != null ? tenant.getName() : null);
        }
        return names;
    }

    private List<Long> parseRoleIds(String roleJson) {
        if (roleJson == null || roleJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(roleJson, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            log.warn("解析用户角色 JSON 失败: {}", roleJson, e);
            return Collections.emptyList();
        }
    }

    /**
     * 用户 ACL 上下文
     */
    private record UserContext(Long userId, Long tenantId, List<Long> roleIds, Long orgId, List<Long> postIds,
                               boolean isSuperAdmin, boolean isTenantAdmin) {}
}
