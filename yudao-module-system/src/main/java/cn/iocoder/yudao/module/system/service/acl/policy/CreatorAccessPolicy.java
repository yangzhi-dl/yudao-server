package cn.iocoder.yudao.module.system.service.acl.policy;

import cn.iocoder.yudao.module.system.service.acl.model.AccessContext;
import cn.iocoder.yudao.module.system.service.acl.model.AccessDecision;
import org.springframework.stereotype.Component;

/**
 * 创建者豁免策略
 * <p>
 * 当前用户是本租户内资源的创建者时直接放行，避免授权后创建者反而失去访问权。
 *
 * @author IIMS
 */
@Component
public class CreatorAccessPolicy implements AccessPolicy {

    @Override
    public int order() {
        return 10;
    }

    @Override
    public AccessDecision decide(AccessContext context) {
        if (context.creator() && context.sameTenant()) {
            return AccessDecision.GRANT;
        }
        return AccessDecision.ABSTAIN;
    }
}
