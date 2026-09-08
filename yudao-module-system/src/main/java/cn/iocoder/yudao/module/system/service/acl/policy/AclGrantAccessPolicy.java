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
 * ACL 授权命中策略
 * <p>
 * 通过 USER / ROLE / ORGANIZATION / POST 主体匹配判断是否放行。
 * TENANT 主体不在此处理，由 {@link TenantAdminAccessPolicy} 专门负责。
 *
 * @author IIMS
 */
@Component
@RequiredArgsConstructor
public class AclGrantAccessPolicy implements AccessPolicy {

    private final PrincipalMatcherRegistry matcherRegistry;

    @Override
    public int order() {
        return 30;
    }

    @Override
    public AccessDecision decide(AccessContext context) {
        if (!context.hasAcl()) {
            return AccessDecision.ABSTAIN;
        }
        for (AclPermissionDO acl : context.aclEntries()) {
            if (acl.getPrincipalType() == PrincipalType.TENANT) {
                continue;
            }
            Integer mask = acl.getPermissionMask();
            if (mask == null || !Permission.hasPermission(mask, context.permission())) {
                continue;
            }
            if (acl.getExpireTime() != null && acl.getExpireTime().isBefore(LocalDateTime.now())) {
                continue;
            }
            if (matcherRegistry.matches(acl.getPrincipalType(), context.principal(), acl.getPrincipalId())) {
                return AccessDecision.GRANT;
            }
        }
        return AccessDecision.ABSTAIN;
    }
}
