package cn.iocoder.yudao.module.system.service.acl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.module.system.controller.admin.acl.vo.AclPermissionPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.acl.vo.AclPermissionGrantCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.acl.vo.AclPermissionRevokeCreateReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.acl.AclPermissionDO;
import cn.iocoder.yudao.module.system.dal.mysql.acl.AclPermissionMapper;
import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.system.enums.acl.PrincipalType;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ACL 权限服务实现
 * <p>
 * 提供资源级 ACL 权限管理能力，包括授予、撤销、校验、查询等操作。
 *
 * @author IIMS
 */
@Slf4j
@Service
public class AclServiceImpl extends ServiceImpl<AclPermissionMapper, AclPermissionDO> implements AclService {

    private final AclPermissionMapper aclPermissionMapper;

    public AclServiceImpl(AclPermissionMapper aclPermissionMapper) {
        this.aclPermissionMapper = aclPermissionMapper;
    }

    /**
     * 授予权限（叠加已有权限）
     */
    @Override
    @Transactional
    public boolean grantPermission(AclPermissionGrantCreateReqVO request) {
        LambdaQueryWrapper<AclPermissionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AclPermissionDO::getResourceType, request.getResourceType())
               .eq(AclPermissionDO::getResourceId, request.getResourceId())
               .eq(AclPermissionDO::getPrincipalType, request.getPrincipalType())
               .eq(AclPermissionDO::getPrincipalId, request.getPrincipalId());

        AclPermissionDO existing = getOne(wrapper);

