package cn.iocoder.yudao.module.system.service.acl;

import java.util.List;
import java.util.Map;

/**
 * 主体名称解析器接口
 * <p>
 * 用于解耦 ACL 模块对 integral 模块业务服务的依赖。
 * integral 模块需提供此接口的实现并注册为 Spring Bean。
 *
 * @author IIMS
 */
public interface IPrincipalNameResolver {

    /**
     * 批量获取用户信息（id → 显示名称，格式如 "姓名(email)"）
     */
    Map<Long, String> resolveUserNames(List<Long> userIds);

    /**
     * 批量获取角色名称（id → roleName）
     */
    Map<Long, String> resolveRoleNames(List<Long> roleIds);

    /**
     * 批量获取岗位名称（id → postName）
     */
    Map<Long, String> resolvePostNames(List<Long> postIds);

    /**
     * 根据用户ID获取用户所属岗位ID列表
     */
    List<Long> getUserPostIds(Long userId);

    /**
     * 获取全部组织树（id → Organization）
     * <p>返回 Map，key 为组织ID，value 需包含 id、parentId、name、type、jobId 字段。
     */
    Map<Long, OrganizationInfo> resolveAllOrganizations();

    /**
     * 根据用户ID获取用户实体（需包含 role JSON 字段）
     */
    UserInfo getUserById(Long userId);

    /**
     * 判断用户是否为超级管理员
     *
     * @param userId 用户ID
     * @return true=超级管理员
     */
    boolean isSuperAdmin(Long userId);

    /**
     * 判断用户是否为租户管理员
     *
     * @param userId 用户ID
     * @return true=租户管理员
     */
    boolean isTenantAdmin(Long userId);

    /**
     * 组织信息（精简结构，仅保留 ACL 模块需要的关键字段）
     */
    record OrganizationInfo(Long id, Long parentId, Long jobId, String name, Integer type) {}

    /**
     * 用户信息（精简结构，仅保留 ACL 模块需要的关键字段）
     */
    record UserInfo(Long id, String role, Long organization) {}
}
