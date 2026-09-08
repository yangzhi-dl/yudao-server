package cn.iocoder.yudao.module.system.service.acl.matcher;

import cn.iocoder.yudao.module.system.enums.acl.PrincipalType;
import cn.iocoder.yudao.module.system.service.acl.model.AclPrincipal;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * TENANT 授权主体匹配策略
 * <p>
 * 关键语义：TENANT 主体只匹配租户管理员，不匹配租户内所有成员。
 * 跨租户授权只对目标租户的管理员可见，管理员再下放给其下的 USER / ROLE / ORGANIZATION / POST。
 *
 * @author IIMS
 */
@Component
public class TenantPrincipalMatcher implements PrincipalMatcher {

    @Override
    public PrincipalType type() {
        return PrincipalType.TENANT;
    }

    @Override
    public boolean matches(AclPrincipal principal, Long principalId) {
        return principal.tenantAdmin()
                && principal.tenantId() != null
                && Objects.equals(principal.tenantId(), principalId);
    }
}
