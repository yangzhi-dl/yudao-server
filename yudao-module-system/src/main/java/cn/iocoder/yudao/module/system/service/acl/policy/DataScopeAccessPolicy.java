package cn.iocoder.yudao.module.system.service.acl.policy;

import cn.iocoder.yudao.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;
import cn.iocoder.yudao.module.system.service.acl.model.AccessContext;
import cn.iocoder.yudao.module.system.service.acl.model.AccessDecision;
import org.springframework.stereotype.Component;

/**
 * yudao 数据范围（ALL / SELF）策略
 * <p>
 * 将 yudao 的创建者数据权限语义融合进 ACL 责任链，仅在本租户内生效：
 * <ul>
 *   <li>ALL：本租户内全部放行；</li>
 *   <li>SELF：至少可见自己创建的，其余交由后续 ACL 授权 / 默认拒绝；</li>
 *   <li>无 ALL/SELF：交由后续 ACL 授权 / 默认拒绝。</li>
 * </ul>
 * 跨租户资源一律 ABSTAIN，不参与数据范围判定，交由租管 / TENANT 授权 / 默认拒绝处理，
 * 从而保证 ALL/SELF 数据范围不会跨租户生效。
 *
 * @author IIMS
 */
@Component
public class DataScopeAccessPolicy implements AccessPolicy {

    @Override
    public int order() {
        return 5;
    }

    @Override
    public AccessDecision decide(AccessContext context) {
        // 数据范围只在当前租户生效，跨租户不生效
        if (!context.sameTenant()) {
            return AccessDecision.ABSTAIN;
        }
        DeptDataPermissionRespDTO dataScope = context.principal().dataScope();
        if (dataScope == null) {
            return AccessDecision.ABSTAIN;
        }
        // ALL：本租户全部放行
        if (Boolean.TRUE.equals(dataScope.getAll())) {
            return AccessDecision.GRANT;
        }
        // SELF：自己创建的放行；非自己创建的交由 ACL 授权 / 默认拒绝
        if (Boolean.TRUE.equals(dataScope.getSelf()) && context.creator()) {
            return AccessDecision.GRANT;
        }
        return AccessDecision.ABSTAIN;
    }
}
