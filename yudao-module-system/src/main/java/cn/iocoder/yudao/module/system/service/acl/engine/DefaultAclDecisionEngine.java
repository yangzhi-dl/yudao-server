package cn.iocoder.yudao.module.system.service.acl.engine;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.biz.system.permission.PermissionCommonApi;
import cn.iocoder.yudao.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;
import cn.iocoder.yudao.framework.datapermission.core.util.DataPermissionUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.system.dal.dataobject.acl.AclPermissionDO;
import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.system.enums.acl.PrincipalType;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import cn.iocoder.yudao.module.system.service.acl.AclService;
import cn.iocoder.yudao.module.system.service.acl.IPrincipalNameResolver;
import cn.iocoder.yudao.module.system.service.acl.matcher.PrincipalMatcherRegistry;
import cn.iocoder.yudao.module.system.service.acl.metadata.ResourceMetadataProvider;
import cn.iocoder.yudao.module.system.service.acl.metadata.ResourceMetadataRegistry;
import cn.iocoder.yudao.module.system.service.acl.model.AccessContext;
import cn.iocoder.yudao.module.system.service.acl.model.AccessDecision;
import cn.iocoder.yudao.module.system.service.acl.model.AclPrincipal;
import cn.iocoder.yudao.module.system.service.acl.policy.AccessPolicy;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ACL 决策引擎默认实现
 * <p>
 * 作为门面统一承载 canAccess / accessibleIds / restrictedIds / userPermissions 四类查询，
 * 内部通过责任链 {@link AccessPolicy} 完成判定，避免决策逻辑在多处重复。
 *
 * @author IIMS
 */
@Slf4j
@Component
public class DefaultAclDecisionEngine implements AclDecisionEngine {

    private final List<AccessPolicy> policies;
    private final IPrincipalNameResolver principalNameResolver;
    private final AclService aclService;
    private final ResourceMetadataRegistry metadataRegistry;
    private final PrincipalMatcherRegistry matcherRegistry;
    private final PermissionCommonApi permissionApi;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public DefaultAclDecisionEngine(List<AccessPolicy> policies,
                                    IPrincipalNameResolver principalNameResolver,
                                    AclService aclService,
                                    ResourceMetadataRegistry metadataRegistry,
                                    PrincipalMatcherRegistry matcherRegistry,
                                    PermissionCommonApi permissionApi) {
        this.policies = policies.stream()
                .sorted(Comparator.comparingInt(AccessPolicy::order))
                .collect(Collectors.toList());
        this.principalNameResolver = principalNameResolver;
        this.aclService = aclService;
        this.metadataRegistry = metadataRegistry;
        this.matcherRegistry = matcherRegistry;
        this.permissionApi = permissionApi;
    }

    @Override
    public boolean canAccess(ResourceType resourceType, Long resourceId, Permission permission) {
        AclPrincipal principal = resolvePrincipal();
        if (principal.superAdmin()) {
            return true;
        }
        AccessContext context = buildContext(principal, resourceType, resourceId, permission);
        return evaluate(context) == AccessDecision.GRANT;
    }

    @Override
    public Set<Long> accessibleIds(ResourceType resourceType, Permission permission, Collection<Long> candidateIds) {
        if (CollUtil.isEmpty(candidateIds)) {
            return Collections.emptySet();
        }
        AclPrincipal principal = resolvePrincipal();
        if (principal.superAdmin()) {
            return new HashSet<>(candidateIds);
        }
        ResourceMetadataProvider provider = metadataRegistry.get(resourceType);
        Map<Long, Long> tenantMap = getTenantIds(provider, candidateIds);
        Set<Long> creatorIds = selectCreatorIds(provider, principal.userId());
        Map<Long, List<AclPermissionDO>> aclMap = loadAclByResource(resourceType);

        Set<Long> result = new HashSet<>();
        for (Long resourceId : candidateIds) {
            AccessContext context = new AccessContext(
                    principal,
                    resourceType,
                    resourceId,
                    permission,
                    tenantMap.get(resourceId),
                    creatorIds.contains(resourceId),
                    aclMap.getOrDefault(resourceId, Collections.emptyList()));
            if (evaluate(context) == AccessDecision.GRANT) {
                result.add(resourceId);
            }
        }
        return result;
    }

    @Override
    public Set<Long> restrictedIds(ResourceType resourceType, Permission permission, Collection<Long> candidateIds) {
        if (CollUtil.isEmpty(candidateIds)) {
            return Collections.emptySet();
        }
        AclPrincipal principal = resolvePrincipal();
        if (principal.superAdmin()) {
            return Collections.emptySet();
        }
        ResourceMetadataProvider provider = metadataRegistry.get(resourceType);
        Map<Long, Long> tenantMap = getTenantIds(provider, candidateIds);
        Set<Long> creatorIds = selectCreatorIds(provider, principal.userId());
        Map<Long, List<AclPermissionDO>> aclMap = loadAclByResource(resourceType);

        Set<Long> result = new HashSet<>();
        for (Long resourceId : candidateIds) {
            AccessContext context = new AccessContext(
                    principal,
                    resourceType,
                    resourceId,
                    permission,
                    tenantMap.get(resourceId),
                    creatorIds.contains(resourceId),
                    aclMap.getOrDefault(resourceId, Collections.emptyList()));
            if (evaluate(context) != AccessDecision.GRANT) {
                result.add(resourceId);
            }
        }
        return result;
    }

    @Override
    public Set<Long> restrictedIds(ResourceType resourceType, Permission permission) {
        ResourceMetadataProvider provider = metadataRegistry.get(resourceType);
        return restrictedIds(resourceType, permission, selectAllIds(provider));
    }

