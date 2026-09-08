package cn.iocoder.yudao.module.system.service.acl;

import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptListReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.PostDO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.UserPostDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.dept.UserPostMapper;
import cn.iocoder.yudao.module.system.enums.permission.RoleCodeEnum;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import cn.iocoder.yudao.module.system.service.dept.PostService;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.system.service.permission.RoleService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * IPrincipalNameResolver 实现类
 * <p>
 * 基于 yudao-module-system 的用户、角色、部门、岗位服务，
 * 为 ACL 模块提供主体名称解析能力。
 *
 * @author IIMS
 */
@Service
public class PrincipalNameResolverImpl implements IPrincipalNameResolver {

    @Resource
    private AdminUserService adminUserService;
    @Resource
    private RoleService roleService;
    @Resource
    private DeptService deptService;
    @Resource
    private PermissionService permissionService;
    @Resource
    private UserPostMapper userPostMapper;
    @Resource
    private PostService postService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    @DataPermission(enable = false)
    public Map<Long, String> resolveUserNames(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<AdminUserDO> users = adminUserService.getUserList(userIds);
        return users.stream().collect(Collectors.toMap(
                AdminUserDO::getId,
                user -> {
                    String nickname = user.getNickname() != null ? user.getNickname() : "";
                    String email = user.getEmail() != null ? user.getEmail() : "";
                    if (!email.isEmpty()) {
                        return nickname + "(" + email + ")";
                    }
                    return nickname.isEmpty() ? "未知用户" : nickname;
                },
                (a, b) -> a
        ));
    }

    @Override
    @DataPermission(enable = false)
    public Map<Long, String> resolveRoleNames(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<RoleDO> roles = roleService.getRoleList(roleIds);
        return roles.stream().collect(Collectors.toMap(
                RoleDO::getId,
                RoleDO::getName,
                (a, b) -> a
        ));
    }

    @Override
    @DataPermission(enable = false)
    public Map<Long, String> resolvePostNames(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<PostDO> posts = postService.getPostList(postIds);
        return posts.stream().collect(Collectors.toMap(
                PostDO::getId,
                PostDO::getName,
                (a, b) -> a
        ));
    }

    @Override
    @DataPermission(enable = false)
    public List<Long> getUserPostIds(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<UserPostDO> userPosts = userPostMapper.selectListByUserId(userId);
        return userPosts.stream()
                .map(UserPostDO::getPostId)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @DataPermission(enable = false)
    public Map<Long, OrganizationInfo> resolveAllOrganizations() {
        List<DeptDO> allDepts = deptService.getDeptList(new DeptListReqVO());
        return allDepts.stream().collect(Collectors.toMap(
                DeptDO::getId,
                dept -> new OrganizationInfo(
                        dept.getId(),
                        dept.getParentId(),
                        null,
                        dept.getName(),
                        DeptDO.PARENT_ID_ROOT.equals(dept.getParentId()) ? 0 : 1 // type: 0=公司, 1=部门
                ),
                (a, b) -> a
        ));
    }

    @Override
    public boolean isSuperAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        Set<Long> roleIds = permissionService.getUserRoleIdListByUserId(userId);
        return roleService.hasAnySuperAdmin(roleIds);
    }

    @Override
    public boolean isTenantAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        Set<Long> roleIds = permissionService.getUserRoleIdListByUserId(userId);
        List<RoleDO> roles = roleService.getRoleListFromCache(roleIds);
        return roles.stream().anyMatch(role -> RoleCodeEnum.isTenantAdmin(role.getCode()));
    }

    @Override
    public UserInfo getUserById(Long userId) {
        if (userId == null) {
            return null;
        }
        AdminUserDO user = adminUserService.getUser(userId);
        if (user == null) {
            return null;
        }
        Set<Long> roleIds = permissionService.getUserRoleIdListByUserId(userId);
        String roleJson;
        try {
            roleJson = OBJECT_MAPPER.writeValueAsString(roleIds);
        } catch (Exception e) {
            roleJson = "[]";
        }
        return new UserInfo(user.getId(), roleJson, user.getDeptId());
    }
}
