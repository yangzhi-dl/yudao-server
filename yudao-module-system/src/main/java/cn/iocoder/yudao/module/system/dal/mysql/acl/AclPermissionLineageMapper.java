package cn.iocoder.yudao.module.system.dal.mysql.acl;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.acl.AclPermissionLineageDO;
import cn.iocoder.yudao.module.system.enums.acl.PrincipalType;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * ACL 授权链路（血缘）Mapper
 *
 * @author IIMS
 */
@Mapper
public interface AclPermissionLineageMapper extends BaseMapperX<AclPermissionLineageDO> {

    /**
     * 查询指定资源、主体的有效授权链节点（已过滤逻辑删除）
     */
    default List<AclPermissionLineageDO> selectActive(ResourceType resourceType, Long resourceId,
                                                      PrincipalType principalType, Long principalId,
                                                      int permissionMask) {
        return selectList(new LambdaQueryWrapper<AclPermissionLineageDO>()
                .eq(AclPermissionLineageDO::getResourceType, resourceType)
                .eq(AclPermissionLineageDO::getResourceId, resourceId)
                .eq(AclPermissionLineageDO::getPrincipalType, principalType)
                .eq(AclPermissionLineageDO::getPrincipalId, principalId)
                .eq(AclPermissionLineageDO::getPermissionMask, permissionMask));
    }

    /**
     * 查询指定资源上拥有某权限的所有有效授权链节点
     */
    default List<AclPermissionLineageDO> selectActiveByPermission(ResourceType resourceType, Long resourceId,
                                                                  int permissionMask) {
        return selectList(new LambdaQueryWrapper<AclPermissionLineageDO>()
                .eq(AclPermissionLineageDO::getResourceType, resourceType)
                .eq(AclPermissionLineageDO::getResourceId, resourceId)
                .eq(AclPermissionLineageDO::getPermissionMask, permissionMask));
    }

    /**
     * 查询指定资源上全部有效授权链节点
     */
    default List<AclPermissionLineageDO> selectActiveByResource(ResourceType resourceType, Long resourceId) {
        return selectList(new LambdaQueryWrapper<AclPermissionLineageDO>()
                .eq(AclPermissionLineageDO::getResourceType, resourceType)
                .eq(AclPermissionLineageDO::getResourceId, resourceId));
    }

    /**
     * 查询指定资源、主体的全部有效授权链节点（不区分权限位）
     */
    default List<AclPermissionLineageDO> selectActiveByPrincipal(ResourceType resourceType, Long resourceId,
                                                                 PrincipalType principalType, Long principalId) {
        return selectList(new LambdaQueryWrapper<AclPermissionLineageDO>()
                .eq(AclPermissionLineageDO::getResourceType, resourceType)
                .eq(AclPermissionLineageDO::getResourceId, resourceId)
                .eq(AclPermissionLineageDO::getPrincipalType, principalType)
                .eq(AclPermissionLineageDO::getPrincipalId, principalId));
    }

    /**
     * 批量查询指定父节点下的全部子授权链节点
     */
    default List<AclPermissionLineageDO> selectActiveByParentIds(Collection<Long> parentIds) {
        if (CollectionUtils.isEmpty(parentIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<AclPermissionLineageDO>()
                .in(AclPermissionLineageDO::getParentId, parentIds));
    }

}
