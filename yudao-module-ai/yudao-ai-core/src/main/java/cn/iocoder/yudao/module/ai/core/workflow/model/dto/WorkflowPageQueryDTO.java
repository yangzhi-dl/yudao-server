package cn.iocoder.yudao.module.ai.core.workflow.model.dto;

import lombok.Data;

/**
 * 工作流分页查询 DTO
 */
@Data
public class WorkflowPageQueryDTO {

    /** 名称（模糊） */
    private String name;

    /** 状态 {@link cn.iocoder.yudao.module.ai.core.workflow.enums.WorkflowStatus} */
    private Integer status;

    /** 页码 */
    private int page = 1;

    /** 每页显示记录数 */
    private int pageSize = 10;
}
