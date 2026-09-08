package cn.iocoder.yudao.module.ai.core.workflow.engine.handler;

import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowVariableResolver;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 指标拦截器（Chain of Responsibility）：记录节点执行状态、耗时，以及每个节点的输入与输出
 *
 * @author yudao
 */
@Slf4j
public class MetricsInterceptor implements NodeExecutionInterceptor {

    @Override
    public Map<String, Object> intercept(NodeExecutionPipeline.NodeExecutionContext execCtx,
                                          Map<String, Object> memory,
                                          NodeExecutionChain chain) {
        long startTime = execCtx.startTime();
        try {
            Map<String, Object> result = chain.proceed(execCtx, memory);
            long elapsed = System.currentTimeMillis() - startTime;
            Map<String, Object> outputs = result == null ? Map.of() : result;
            Map<String, Object> log = new LinkedHashMap<>();
            log.put("status", "success");
            log.put("type", execCtx.node().getType());
            log.put("name", execCtx.node().getName());
            log.put("elapsedMs", elapsed);
            // 输入：节点配置中引用的、能在执行内存解析出的变量
            Map<String, Object> inputs = resolveNodeInputs(execCtx, memory);
            if (!inputs.isEmpty()) {
                log.put("inputs", inputs);
            }
            log.put("outputs", outputs);
            log.put("outputKeys", outputs.keySet().toString());
            execCtx.runCtx().logNode(execCtx.node().getId(), log);
            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            Map<String, Object> log = new LinkedHashMap<>();
            log.put("status", "error");
            log.put("type", execCtx.node().getType());
            log.put("name", execCtx.node().getName());
            log.put("error", e.getMessage() != null && e.getMessage().length() > 500
                    ? e.getMessage().substring(0, 500) : e.getMessage());
            log.put("elapsedMs", elapsed);
            execCtx.runCtx().logNode(execCtx.node().getId(), log);
            throw e;
        }
    }

    private Map<String, Object> resolveNodeInputs(NodeExecutionPipeline.NodeExecutionContext execCtx,
                                                  Map<String, Object> memory) {
        if (execCtx.node().getData() == null) {
            return Map.of();
        }
        return WorkflowVariableResolver.resolveInputs(execCtx.node().getData().toJSONString(), memory);
    }
}