    @Override
    public Set<String> userPermissions(ResourceType resourceType, Long resourceId) {
        AclPrincipal principal = resolvePrincipal();
        ResourceMetadataProvider provider = metadataRegistry.get(resourceType);
        Long ownerTenantId = getTenantId(provider, resourceId);
        boolean creator = isCreator(provider, resourceId, principal.userId());
        boolean sameTenant = ownerTenantId != null && Objects.equals(ownerTenantId, principal.tenantId());

        // 超管 / 本租户创建者 / 本租户租管 -> 全部权限
        if (principal.superAdmin() || (creator && sameTenant) || (principal.tenantAdmin() && sameTenant)) {
            return allPermissionNames();
        }

        List<AclPermissionDO> aclEntries = aclService.listResourcePermissions(resourceType, resourceId);
        int mask = 0;
        for (AclPermissionDO acl : aclEntries) {
            if (acl.getExpireTime() != null && acl.getExpireTime().isBefore(LocalDateTime.now())) {
                continue;
            }
            Integer aclMask = acl.getPermissionMask();
            if (aclMask == null) {
                continue;
            }
            if (principal.tenantAdmin()) {
                // 跨租户租管只统计 TENANT 授权
                if (acl.getPrincipalType() != PrincipalType.TENANT) {
                    continue;
                }
            } else if (acl.getPrincipalType() == PrincipalType.TENANT) {
                // 普通用户不受 TENANT 授权影响
                continue;
            }
            if (matcherRegistry.matches(acl.getPrincipalType(), principal, acl.getPrincipalId())) {
                mask |= aclMask;
            }
        }
        return permissionNames(mask);
    }

    @Override
    public boolean isCreator(ResourceType resourceType, Long resourceId) {
        if (resourceId == null) {
            return false;
        }
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return false;
        }
        ResourceMetadataProvider provider = metadataRegistry.get(resourceType);
        return isCreator(provider, resourceId, userId);
    }

    // ==================== 内部方法 ====================

    /**
     * 按责任链顺序执行访问决策
     */
    private AccessDecision evaluate(AccessContext context) {
        for (AccessPolicy policy : policies) {
            AccessDecision decision = policy.decide(context);
            if (decision != AccessDecision.ABSTAIN) {
                return decision;
            }
        }
        return AccessDecision.DENY;
    }

    /**
     * 解析当前登录主体上下文
     */
    private AclPrincipal resolvePrincipal() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        Long tenantId = TenantContextHolder.getRequiredTenantId();
        IPrincipalNameResolver.UserInfo user = principalNameResolver.getUserById(userId);
        List<Long> roleIds = parseRoleIds(user != null ? user.role() : null);
        Long orgId = user != null ? user.organization() : null;
        List<Long> postIds = principalNameResolver.getUserPostIds(userId);
        boolean superAdmin = principalNameResolver.isSuperAdmin(userId);
        boolean tenantAdmin = principalNameResolver.isTenantAdmin(userId);
        DeptDataPermissionRespDTO dataScope = permissionApi.getDeptDataPermission(userId);
        return new AclPrincipal(userId, tenantId, roleIds, orgId, postIds, superAdmin, tenantAdmin, dataScope);
    }

    private AccessContext buildContext(AclPrincipal principal, ResourceType resourceType,
                                       Long resourceId, Permission permission) {
        ResourceMetadataProvider provider = metadataRegistry.get(resourceType);
        Long ownerTenantId = getTenantId(provider, resourceId);
        boolean creator = isCreator(provider, resourceId, principal.userId());
        List<AclPermissionDO> aclEntries = aclService.listResourcePermissions(resourceType, resourceId);
        return new AccessContext(principal, resourceType, resourceId, permission, ownerTenantId, creator, aclEntries);
    }

    private Map<Long, List<AclPermissionDO>> loadAclByResource(ResourceType resourceType) {
        List<AclPermissionDO> all = aclService.listAllByResourceType(resourceType);
        if (CollUtil.isEmpty(all)) {
            return Collections.emptyMap();
        }
        return all.stream().collect(Collectors.groupingBy(AclPermissionDO::getResourceId));
    }

    /**
     * 元数据查询统一绕过数据权限：这些查询用于给引擎做归属/创建者判定，
     * 若再套用 ACL 数据权限规则，会在 {@code AclDataPermissionRule} 中形成无限递归。
     */
    private Set<Long> selectAllIds(ResourceMetadataProvider provider) {
        return DataPermissionUtils.executeIgnore(provider::selectAllIds);
    }

    private Map<Long, Long> getTenantIds(ResourceMetadataProvider provider, Collection<Long> resourceIds) {
        return DataPermissionUtils.executeIgnore(() -> provider.getTenantIds(resourceIds));
    }

    private Long getTenantId(ResourceMetadataProvider provider, Long resourceId) {
        return DataPermissionUtils.executeIgnore(() -> provider.getTenantId(resourceId));
    }

    private Set<Long> selectCreatorIds(ResourceMetadataProvider provider, Long userId) {
        Set<Long> ids = DataPermissionUtils.executeIgnore(() -> provider.selectCreatorIds(userId));
        return ids != null ? ids : Collections.emptySet();
    }

    private boolean isCreator(ResourceMetadataProvider provider, Long resourceId, Long userId) {
        return DataPermissionUtils.executeIgnore(() -> provider.isCreator(resourceId, userId));
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

    private Set<String> allPermissionNames() {
        return Arrays.stream(Permission.values())
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    private Set<String> permissionNames(int mask) {
        Set<String> names = new HashSet<>();
        for (Permission permission : Permission.values()) {
            if (Permission.hasPermission(mask, permission)) {
                names.add(permission.name());
            }
        }
        return names;
    }
}
