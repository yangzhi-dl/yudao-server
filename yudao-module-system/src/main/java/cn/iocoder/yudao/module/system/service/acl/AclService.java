package cn.iocoder.yudao.module.system.service.acl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.dal.dataobject.acl.AclPermissionDO;
import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.system.enums.acl.PrincipalType;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import cn.iocoder.yudao.module.system.controller.admin.acl.vo.AclPermissionPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.acl.vo.AclPermissionGrantCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.acl.vo.AclPermissionRevokeCreateReqVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ACL 权限服务
 * <p>
 * 提供面向业务场景的便捷方法，简化权限授予、撤销、校验等操作。
 *
 * @author IIMS
 */
public interface AclService {

    // ==================== 基础操作 ====================

    /**
     * 授予权限
     */
    boolean grantPermission(AclPermissionGrantCreateReqVO request);

    /**
     * 撤销权限
     */
    boolean revokePermission(AclPermissionRevokeCreateReqVO request);

    /**
     * 检查是否具有指定权限
     *
     * @param resourceType  资源类型
     * @param resourceId    资源ID
     * @param principalType 主体类型
     * @param principalId   主体ID
     * @param permission    需要检查的权限
     * @return true=具有该权限
     */
    boolean hasPermission(ResourceType resourceType, Long resourceId,
                          PrincipalType principalType, Long principalId,
                          Permission permission);

    /**
     * 批量检查多个主体对同一资源的权限
     */
    Map<Long, Boolean> hasPermissions(ResourceType resourceType, Long resourceId,
                                      PrincipalType principalType, List<Long> principalIds,
                                      Permission permission);

    // ==================== 用户便捷方法 ====================

    /**
     * 为用户授予资源权限
     */
    boolean grantUserPermission(Long userId, ResourceType resourceType, Long resourceId,
                                Permission permission, Long grantBy, LocalDateTime expireTime,
                                String remark);

    /**
     * 撤销用户的资源权限
     */
    boolean revokeUserPermission(Long userId, ResourceType resourceType, Long resourceId,
                                 Permission permission);

    /**
     * 检查用户是否具有指定资源权限
     */
    boolean userHasPermission(Long userId, ResourceType resourceType, Long resourceId,
                              Permission permission);

    /**
     * 批量检查多个用户对同一资源的权限
     */
    Map<Long, Boolean> userHasPermissions(List<Long> userIds, ResourceType resourceType,
                                          Long resourceId, Permission permission);

    // ==================== 角色便捷方法 ====================

    /**
     * 为角色授予资源权限
     */
    boolean grantRolePermission(Long roleId, ResourceType resourceType, Long resourceId,
                                Permission permission, Long grantBy, LocalDateTime expireTime,
                                String remark);

    /**
     * 撤销角色的资源权限
     */
    boolean revokeRolePermission(Long roleId, ResourceType resourceType, Long resourceId,
                                 Permission permission);

    /**
     * 检查角色是否具有指定资源权限
     */
    boolean roleHasPermission(Long roleId, ResourceType resourceType, Long resourceId,
                              Permission permission);

    // ==================== 岗位便捷方法 ====================

    /**
     * 为岗位授予资源权限
     */
    boolean grantPostPermission(Long postId, ResourceType resourceType, Long resourceId,
                                Permission permission, Long grantBy, LocalDateTime expireTime,
                                String remark);

    /**
     * 撤销岗位的资源权限
     */
    boolean revokePostPermission(Long postId, ResourceType resourceType, Long resourceId,
                                 Permission permission);

    /**
     * 检查岗位是否具有指定资源权限
     */
    boolean postHasPermission(Long postId, ResourceType resourceType, Long resourceId,
                              Permission permission);

    // ==================== 组合权限便捷方法 ====================

    /**
     * 检查用户对资源是否同时拥有多个权限（全部满足）
     */
    boolean userHasAllPermissions(Long userId, ResourceType resourceType, Long resourceId,
                                  Permission... permissions);

    /**
     * 检查用户对资源是否拥有任意一个权限
     */
    boolean userHasAnyPermission(Long userId, ResourceType resourceType, Long resourceId,
                                 Permission... permissions);

    // ==================== 批量查询 ====================

    /**
     * 查询指定资源的所有权限记录
     */
    List<AclPermissionDO> listResourcePermissions(ResourceType resourceType, Long resourceId);

    /**
     * 分页查询指定资源的权限记录
     */
    PageResult<AclPermissionDO> pageResourcePermissions(AclPermissionPageReqVO dto);

    /**
     * 查询指定资源类型下的全部 ACL 记录（所有资源）
     */
    List<AclPermissionDO> listAllByResourceType(ResourceType resourceType);

    /**
     * 设置权限（覆盖已有权限掩码，非叠加）
     */
    void setPermission(ResourceType resourceType, Long resourceId, PrincipalType principalType,
                       Long principalId, int combinedMask, Long grantBy,
                       LocalDateTime expireTime, String remark);
}