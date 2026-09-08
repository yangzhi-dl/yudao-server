package cn.iocoder.yudao.module.system.service.acl;

import cn.iocoder.yudao.module.system.enums.acl.AclPrincipalTypeOwnerType;
import cn.iocoder.yudao.module.system.enums.acl.PrincipalType;

import java.util.Collection;
import java.util.Set;

/**
 * ACL 授权主体配置 Service
 * <p>
 * 管理角色、租户套餐分别允许使用的授权主体类型。
 *
 * @author IIMS
 */
public interface AclPrincipalTypeConfigService {

    /**
     * 保存（覆盖）某个角色或套餐的授权主体配置
     *
     * @param ownerType      归属类型
     * @param ownerId        归属编号
     * @param principalTypes 允许使用的授权主体集合
     */
    void savePrincipalTypes(AclPrincipalTypeOwnerType ownerType, Long ownerId,
                            Set<PrincipalType> principalTypes);

    /**
     * 获取某个角色或套餐的授权主体集合
     */
    Set<PrincipalType> getPrincipalTypes(AclPrincipalTypeOwnerType ownerType, Long ownerId);

    /**
     * 获取多个角色或套餐的授权主体并集
     */
    Set<PrincipalType> getPrincipalTypes(AclPrincipalTypeOwnerType ownerType,
                                         Collection<Long> ownerIds);

    /**
     * 删除某个角色或套餐的授权主体配置
     */
    void deleteByOwner(AclPrincipalTypeOwnerType ownerType, Long ownerId);

}
