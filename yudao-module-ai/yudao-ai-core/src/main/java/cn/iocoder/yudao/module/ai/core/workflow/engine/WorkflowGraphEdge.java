package cn.iocoder.yudao.module.ai.core.workflow.engine;

import lombok.Data;

/**
 * 工作流连线（画布 JSON edge 的解析模型）
 * <pre>
 * { "id": "xxx", "source": "node-a", "target": "node-b",
 *   "sourceHandle": "true", "data": { "condition": "{{x}} > 1" } }
 * </pre>
 */
@Data
public class WorkflowGraphEdge {

    /** 连线 ID */
    private String id;

    /** 源节点 ID */
    private String source;

    /** 目标节点 ID */
    private String target;

    /** 源节点上的输出连接点标识（如条件节点的 true / false 分支） */
    private String sourceHandle;

    /** 条件表达式（edge.data.condition，支持 {{变量}} 引用，求值为布尔；空表示无条件） */
    private String condition;
}
