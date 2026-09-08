package cn.iocoder.yudao.module.ai.core.workflow.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.ai.core.workflow.enums.WorkflowStatus;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

/**
 * AI 工作流定义 DO
 *
 * @author yudao
 */
@TableName(value = "ai_workflow", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiWorkflowDO extends TenantBaseDO {

    /**
     * 工作流编号
     */
    @TableId
    private Long id;

    /**
     * 工作流名称
     */
    private String name;

    /**
     * 工作流描述
     */
    private String description;

    /**
     * 图标文件ID
     */
    private Long icon;

    /**
     * 工作流状态 {@link WorkflowStatus}
     */
    private Integer status;

    /**
     * 版本号（每次发布自增）
     */
    private Integer version;

    /**
     * 画布数据（JSON：nodes + edges）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object graph;

    /**
     * 已发布画布快照（JSON），用于运行已发布版本
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object publishedGraph;

    /**
     * 使用次数
     */
    private Long usageCount;
}
