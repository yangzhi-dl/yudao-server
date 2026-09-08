package cn.iocoder.yudao.module.ai.core.workflow.engine.node;

import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowGraphNode;
import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowRunContext;
import cn.iocoder.yudao.module.ai.core.workflow.engine.handler.WorkflowNodeHandlerAdapter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 开始节点：工作流入口，支持多种触发方式
 * <ul>
 *   <li>表单触发：接收外部表单提交的参数</li>
 *   <li>API 触发：接收 API 调用传入的 inputs</li>
 *   <li>手动触发：从测试面板输入参数</li>
 * </ul>
 * 参数映射：定义参数名（name）和默认值（defaultValue），
 * 运行时优先使用外部传入的实际值，其次使用默认值。
 *
 * @author yudao
 */
@Component
public class StartNodeHandler implements WorkflowNodeHandler, WorkflowNodeHandlerAdapter {

    @Override
    public String[] types() {
        return new String[]{"startNode", "start"};
    }

    @Override
    public Map<String, Object> execute(WorkflowGraphNode node, WorkflowRunContext ctx, Map<String, Object> memory) {
        Map<String, Object> outputs = new LinkedHashMap<>();

        // 输出触发类型信息
        outputs.put("triggerType", "manual");

        // 处理参数定义：从外部 inputs 取值，无则使用默认值
        JSONArray parameters = node.getData() == null ? null : node.getData().getJSONArray("parameters");
        if (parameters != null) {
            Map<String, Object> inputs = ctx.getInputs();
            for (int i = 0; i < parameters.size(); i++) {
                JSONObject param = parameters.getJSONObject(i);
                String name = param.getString("name");
                if (name == null || name.isBlank()) {
                    continue;
                }
                // 优先外部输入值 -> 默认值
                Object value = inputs.get(name);
                if (value == null) {
                    value = param.get("value");
                }
                if (value == null) {
                    value = param.get("defaultValue");
                }
                outputs.put(name, value);
            }
        }

        // 同时将 inputs 中未在 parameters 定义的参数也透传
        ctx.getInputs().forEach((k, v) -> {
            if (!outputs.containsKey(k)) {
                outputs.put(k, v);
            }
        });

        return outputs;
    }
}