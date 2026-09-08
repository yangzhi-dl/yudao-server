package cn.iocoder.yudao.module.bpm.dal.dataobject.acl;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ACL 授权审批单据 DO
 *
 * <p>把 {@code AclPermissionService} 的直接授权/撤销/设置动作，包装成一张待审批的单据，
 * 通过 {@link BpmAclPermissionApprovalOperationTypeEnum} + {@link #getPermissions()}（JSON 快照）
 * 记录审批通过后的真实执行内容，避免审批与执行脱节。
 *
 * @author IIMS
 */
@TableName("bpm_acl_permission_approval")
@KeySequence("bpm_acl_permission_approval_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmAclPermissionApprovalDO extends TenantBaseDO {

    /**
     * 主键
     *
     * 使用 ASSIGN_ID（雪花 ID）由 MyBatis-Plus 生成，不依赖数据库自增
     */
    @TableId
    private Long id;
    /**
     * 申请人的用户编号
     *
     * <p>审批通过后，以该用户身份执行真实的授权/撤销（授权服务依赖 SecurityContext 读取当前操作人）
     */
    private Long userId;
    /**
     * 操作类型
     *
     * 枚举 {@link BpmAclPermissionApprovalOperationTypeEnum#getType()}
     */
    private String operationType;
    /**
     * 资源类型（大写枚举名，如 FILE / WIKI / DOCUMENT）
     */
    private String resourceType;
    /**
     * 资源 ID
     */
    private Long resourceId;
    /**
     * 主体类型（USER / ORGANIZATION / POST / ROLE / TENANT）
     */
    private String principalType;
    /**
     * 主体 ID
     */
    private Long principalId;
    /**
     * 权限列表的 JSON 快照（List&lt;String&gt;）
     */
    private String permissions;
    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
    /**
     * 审批说明 / 理由
     */
    private String remark;
    /**
     * 审批结果
     *
     * 枚举 {@link BpmTaskStatusEnum}
     */
    private Integer status;
    /**
     * 对应的流程编号
     *
     * 关联 ProcessInstance 的 id 属性
     */
    private String processInstanceId;

}