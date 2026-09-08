package cn.iocoder.yudao.module.ai.core.workflow.engine.handler;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 日志拦截器（Chain of Responsibility）：记录节点执行日志
 *
 * @author yudao
 */
@Slf4j
public class LoggingInterceptor implements NodeExecutionInterceptor {

    @Override
    public Map<String, Object> intercept(NodeExecutionPipeline.NodeExecutionContext execCtx,
                                          Map<String, Object> memory,
                                          NodeExecutionChain chain) {
        log.debug("[workflow][node({})] 开始执行: type={}, name={}",
                execCtx.node().getId(), execCtx.node().getType(), execCtx.node().getName());
        Map<String, Object> result = chain.proceed(execCtx, memory);
        log.debug("[workflow][node({})] 执行完成: type={}, elapsed={}ms",
                execCtx.node().getId(), execCtx.node().getType(),
                System.currentTimeMillis() - execCtx.startTime());
        return result;
    }
}