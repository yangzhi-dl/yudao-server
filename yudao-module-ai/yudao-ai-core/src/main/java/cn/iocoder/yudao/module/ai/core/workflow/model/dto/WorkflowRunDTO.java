package cn.iocoder.yudao.module.ai.core.workflow.model.dto;

import lombok.Data;

import java.util.Map;

/**
 * 工作流测试运行 DTO
 */
@Data
public class WorkflowRunDTO {

    /** 工作流编号 */
    private Long workflowId;

    /** 运行输入（变量名 -> 值），对应开始节点的输出变量 */
    private Map<String, Object> inputs;
}
