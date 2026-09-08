package cn.iocoder.yudao.module.system.service.acl.policy;

import cn.iocoder.yudao.module.system.dal.dataobject.acl.AclPermissionDO;
import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.system.enums.acl.PrincipalType;
import cn.iocoder.yudao.module.system.service.acl.matcher.PrincipalMatcherRegistry;
import cn.iocoder.yudao.module.system.service.acl.model.AccessContext;
import cn.iocoder.yudao.module.system.service.acl.model.AccessDecision;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 租户管理员策略
 * <p>
 * 本租户资源：租户管理员全部可见；
 * 跨租户资源：仅当存在显式 TENANT 授权时才可见，否则拒绝。
 *
 * @author IIMS
 */
@Component
@RequiredArgsConstructor
public class TenantAdminAccessPolicy implements AccessPolicy {

    private final PrincipalMatcherRegistry matcherRegistry;

    @Override
    public int order() {
        return 20;
    }

    @Override
    public AccessDecision decide(AccessContext context) {
        if (!context.principal().tenantAdmin()) {
            return AccessDecision.ABSTAIN;
        }
        // 本租户资源直接放行
        if (context.sameTenant()) {
            return AccessDecision.GRANT;
        }
        // 跨租户资源：只有显式 TENANT 授权才能放行
        return hasTenantGrant(context) ? AccessDecision.GRANT : AccessDecision.DENY;
    }

    private boolean hasTenantGrant(AccessContext context) {
        if (!context.hasAcl()) {
            return false;
        }
        for (AclPermissionDO acl : context.aclEntries()) {
            if (acl.getPrincipalType() != PrincipalType.TENANT) {
                continue;
            }
            Integer mask = acl.getPermissionMask();
            if (mask == null || !Permission.hasPermission(mask, context.permission())) {
                continue;
            }
            if (acl.getExpireTime() != null && acl.getExpireTime().isBefore(LocalDateTime.now())) {
                continue;
            }
            if (matcherRegistry.matches(PrincipalType.TENANT, context.principal(), acl.getPrincipalId())) {
                return true;
            }
        }
        return false;
    }
}
