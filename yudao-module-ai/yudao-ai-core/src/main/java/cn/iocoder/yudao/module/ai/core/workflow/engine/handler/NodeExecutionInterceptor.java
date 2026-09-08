package cn.iocoder.yudao.module.ai.core.workflow.engine.handler;

import java.util.Map;

/**
 * 责任链拦截器接口：节点执行管道的处理单元
 *
 * @author yudao
 */
public interface NodeExecutionInterceptor {

    /**
     * 拦截处理
     *
     * @param execCtx 执行上下文
     * @param memory  执行内存
     * @param chain   责任链（调用 chain.proceed 继续传递）
     * @return 节点输出
     */
    Map<String, Object> intercept(NodeExecutionPipeline.NodeExecutionContext execCtx,
                                   Map<String, Object> memory,
                                   NodeExecutionChain chain);
}