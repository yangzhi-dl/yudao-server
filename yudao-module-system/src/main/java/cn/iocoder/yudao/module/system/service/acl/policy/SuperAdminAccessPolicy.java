package cn.iocoder.yudao.module.system.service.acl.policy;

import cn.iocoder.yudao.module.system.service.acl.model.AccessContext;
import cn.iocoder.yudao.module.system.service.acl.model.AccessDecision;
import org.springframework.stereotype.Component;

/**
 * 超级管理员放行策略
 * <p>
 * 超级管理员无视租户与 ACL，直接放行。
 *
 * @author IIMS
 */
@Component
public class SuperAdminAccessPolicy implements AccessPolicy {

    @Override
    public int order() {
        return 0;
    }

    @Override
    public AccessDecision decide(AccessContext context) {
        return context.principal().superAdmin() ? AccessDecision.GRANT : AccessDecision.ABSTAIN;
    }
}
