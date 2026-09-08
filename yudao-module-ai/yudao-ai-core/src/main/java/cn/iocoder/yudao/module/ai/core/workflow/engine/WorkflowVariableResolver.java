package cn.iocoder.yudao.module.ai.core.workflow.engine;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工作流变量解析器
 * <ul>
 *   <li>{@link #resolve(String, Map)}：按引用路径从执行内存取值（支持 {@code nodeId.outputName}、嵌套 Map/List）</li>
 *   <li>{@link #render(String, Map)}：渲染模板中的 {@code {{引用}}} 占位符</li>
 *   <li>{@link #evaluateCondition(String, Map)}：求值条件表达式（{{引用}} 替换后按 SpEL 布尔求值）</li>
 * </ul>
 */
public final class WorkflowVariableResolver {

    private static final Pattern VAR_PATTERN = Pattern.compile("\\{\\{\\s*([^}]+?)\\s*}}");
    /** 裸引用（非模板）：形如 nodeId.outputName，用于结束节点 ref、判断条件等字段的解析 */
    private static final Pattern BARE_REF_PATTERN = Pattern.compile("[\\w-]+\\.[\\w-]+");
    private static final ExpressionParser SPEL_PARSER = new SpelExpressionParser();

    private WorkflowVariableResolver() {
    }

    /**
     * 从执行内存解析引用：优先全键匹配，其次按点号逐级查找（Map 字段 / List 下标）
     */
    public static Object resolve(String ref, Map<String, Object> memory) {
        if (ref == null || ref.isBlank()) {
            return null;
        }
        String key = ref.trim();
        if (memory.containsKey(key)) {
            return memory.get(key);
        }
        String[] parts = key.split("\\.");
        Object current = memory.get(parts[0]);
        for (int i = 1; i < parts.length && current != null; i++) {
            if (current instanceof Map<?, ?> map) {
                current = map.get(parts[i]);
            } else if (current instanceof List<?> list) {
                try {
                    current = list.get(Integer.parseInt(parts[i]));
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    return null;
                }
            } else {
                return null;
            }
        }
        return current;
    }

    /**
     * 渲染模板：将 {@code {{引用}}} 替换为实际值（null 替换为空串）
     */
    public static String render(String template, Map<String, Object> memory) {
        if (template == null || template.isEmpty()) {
            return template;
        }
        Matcher matcher = VAR_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder(template.length() + 64);
        int last = 0;
        while (matcher.find()) {
            sb.append(template, last, matcher.start());
            Object value = resolve(matcher.group(1), memory);
            sb.append(value == null ? "" : String.valueOf(value));
            last = matcher.end();
        }
        sb.append(template.substring(last));
        return sb.toString();
    }

    /**
     * 提取节点配置引用的输入变量并解析为实际值。
     * <p>
     * 同时匹配 {@code {{引用}}} 模板占位符与 {@code nodeId.outputName} 裸引用（结束节点 ref 等），
     * 仅保留能在执行内存中解析出真实值的变量，用于运行记录的节点输入明细。
     */
    public static Map<String, Object> resolveInputs(String configJson, Map<String, Object> memory) {
        Map<String, Object> inputs = new LinkedHashMap<>();
        if (configJson == null || configJson.isEmpty() || memory == null || memory.isEmpty()) {
            return inputs;
        }
        Matcher templateMatcher = VAR_PATTERN.matcher(configJson);
        while (templateMatcher.find()) {
            addResolved(inputs, templateMatcher.group(1), memory);
        }
        Matcher bareMatcher = BARE_REF_PATTERN.matcher(configJson);
        while (bareMatcher.find()) {
            addResolved(inputs, bareMatcher.group(), memory);
        }
        return inputs;
    }

    private static void addResolved(Map<String, Object> inputs, String ref, Map<String, Object> memory) {
        if (ref == null || ref.isBlank()) {
            return;
        }
        String key = ref.trim();
        if (inputs.containsKey(key)) {
            return;
        }
        Object value = resolve(key, memory);
        if (value != null) {
            inputs.put(key, value);
        }
    }

    /**
     * 求值条件表达式：如 {@code {{node-1.count}} > 1}、{@code {{node-1.result}} == '成功'}。
     * 字符串值加单引号后交由 SpEL 求值为布尔。
     */
    public static boolean evaluateCondition(String condition, Map<String, Object> memory) {
        if (condition == null || condition.isBlank()) {
            return true;
        }
        String expr = renderForExpression(condition, memory);
        try {
            Boolean result = SPEL_PARSER.parseExpression(expr).getValue(Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            throw new IllegalArgumentException("条件表达式求值失败: " + expr, e);
        }
    }

    /** 条件渲染：字符串值加单引号（转义内部单引号），数字/布尔原样 */
    private static String renderForExpression(String template, Map<String, Object> memory) {
        Matcher matcher = VAR_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder(template.length() + 64);
        int last = 0;
        while (matcher.find()) {
            sb.append(template, last, matcher.start());
            Object value = resolve(matcher.group(1), memory);
            sb.append(quote(value));
            last = matcher.end();
        }
        sb.append(template.substring(last));
        return sb.toString();
    }

    private static String quote(Object value) {
        if (value == null) {
            return "''";
        }
        if (value instanceof String str) {
            return "'" + str.replace("'", "\\'") + "'";
        }
        return String.valueOf(value);
    }
}
