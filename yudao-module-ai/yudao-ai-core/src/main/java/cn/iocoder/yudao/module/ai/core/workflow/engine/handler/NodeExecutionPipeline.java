package cn.iocoder.yudao.module.ai.core.workflow.engine.handler;

import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowGraphNode;
import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowRunContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 责任链模式（Chain of Responsibility）：节点执行管道
 * <p>
 * 将节点执行分解为一系列拦截器链：验证 → 前置处理 → 执行 → 后置处理 → 指标收集。
 * 每个拦截器可决定是否继续传递或短路。
 *
 * @author yudao
 */
@Slf4j
public class NodeExecutionPipeline {

    private final List<NodeExecutionInterceptor> interceptors = new ArrayList<>();

    public NodeExecutionPipeline() {
        registerInterceptor(new ValidationInterceptor());
        registerInterceptor(new MetricsInterceptor());
        registerInterceptor(new LoggingInterceptor());
    }

    public NodeExecutionPipeline registerInterceptor(NodeExecutionInterceptor interceptor) {
        interceptors.add(interceptor);
        return this;
    }

    /**
     * 执行管道：按序调用拦截器，最终调用 handler
     */
    public Map<String, Object> execute(NodeExecutionContext execCtx, Map<String, Object> memory) {
        NodeExecutionChain chain = new DefaultNodeExecutionChain(interceptors, execCtx.handler());
        return chain.proceed(execCtx, memory);
    }

    /**
     * 节点执行上下文
     */
    public record NodeExecutionContext(
            WorkflowGraphNode node,
            WorkflowRunContext runCtx,
            WorkflowNodeHandlerAdapter handler,
            long startTime
    ) {
        public static NodeExecutionContext of(WorkflowGraphNode node,
                                               WorkflowRunContext runCtx,
                                               WorkflowNodeHandlerAdapter handler) {
            return new NodeExecutionContext(node, runCtx, handler, System.currentTimeMillis());
        }
    }

    /**
     * 责任链默认实现（内部类）
     */
    private static class DefaultNodeExecutionChain implements NodeExecutionChain {

        private final List<NodeExecutionInterceptor> interceptors;
        private final WorkflowNodeHandlerAdapter handler;
        private int currentIndex = 0;

        DefaultNodeExecutionChain(List<NodeExecutionInterceptor> interceptors,
                                   WorkflowNodeHandlerAdapter handler) {
            this.interceptors = interceptors;
            this.handler = handler;
        }

        @Override
        public Map<String, Object> proceed(NodeExecutionContext execCtx, Map<String, Object> memory) {
            if (currentIndex < interceptors.size()) {
                NodeExecutionInterceptor interceptor = interceptors.get(currentIndex++);
                return interceptor.intercept(execCtx, memory, this);
            }
            return handler.execute(execCtx.node(), execCtx.runCtx(), memory);
        }
    }
}