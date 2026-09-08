package cn.iocoder.yudao.module.ai.core.workflow.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.ai.core.workflow.enums.WorkflowRunStatus;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;

/**
 * AI 工作流运行记录 DO
 *
 * @author yudao
 */
@TableName(value = "ai_workflow_run", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiWorkflowRunDO extends TenantBaseDO {

    /**
     * 运行编号
     */
    @TableId
    private Long id;

    /**
     * 工作流编号
     */
    private Long workflowId;

    /**
     * 运行状态 {@link WorkflowRunStatus}
     */
    private Integer status;

    /**
     * 运行输入（JSON：变量名 -> 值）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object inputs;

    /**
     * 运行输出（JSON：变量名 -> 值）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object outputs;

    /**
     * 节点执行日志（JSON：nodeId -> {status, inputs, outputs, elapsedMs, error}）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object nodeLogs;

    /**
     * 最终产物（JSON：presentFiles 工具生成的文件列表 [{id, filename, fileSize, fileType, url}, ...]）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Object> fileInfos;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 总耗时（毫秒）
     */
    private Long elapsedMs;
}
