package cn.iocoder.yudao.module.ai.core.workflow.engine.node;

import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowGraphNode;
import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowRunContext;
import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowVariableResolver;
import cn.iocoder.yudao.module.ai.core.workflow.engine.handler.WorkflowNodeHandlerAdapter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 结束节点：按输出定义收集流程结果
 *
 * @author yudao
 */
@Component
public class EndNodeHandler implements WorkflowNodeHandler, WorkflowNodeHandlerAdapter {

    @Override
    public String[] types() {
        return new String[]{"endNode", "end"};
    }

    @Override
    public Map<String, Object> execute(WorkflowGraphNode node, WorkflowRunContext ctx, Map<String, Object> memory) {
        Map<String, Object> outputs = new LinkedHashMap<>();
        JSONArray outputDefs = node.getData() == null ? null : node.getData().getJSONArray("outputDefs");
        if (outputDefs != null) {
            for (int i = 0; i < outputDefs.size(); i++) {
                JSONObject def = outputDefs.getJSONObject(i);
                String name = def.getString("name");
                if (name == null || name.isBlank()) {
                    continue;
                }
                String ref = def.getString("ref");
                if (ref == null || ref.isBlank()) {
                    continue;
                }
                outputs.put(name, resolveRef(ref, memory));
            }
        }
        return outputs;
    }

    /**
     * 解析输出引用：兼容前端保存的 {@code {{nodeId.output}}} 模板写法与
     * {@code nodeId.output} 裸引用写法（模板写法需先剥掉花括号才能命中内存键）。
     */
    private static Object resolveRef(String ref, Map<String, Object> memory) {
        String key = ref.trim();
        if (key.startsWith("{{") && key.endsWith("}}")) {
            key = key.substring(2, key.length() - 2).trim();
        }
        return WorkflowVariableResolver.resolve(key, memory);
    }
}