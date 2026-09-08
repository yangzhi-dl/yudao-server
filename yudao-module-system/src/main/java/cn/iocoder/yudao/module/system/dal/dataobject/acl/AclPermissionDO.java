package cn.iocoder.yudao.module.system.dal.dataobject.acl;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.system.enums.acl.PrincipalType;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ACL 权限 DO
 *
 * @author IIMS
 */
@TableName("system_acl_permission")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TenantIgnore
public class AclPermissionDO extends BaseDO {

    /**
     * 权限编号
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 归属租户编号
     * <p>该 ACL 记录属于哪个租户的授权。由于实体标注了 {@link TenantIgnore}，MyBatis Plus 不会自动填充，
     * 需在写入时由 {@link cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder} 手动赋值；
     * 读取时仅超管可见全部租户数据，其余用户按本字段隔离。
     */
    private Long tenantId;

    /**
     * 资源类型
     *
     * 枚举 {@link ResourceType}
     */
    private ResourceType resourceType;

    /**
     * 资源编号
     */
    private Long resourceId;

    /**
     * 主体类型
     *
     * 枚举 {@link PrincipalType}
     */
    private PrincipalType principalType;

    /**
     * 主体编号
     */
    private Long principalId;

    /**
     * 权限掩码
     */
    private Integer permissionMask;

    /**
     * 授权时间
     */
    private LocalDateTime grantTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 授权人
     */
    private Long grantBy;

    /**
     * 备注
     */
    private String remark;

}