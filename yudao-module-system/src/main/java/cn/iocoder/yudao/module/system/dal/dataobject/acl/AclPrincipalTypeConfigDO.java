package cn.iocoder.yudao.module.system.dal.dataobject.acl;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.system.enums.acl.AclPrincipalTypeOwnerType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * ACL 授权主体可用性配置 DO
 * <p>
 * 记录角色或租户套餐分别允许使用哪些授权主体（USER / ORGANIZATION / POST / ROLE / TENANT），
 * 使用 {@code principal_type_mask} 位掩码存储，一个归属只对应一行。
 *
 * @author IIMS
 */
@TableName("system_acl_principal_type_config")
@KeySequence("system_acl_principal_type_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TenantIgnore
public class AclPrincipalTypeConfigDO extends BaseDO {

    /**
     * 配置编号
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 归属类型
     *
     * 枚举 {@link AclPrincipalTypeOwnerType}
     */
    private AclPrincipalTypeOwnerType ownerType;

    /**
     * 归属编号（角色 ID 或租户套餐 ID）
     */
    private Long ownerId;

    /**
     * 授权主体类型掩码（位运算）
     *
     * 参见 {@link cn.iocoder.yudao.module.system.enums.acl.PrincipalType}
     */
    private Integer principalTypeMask;

}
