package cn.iocoder.yudao.module.system.service.acl.matcher;

import cn.iocoder.yudao.module.system.enums.acl.PrincipalType;
import cn.iocoder.yudao.module.system.service.acl.model.AclPrincipal;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * ORGANIZATION 授权主体匹配策略
 *
 * @author IIMS
 */
@Component
public class OrganizationPrincipalMatcher implements PrincipalMatcher {

    @Override
    public PrincipalType type() {
        return PrincipalType.ORGANIZATION;
    }

    @Override
    public boolean matches(AclPrincipal principal, Long principalId) {
        return principal.orgId() != null && Objects.equals(principal.orgId(), principalId);
    }
}
