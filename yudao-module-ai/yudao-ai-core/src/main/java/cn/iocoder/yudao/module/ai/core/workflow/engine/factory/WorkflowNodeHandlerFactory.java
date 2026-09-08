package cn.iocoder.yudao.module.ai.core.workflow.engine.factory;

import cn.iocoder.yudao.module.ai.core.workflow.engine.handler.NodeExecutionPipeline;
import cn.iocoder.yudao.module.ai.core.workflow.engine.handler.WorkflowNodeHandlerAdapter;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工厂模式（Factory Pattern）：节点处理器工厂
 * <p>
 * 负责注册和管理所有节点处理器，支持运行时按类型查找。
 * 通过 Spring 自动注入所有 {@link WorkflowNodeHandlerAdapter} 实现。
 *
 * @author yudao
 */
@Getter
@Component
public class WorkflowNodeHandlerFactory {

    /** 类型 -> 处理器
     * -- GETTER --
     *  获取所有支持的类型
     */
    private final Map<String, WorkflowNodeHandlerAdapter> handlerMap = new HashMap<>();

    /** 执行管道（可自定义拦截器链）
     * -- GETTER --
     *  获取执行管道
     */
    private final NodeExecutionPipeline pipeline;

    public WorkflowNodeHandlerFactory(List<WorkflowNodeHandlerAdapter> handlers) {
        this.pipeline = new NodeExecutionPipeline();
        if (handlers != null) {
            for (WorkflowNodeHandlerAdapter handler : handlers) {
                for (String type : handler.types()) {
                    handlerMap.put(type, handler);
                }
            }
        }
    }

    /**
     * 按节点类型获取处理器
     */
    public WorkflowNodeHandlerAdapter getHandler(String nodeType) {
        WorkflowNodeHandlerAdapter handler = handlerMap.get(nodeType);
        if (handler == null) {
            throw new IllegalArgumentException("不支持的节点类型: " + nodeType);
        }
        return handler;
    }

    /**
     * 注册自定义拦截器
     */
    public WorkflowNodeHandlerFactory registerInterceptor(
            cn.iocoder.yudao.module.ai.core.workflow.engine.handler.NodeExecutionInterceptor interceptor) {
        pipeline.registerInterceptor(interceptor);
        return this;
    }

}