package cn.iocoder.yudao.module.system.service.acl.policy;

import cn.iocoder.yudao.module.system.service.acl.model.AccessContext;
import cn.iocoder.yudao.module.system.service.acl.model.AccessDecision;
import org.springframework.stereotype.Component;

/**
 * 默认拒绝策略
 * <p>
 * 责任链兜底：前面所有策略均未放行时，统一拒绝。
 * 这也包含“无 ACL 记录时默认私有”的语义。
 *
 * @author IIMS
 */
@Component
public class DefaultDenyAccessPolicy implements AccessPolicy {

    @Override
    public int order() {
        return Integer.MAX_VALUE;
    }

    @Override
    public AccessDecision decide(AccessContext context) {
        return AccessDecision.DENY;
    }
}
