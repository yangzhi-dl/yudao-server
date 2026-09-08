package cn.iocoder.yudao.module.ai.core.workflow.engine.node;

import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowGraphNode;
import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowRunContext;

import java.util.Map;

/**
 * 工作流节点处理器（自研引擎的节点执行单元）
 * <p>
 * 返回的 Map 会按 {@code 节点ID.输出名} 写入执行内存，供下游 {@code {{节点ID.输出名}}} 或 {@code {{输出名}}} 引用。
 */
public interface WorkflowNodeHandler {

    /**
     * 支持的节点类型（含兼容别名）
     */
    String[] types();

    /**
     * 执行节点
     *
     * @param node   当前节点（data 为画布配置）
     * @param ctx    运行上下文（SSE 事件、停止标记等）
     * @param memory 执行内存（含输入变量与已执行节点的输出）
     * @return 节点输出（key 为输出名）
     */
    Map<String, Object> execute(WorkflowGraphNode node, WorkflowRunContext ctx, Map<String, Object> memory);
}
