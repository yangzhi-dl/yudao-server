package cn.iocoder.yudao.module.ai.core.workflow.engine.handler;

import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowGraphNode;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 验证拦截器（Chain of Responsibility）：节点执行前校验
 * <p>
 * 检查节点是否可执行，若已停止则短路。
 *
 * @author yudao
 */
@Slf4j
public class ValidationInterceptor implements NodeExecutionInterceptor {

    @Override
    public Map<String, Object> intercept(NodeExecutionPipeline.NodeExecutionContext execCtx,
                                          Map<String, Object> memory,
                                          NodeExecutionChain chain) {
        WorkflowGraphNode node = execCtx.node();

        // 停止检查：若已标记停止，短路跳过
        if (execCtx.runCtx().getStopped().get()) {
            log.debug("[workflow][node({})] 工作流已停止，跳过执行", node.getId());
            return Map.of();
        }

        // 基本校验
        if (node.getType() == null || node.getType().isBlank()) {
            throw new IllegalArgumentException("节点类型不能为空: " + node.getId());
        }

        return chain.proceed(execCtx, memory);
    }
}