        if (existing != null) {
            int newMask = Permission.addPermission(existing.getPermissionMask(), request.getPermission());
            existing.setPermissionMask(newMask);
            existing.setExpireTime(request.getExpireTime());
            existing.setGrantBy(request.getGrantBy());
            existing.setRemark(request.getRemark());
            return updateById(existing);
        } else {
            AclPermissionDO permission = new AclPermissionDO();
            permission.setTenantId(TenantContextHolder.getTenantId());
            permission.setResourceType(request.getResourceType());
            permission.setResourceId(request.getResourceId());
            permission.setPrincipalType(request.getPrincipalType());
            permission.setPrincipalId(request.getPrincipalId());
            permission.setPermissionMask(request.getPermission().getMask());
            permission.setGrantTime(LocalDateTime.now());
            permission.setExpireTime(request.getExpireTime());
            permission.setGrantBy(request.getGrantBy());
            permission.setRemark(request.getRemark());
            return save(permission);
        }
    }

    /**
     * 撤销权限
     */
    @Override
    @Transactional
    public boolean revokePermission(AclPermissionRevokeCreateReqVO request) {
        LambdaQueryWrapper<AclPermissionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AclPermissionDO::getResourceType, request.getResourceType())
               .eq(AclPermissionDO::getResourceId, request.getResourceId())
               .eq(AclPermissionDO::getPrincipalType, request.getPrincipalType())
               .eq(AclPermissionDO::getPrincipalId, request.getPrincipalId());

        AclPermissionDO permission = getOne(wrapper);
        if (permission == null) {
            return false;
        }

        int newMask = Permission.removePermission(permission.getPermissionMask(), request.getPermission());

        if (newMask == 0) {
            return removeById(permission.getId());
        } else {
            permission.setPermissionMask(newMask);
            return updateById(permission);
        }
    }

    /**
     * 检查是否具有指定权限
     */
    @Override
    public boolean hasPermission(ResourceType resourceType, Long resourceId,
                                 PrincipalType principalType, Long principalId,
                                 Permission permission) {
        LambdaQueryWrapper<AclPermissionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AclPermissionDO::getResourceType, resourceType)
                .eq(AclPermissionDO::getResourceId, resourceId)
                .eq(AclPermissionDO::getPrincipalType, principalType)
                .eq(AclPermissionDO::getPrincipalId, principalId);

        AclPermissionDO aclPermission = getOne(wrapper);

        if (aclPermission == null) {
            return false;
        }

        if (aclPermission.getExpireTime() != null &&
                aclPermission.getExpireTime().isBefore(LocalDateTime.now())) {
            return false;
        }

        return Permission.hasPermission(aclPermission.getPermissionMask(), permission);
    }

    /**
     * 批量检查多个主体对同一资源的权限
     */
    @Override
    public Map<Long, Boolean> hasPermissions(ResourceType resourceType, Long resourceId,
                                             PrincipalType principalType, List<Long> principalIds,
                                             Permission permission) {
        LambdaQueryWrapper<AclPermissionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AclPermissionDO::getResourceType, resourceType)
                .eq(AclPermissionDO::getResourceId, resourceId)
                .eq(AclPermissionDO::getPrincipalType, principalType)
                .in(AclPermissionDO::getPrincipalId, principalIds);

        List<AclPermissionDO> permissions = list(wrapper);
        Map<Long, Boolean> result = new HashMap<>();

        for (Long principalId : principalIds) {
            result.put(principalId, false);
        }

        for (AclPermissionDO acl : permissions) {
            if (acl.getExpireTime() != null &&
                    acl.getExpireTime().isBefore(LocalDateTime.now())) {
                continue;
            }
            if (Permission.hasPermission(acl.getPermissionMask(), permission)) {
                result.put(acl.getPrincipalId(), true);
            }
        }

        return result;
    }

    // ==================== 用户便捷方法 ====================

    @Override
    public boolean grantUserPermission(Long userId, ResourceType resourceType, Long resourceId,
                                       Permission permission, Long grantBy, LocalDateTime expireTime,
                                       String remark) {
        AclPermissionGrantCreateReqVO request = AclPermissionGrantCreateReqVO.builder()
                .resourceType(resourceType)
                .resourceId(resourceId)
                .principalType(PrincipalType.USER)
                .principalId(userId)
                .permission(permission)
                .grantBy(grantBy)
                .expireTime(expireTime)
                .remark(remark)
                .build();
        return grantPermission(request);
    }

    @Override
    public boolean revokeUserPermission(Long userId, ResourceType resourceType, Long resourceId,
                                        Permission permission) {
        AclPermissionRevokeCreateReqVO request = AclPermissionRevokeCreateReqVO.builder()
                .resourceType(resourceType)
                .resourceId(resourceId)
                .principalType(PrincipalType.USER)
                .principalId(userId)
                .permission(permission)
                .build();
        return revokePermission(request);
    }

    @Override
    public boolean userHasPermission(Long userId, ResourceType resourceType, Long resourceId,
                                     Permission permission) {
        return hasPermission(resourceType, resourceId,
                PrincipalType.USER, userId, permission);
    }

    @Override
    public Map<Long, Boolean> userHasPermissions(List<Long> userIds, ResourceType resourceType,
                                                 Long resourceId, Permission permission) {
        return hasPermissions(resourceType, resourceId,
                PrincipalType.USER, userIds, permission);
    }

    // ==================== 角色便捷方法 ====================

    @Override
    public boolean grantRolePermission(Long roleId, ResourceType resourceType, Long resourceId,
                                       Permission permission, Long grantBy, LocalDateTime expireTime,
                                       String remark) {
        AclPermissionGrantCreateReqVO request = AclPermissionGrantCreateReqVO.builder()
                .resourceType(resourceType)
                .resourceId(resourceId)
                .principalType(PrincipalType.ROLE)
                .principalId(roleId)
                .permission(permission)
                .grantBy(grantBy)
                .expireTime(expireTime)
                .remark(remark)
                .build();
        return grantPermission(request);
    }

    @Override
    public boolean revokeRolePermission(Long roleId, ResourceType resourceType, Long resourceId,
                                        Permission permission) {
        AclPermissionRevokeCreateReqVO request = AclPermissionRevokeCreateReqVO.builder()
                .resourceType(resourceType)
                .resourceId(resourceId)
                .principalType(PrincipalType.ROLE)
                .principalId(roleId)
                .permission(permission)
                .build();
        return revokePermission(request);
    }

    @Override
    public boolean roleHasPermission(Long roleId, ResourceType resourceType, Long resourceId,
                                     Permission permission) {
        return hasPermission(resourceType, resourceId,
                PrincipalType.ROLE, roleId, permission);
    }

    // ==================== 岗位便捷方法 ====================

    @Override
    public boolean grantPostPermission(Long postId, ResourceType resourceType, Long resourceId,
                                       Permission permission, Long grantBy, LocalDateTime expireTime,
                                       String remark) {
        AclPermissionGrantCreateReqVO request = AclPermissionGrantCreateReqVO.builder()
                .resourceType(resourceType)
                .resourceId(resourceId)
                .principalType(PrincipalType.POST)
                .principalId(postId)
                .permission(permission)
                .grantBy(grantBy)
                .expireTime(expireTime)
                .remark(remark)
                .build();
        return grantPermission(request);
    }

    @Override
    public boolean revokePostPermission(Long postId, ResourceType resourceType, Long resourceId,
                                        Permission permission) {
        AclPermissionRevokeCreateReqVO request = AclPermissionRevokeCreateReqVO.builder()
                .resourceType(resourceType)
                .resourceId(resourceId)
                .principalType(PrincipalType.POST)
                .principalId(postId)
                .permission(permission)
                .build();
        return revokePermission(request);
    }

    @Override
    public boolean postHasPermission(Long postId, ResourceType resourceType, Long resourceId,
                                     Permission permission) {
        return hasPermission(resourceType, resourceId,
                PrincipalType.POST, postId, permission);
    }

    // ==================== 组合权限便捷方法 ====================

    @Override
    public boolean userHasAllPermissions(Long userId, ResourceType resourceType, Long resourceId,
                                         Permission... permissions) {
        if (permissions == null) {
            return true;
        }
        for (Permission permission : permissions) {
            if (!userHasPermission(userId, resourceType, resourceId, permission)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean userHasAnyPermission(Long userId, ResourceType resourceType, Long resourceId,
                                        Permission... permissions) {
        if (permissions == null) {
            return false;
        }
        for (Permission permission : permissions) {
            if (userHasPermission(userId, resourceType, resourceId, permission)) {
                return true;
            }
        }
        return false;
    }

    // ==================== 批量查询 ====================

    @Override
    @DataPermission(enable = false)
    public List<AclPermissionDO> listResourcePermissions(ResourceType resourceType, Long resourceId) {
        LambdaQueryWrapper<AclPermissionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AclPermissionDO::getResourceType, resourceType)
                .eq(AclPermissionDO::getResourceId, resourceId);
        return list(wrapper);
    }

    @Override
    public PageResult<AclPermissionDO> pageResourcePermissions(AclPermissionPageReqVO dto) {
        return aclPermissionMapper.pageResourcePermissions(dto);
    }

    @Override
    @DataPermission(enable = false)
    public List<AclPermissionDO> listAllByResourceType(ResourceType resourceType) {
        LambdaQueryWrapper<AclPermissionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AclPermissionDO::getResourceType, resourceType);
        return list(wrapper);
    }

    /**
     * 设置权限（覆盖已有权限掩码，非叠加）
     */
    @Override
    @Transactional
    public void setPermission(ResourceType resourceType, Long resourceId, PrincipalType principalType,
                              Long principalId, int combinedMask, Long grantBy,
                              LocalDateTime expireTime, String remark) {
        LambdaQueryWrapper<AclPermissionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AclPermissionDO::getResourceType, resourceType)
               .eq(AclPermissionDO::getResourceId, resourceId)
               .eq(AclPermissionDO::getPrincipalType, principalType)
               .eq(AclPermissionDO::getPrincipalId, principalId);

        AclPermissionDO existing = getOne(wrapper);
        if (existing != null) {
            existing.setPermissionMask(combinedMask);
            existing.setExpireTime(expireTime);
            existing.setGrantBy(grantBy);
            existing.setRemark(remark);
            updateById(existing);
        } else {
            AclPermissionDO permission = new AclPermissionDO();
            permission.setTenantId(TenantContextHolder.getTenantId());
            permission.setResourceType(resourceType);
            permission.setResourceId(resourceId);
            permission.setPrincipalType(principalType);
            permission.setPrincipalId(principalId);
            permission.setPermissionMask(combinedMask);
            permission.setGrantTime(LocalDateTime.now());
            permission.setExpireTime(expireTime);
            permission.setGrantBy(grantBy);
            permission.setRemark(remark);
            save(permission);
        }
    }
}