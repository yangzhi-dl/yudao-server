package cn.iocoder.yudao.module.ai.core.workflow.engine.node;

import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowGraphNode;
import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowRunContext;
import cn.iocoder.yudao.module.ai.core.workflow.engine.handler.WorkflowNodeHandlerAdapter;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 资源节点（被动节点）：仅作为 Agent 节点的工具/技能/模型等资源的可视化挂载点。
 * <p>
 * 资源节点本身不参与执行：Agent 节点运行时会根据自身 data 中的
 * toolIds / skillIds / llmId 加载对应资源。因此这里采用空操作（no-op）处理器，
 * 返回空 Map 占位，避免执行器因"不支持的节点类型: resource"而中断。
 *
 * @author yudao
 */
@Component
public class ResourceNodeHandler implements WorkflowNodeHandler, WorkflowNodeHandlerAdapter {

    @Override
    public String[] types() {
        return new String[]{"resourceNode", "resource"};
    }

    @Override
    public Map<String, Object> execute(WorkflowGraphNode node, WorkflowRunContext ctx, Map<String, Object> memory) {
        // 资源节点由 Agent 节点消费其关联数据，本身无执行逻辑
        return Map.of();
    }
}