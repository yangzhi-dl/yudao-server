package cn.iocoder.yudao.module.ai.core.workflow.engine.handler;

import java.util.Map;

/**
 * 责任链接口：控制拦截器链的执行流程
 *
 * @author yudao
 */
public interface NodeExecutionChain {

    /**
     * 继续执行下一个拦截器或最终处理器
     */
    Map<String, Object> proceed(NodeExecutionPipeline.NodeExecutionContext execCtx,
                                 Map<String, Object> memory);
}