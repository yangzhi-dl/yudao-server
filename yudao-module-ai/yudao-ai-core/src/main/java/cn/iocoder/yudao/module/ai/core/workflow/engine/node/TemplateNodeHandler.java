package cn.iocoder.yudao.module.ai.core.workflow.engine.node;

import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowGraphNode;
import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowRunContext;
import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowVariableResolver;
import cn.iocoder.yudao.module.ai.core.workflow.engine.handler.WorkflowNodeHandlerAdapter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 模板渲染节点：渲染模板文本，支持 {{变量}} 引用
 *
 * @author yudao
 */
@Component
public class TemplateNodeHandler implements WorkflowNodeHandler, WorkflowNodeHandlerAdapter {

    private static final String DEFAULT_OUTPUT = "output";

    @Override
    public String[] types() {
        return new String[]{"templateNode", "template"};
    }

    @Override
    public Map<String, Object> execute(WorkflowGraphNode node, WorkflowRunContext ctx, Map<String, Object> memory) {
        JSONObject data = node.getData();
        String template = data.getString("template");
        if (!StringUtils.hasText(template)) {
            throw new IllegalArgumentException("模板节点 [%s] 未配置模板内容".formatted(
                    node.getName() == null ? node.getId() : node.getName()));
        }
        String rendered = WorkflowVariableResolver.render(template, memory);
        return Map.of(outputNameOf(node), rendered);
    }

    private String outputNameOf(WorkflowGraphNode node) {
        JSONArray outputDefs = node.getData() == null ? null : node.getData().getJSONArray("outputDefs");
        if (outputDefs != null && !outputDefs.isEmpty()) {
            String name = outputDefs.getJSONObject(0).getString("name");
            if (StringUtils.hasText(name)) {
                return name;
            }
        }
        return DEFAULT_OUTPUT;
    }
}