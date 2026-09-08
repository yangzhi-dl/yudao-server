package cn.iocoder.yudao.module.system.dal.mysql.acl;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.system.controller.admin.acl.vo.AclPermissionPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.acl.AclPermissionDO;
import cn.iocoder.yudao.module.system.enums.acl.PrincipalType;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * ACL 权限 Mapper
 *
 * @author IIMS
 */
@Mapper
public interface AclPermissionMapper extends BaseMapperX<AclPermissionDO> {

    /**
     * 分页查询资源权限
     */
    default PageResult<AclPermissionDO> pageResourcePermissions(AclPermissionPageReqVO dto) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(dto.getPage());
        pageParam.setPageSize(dto.getPageSize());
        LambdaQueryWrapper<AclPermissionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AclPermissionDO::getResourceType, ResourceType.valueOf(dto.getResourceType()))
                .eq(AclPermissionDO::getResourceId, dto.getResourceId())
                .orderByDesc(AclPermissionDO::getGrantTime);
        wrapper.eq(AclPermissionDO::getTenantId, TenantContextHolder.getTenantId());

        return selectPage(pageParam, wrapper);
    }

    /**
     * 按租户 + 资源 + 主体查询物化授权记录（AclPermissionDO 为 {@code @TenantIgnore}，需显式指定租户）
     */
    default AclPermissionDO selectByTenantResourcePrincipal(Long tenantId, ResourceType resourceType, Long resourceId,
                                                            PrincipalType principalType, Long principalId) {
        return selectOne(new LambdaQueryWrapper<AclPermissionDO>()
                .eq(AclPermissionDO::getTenantId, tenantId)
                .eq(AclPermissionDO::getResourceType, resourceType)
                .eq(AclPermissionDO::getResourceId, resourceId)
                .eq(AclPermissionDO::getPrincipalType, principalType)
                .eq(AclPermissionDO::getPrincipalId, principalId));
    }

}