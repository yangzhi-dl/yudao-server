package cn.iocoder.yudao.module.system.service.acl.model;

import cn.iocoder.yudao.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;

import java.util.List;

/**
 * 当前访问主体上下文
 * <p>
 * 由 ACL 决策引擎在每次判定前解析一次，聚合用户、租户、角色、组织、岗位等身份信息。
 * 后续所有策略与主体匹配器都基于该值对象进行判断，避免重复查询用户信息。
 *
 * @author IIMS
 */
public record AclPrincipal(
        Long userId,
        Long tenantId,
        List<Long> roleIds,
        Long orgId,
        List<Long> postIds,
        boolean superAdmin,
        boolean tenantAdmin,
        DeptDataPermissionRespDTO dataScope) {
}
