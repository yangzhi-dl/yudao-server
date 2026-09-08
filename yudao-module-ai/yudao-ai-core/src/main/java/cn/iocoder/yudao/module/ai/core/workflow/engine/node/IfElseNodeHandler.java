package cn.iocoder.yudao.module.ai.core.workflow.engine.node;

import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowGraphNode;
import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowRunContext;
import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowVariableResolver;
import cn.iocoder.yudao.module.ai.core.workflow.engine.handler.WorkflowNodeHandlerAdapter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 判断节点：透传路由（等价于模型工具/网关）
 * <p>
 * 判断节点自身不做任何模型判断，而是"透传"上游模型节点的输出作为分支序号来路由：
 * <ul>
 *   <li>上游模型节点先对用户意图分类，把走哪个分支以"序号"输出（如 0/1/2...）；</li>
 *   <li>判断节点通过 inputRef 读取该序号，路由到第 N 个分支（分支索引 → 出边连接点 0、1、2...）；</li>
 *   <li>分支的名称/描述仅用于展示，不参与匹配。</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>
 *   [开始] → [大模型(意图分类输出序号)] → [判断] → [分支0: A股行情] → [A股行情智能体]
 *                                           ↘ [分支1: 资料检索] → [资料检索智能体]
 * </pre>
 *
 * @author yudao
 */
@Slf4j
@Component
public class IfElseNodeHandler implements WorkflowNodeHandler, WorkflowNodeHandlerAdapter {

    private static final Pattern INT_PATTERN = Pattern.compile("-?\\d+");

    @Override
    public String[] types() {
        return new String[]{"ifElseNode", "ifelse", "condition"};
    }

    /**
     * 判断节点类型是否为"条件/判断"节点。
     */
    public static boolean isIfElse(String type) {
        return type != null && (type.equalsIgnoreCase("ifElseNode")
                || type.equalsIgnoreCase("ifelse")
                || type.equalsIgnoreCase("condition"));
    }

    /**
     * 节点是否配置了可用分支（conditions 数组非空）。
     */
    public static boolean hasConditions(WorkflowGraphNode node) {
        JSONObject data = node.getData();
        if (data == null) {
            return false;
        }
        JSONArray conditions = data.getJSONArray("conditions");
        return conditions != null && !conditions.isEmpty();
    }

    /**
     * 返回命中分支对应的出边连接点（sourceHandle）。
     * <p>
     * 透传上游模型输出的分支序号（inputRef）路由，不做任何匹配。
     * 未命中时返回 null，表示当前路径终止、不进入任何出边。
     *
     * @return 命中的 sourceHandle；未命中/无条件时返回 null
     */
    public String resolveBranch(WorkflowGraphNode node, Map<String, Object> memory) {
        JSONObject data = node.getData();
        if (data == null) {
            return null;
        }
        JSONArray conditions = data.getJSONArray("conditions");
        if (conditions == null || conditions.isEmpty()) {
            return null;
        }
        return routeByIndex(data, memory, conditions);
    }

    /** 按 inputRef 读取上游模型输出的分支序号，路由到对应分支 */
    private String routeByIndex(JSONObject data, Map<String, Object> memory, JSONArray conditions) {
        String value = resolveIndexValue(data, memory);
        int index = parseIndex(value);
        if (index >= 0 && index < conditions.size()) {
            return matchHandle(index);
        }
        log.warn("[IfElse] 未命中分支：inputRef 取值为 [{}]，合法序号应为 [0, {})", value, conditions.size());
        return null;
    }

    /** 读取 inputRef 指向的变量值（透传上游模型输出），支持 {{ }} 包裹 */
    private static String resolveIndexValue(JSONObject data, Map<String, Object> memory) {
        String raw = data.getString("inputRef");
        String ref = raw == null ? null : raw.replace("{{", "").replace("}}", "").trim();
        if (ref == null || ref.isBlank()) {
            return null;
        }
        Object v = WorkflowVariableResolver.resolve(ref, memory);
        return v == null ? null : String.valueOf(v);
    }

    /** 从文本中提取分支序号（支持 -1） */
    private static int parseIndex(String text) {
        if (text == null) {
            return -1;
        }
        Matcher matcher = INT_PATTERN.matcher(text.trim());
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group());
            } catch (NumberFormatException ignored) {
                // ignore
            }
        }
        return -1;
    }

    /** 分支索引 -> 出边连接点：0、1、2... */
    private static String matchHandle(int index) {
        return String.valueOf(index);
    }

    @Override
    public Map<String, Object> execute(WorkflowGraphNode node, WorkflowRunContext ctx, Map<String, Object> memory) {
        // 判断节点是路由节点，不做数据转换，直接透传
        // 分支选择由 WorkflowExecutor 依据上游模型输出序号（inputRef）决定
        Map<String, Object> outputs = new LinkedHashMap<>();

        // 输出命中的分支信息供调试使用
        JSONArray conditions = node.getData() == null ? null : node.getData().getJSONArray("conditions");
        if (conditions != null) {
            for (int i = 0; i < conditions.size(); i++) {
                JSONObject cond = conditions.getJSONObject(i);
                String label = cond == null ? null : cond.getString("label");
                String description = cond == null ? null : cond.getString("description");
                if (label != null && !label.isBlank()) {
                    outputs.put("branch_" + i, Map.of(
                            "label", label,
                            "description", description == null ? "" : description));
                }
            }
        }

        return outputs;
    }

    /**
     * 提示词注入：当该判断节点作为大模型节点的下游（承接点）时，
     * 向来源大模型提供「分支路由」提示词模板（列出各分支），
     * 用户的系统提示词将置于 {@code %s} 处。
     */
    @Override
    public String buildPredecessorSystemPrompt(WorkflowGraphNode self, WorkflowGraphNode predecessor) {
        JSONObject data = self.getData();
        JSONArray conditions = data == null ? null : data.getJSONArray("conditions");
        if (conditions == null || conditions.isEmpty()) {
            return null;
        }
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是工作流的分支路由器。请根据用户问题判断应走哪个分支：\n");
        for (int i = 0; i < conditions.size(); i++) {
            JSONObject cond = conditions.getJSONObject(i);
            String label = cond == null ? null : cond.getString("label");
            String desc = cond == null ? null : cond.getString("description");
            prompt.append("- 分支 ")
                    .append(i)
                    .append("：")
                    .append(label == null ? "" : label)
                    .append(desc == null || desc.isBlank() ? "" : "（" + desc + "）")
                    .append("\n");
        }
        // 模板：前缀 + %s（用户系统提示词）+ 后缀（字段说明取自前驱大模型的 outputDefs）
        String fields = LlmNodeHandler.buildOutputFieldsDescription(
                predecessor == null ? null : predecessor.getData());
        String fieldDesc = fields.isBlank() ? "字段与输出定义一致" : fields;
        return prompt + "%s\n只返回一个合法的 JSON 对象，不要 Markdown 代码块，字段定义为：" + fieldDesc + "\n";
    }
}