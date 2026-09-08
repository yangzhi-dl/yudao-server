package cn.iocoder.yudao.module.system.service.acl.matcher;

import cn.iocoder.yudao.module.system.enums.acl.PrincipalType;
import cn.iocoder.yudao.module.system.service.acl.model.AclPrincipal;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * USER 授权主体匹配策略
 *
 * @author IIMS
 */
@Component
public class UserPrincipalMatcher implements PrincipalMatcher {

    @Override
    public PrincipalType type() {
        return PrincipalType.USER;
    }

    @Override
    public boolean matches(AclPrincipal principal, Long principalId) {
        return principal.userId() != null && Objects.equals(principal.userId(), principalId);
    }
}
