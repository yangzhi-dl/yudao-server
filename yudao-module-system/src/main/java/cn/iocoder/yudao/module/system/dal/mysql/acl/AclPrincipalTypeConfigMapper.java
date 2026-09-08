package cn.iocoder.yudao.module.system.dal.mysql.acl;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.acl.AclPrincipalTypeConfigDO;
import cn.iocoder.yudao.module.system.enums.acl.AclPrincipalTypeOwnerType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * ACL 授权主体配置 Mapper
 *
 * @author IIMS
 */
@Mapper
public interface AclPrincipalTypeConfigMapper extends BaseMapperX<AclPrincipalTypeConfigDO> {

    default AclPrincipalTypeConfigDO selectByOwner(AclPrincipalTypeOwnerType ownerType, Long ownerId) {
        return selectOne(new LambdaQueryWrapper<AclPrincipalTypeConfigDO>()
                .eq(AclPrincipalTypeConfigDO::getOwnerType, ownerType)
                .eq(AclPrincipalTypeConfigDO::getOwnerId, ownerId));
    }

    default List<AclPrincipalTypeConfigDO> selectByOwner(AclPrincipalTypeOwnerType ownerType,
                                                         Collection<Long> ownerIds) {
        return selectList(new LambdaQueryWrapper<AclPrincipalTypeConfigDO>()
                .eq(AclPrincipalTypeConfigDO::getOwnerType, ownerType)
                .in(AclPrincipalTypeConfigDO::getOwnerId, ownerIds));
    }

    /**
     * 物理删除指定归属的授权主体配置
     * <p>
     * 使用原生 SQL 绕过 {@code @TableLogic} 的逻辑删除。该表唯一键
     * {@code uk_owner_principal(owner_type, owner_id, principal_type)} 不包含 deleted，
     * 若走逻辑删除会残留 deleted=1 的旧副本，导致再次插入同键时冲突。
     */
    @Delete("DELETE FROM system_acl_principal_type_config WHERE owner_type = #{ownerType} AND owner_id = #{ownerId}")
    int deleteByOwner(@Param("ownerType") Integer ownerType, @Param("ownerId") Long ownerId);

}
