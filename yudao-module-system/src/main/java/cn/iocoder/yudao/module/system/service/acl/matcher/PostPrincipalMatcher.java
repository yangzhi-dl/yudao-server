package cn.iocoder.yudao.module.system.service.acl.matcher;

import cn.iocoder.yudao.module.system.enums.acl.PrincipalType;
import cn.iocoder.yudao.module.system.service.acl.model.AclPrincipal;
import org.springframework.stereotype.Component;

/**
 * POST 授权主体匹配策略
 *
 * @author IIMS
 */
@Component
public class PostPrincipalMatcher implements PrincipalMatcher {

    @Override
    public PrincipalType type() {
        return PrincipalType.POST;
    }

    @Override
    public boolean matches(AclPrincipal principal, Long principalId) {
        return principal.postIds() != null && principal.postIds().contains(principalId);
    }
}
