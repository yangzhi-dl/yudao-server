package cn.iocoder.yudao.module.ai.core.workflow.model.dto;

import lombok.Data;

/**
 * 更新工作流 DTO
 */
@Data
public class UpdateWorkflowDTO {

    /** 工作流编号 */
    private Long id;

    /** 工作流名称 */
    private String name;

    /** 工作流描述 */
    private String description;

    /** 图标文件ID */
    private Long icon;

    /** 画布数据（JSON：nodes + edges） */
    private Object graph;
}
