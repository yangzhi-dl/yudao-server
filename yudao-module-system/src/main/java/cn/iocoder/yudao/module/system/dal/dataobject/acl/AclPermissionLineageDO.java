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
 * ACL 授权链路（血缘）DO
 * <p>
 * 记录每一次授权动作及其「父授权」关系，形成一棵授权树：
 * 撤销某节点时，可级联撤销其整棵下游子树，避免授权链下游出现孤儿权限。
 * 每次授权对应一条记录（{@code permission_mask} 为单个权限位），与 {@link AclPermissionDO}
 * 的物化结果（按主体合并掩码）解耦，保证每条授权边可独立追溯、独立级联。
 *
 * @author IIMS
 */
@TableName("system_acl_permission_lineage")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TenantIgnore
public class AclPermissionLineageDO extends BaseDO {

    /**
     * 链路节点编号
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 归属租户编号
     * <p>跨租户授权时，记录发起授权的租户；写入时由 {@code TenantContextHolder} 手动赋值。
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
     * 被授权主体类型
     *
     * 枚举 {@link PrincipalType}
     */
    private PrincipalType principalType;

    /**
     * 被授权主体编号
     */
    private Long principalId;

    /**
     * 本次授予的单个权限掩码位（非合并掩码）
     */
    private Integer permissionMask;

    /**
     * 授权人用户 ID
     */
    private Long grantBy;

    /**
     * 父授权节点编号（授权人赖以授权的 AUTH 来源），NULL 表示根授权
     */
    private Long parentId;

    /**
     * 过期时间（继承自父节点，不能晚于父节点）
     */
    private LocalDateTime expireTime;

}
