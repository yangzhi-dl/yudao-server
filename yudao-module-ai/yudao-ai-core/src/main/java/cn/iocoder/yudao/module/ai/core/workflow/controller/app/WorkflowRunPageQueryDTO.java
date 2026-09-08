package cn.iocoder.yudao.module.ai.core.workflow.controller.app;

import lombok.Data;

/**
 * 运行记录分页查询 DTO
 */
@Data
public class WorkflowRunPageQueryDTO {

    /** 工作流编号 */
    private Long workflowId;

    /** 页码 */
    private int page = 1;

    /** 每页显示记录数 */
    private int pageSize = 10;
}
