package cn.iocoder.yudao.module.system.service.acl.matcher;

import cn.iocoder.yudao.module.system.enums.acl.PrincipalType;
import cn.iocoder.yudao.module.system.service.acl.model.AclPrincipal;
import org.springframework.stereotype.Component;

/**
 * ROLE 授权主体匹配策略
 *
 * @author IIMS
 */
@Component
public class RolePrincipalMatcher implements PrincipalMatcher {

    @Override
    public PrincipalType type() {
        return PrincipalType.ROLE;
    }

    @Override
    public boolean matches(AclPrincipal principal, Long principalId) {
        return principal.roleIds() != null && principal.roleIds().contains(principalId);
    }
}
