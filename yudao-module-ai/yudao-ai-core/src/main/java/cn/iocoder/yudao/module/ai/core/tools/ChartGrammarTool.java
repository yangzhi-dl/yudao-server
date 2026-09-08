package cn.iocoder.yudao.module.ai.core.tools;

import cn.iocoder.yudao.module.ai.core.tools.factory.AITool;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChartGrammarTool implements AITool {

    private final ObjectMapper objectMapper;

    private static final String MERMAID_DOCS_PATH = "mermaid/";

    @Override
    public Object getToolInstance() {
        return this;
    }

    @Override
    public String getName() {
        return "chart-grammar-tools";
    }

    @Override
    public String getTitle() {
        return "Mermaid语法说明书";
    }

    @Override
    public String getDescription() {
        return "给模型提供相应图表构建的语法说明书，模型可以根据该语法来构建图表";
    }

    @Tool(description = "获取所有可用的Mermaid图类型列表，返回图表英文名称与中文名称的映射关系")
    public String getExistingCharts() {
        Map<String, String> chartMap = getChartMapping();
        try {
            return objectMapper.writeValueAsString(chartMap);
        } catch (Exception e) {
            return chartMap.toString();
        }
    }

    @Tool(description = "根据图英文名称获取对应的Mermaid语法说明书，包含图的标准语法、配置选项和使用示例")
    public String getChartDocumentation(
            @ToolParam(description = "图英文名称，如：flowchart、sequenceDiagram、classDiagram、gantt、pie等，可通过getExistingCharts方法获取完整列表")
            String name) {

        if (name == null || name.trim().isEmpty()) {
            return "错误：图名称不能为空，请通过getExistingCharts方法获取可用的图类型列表";
        }

        String chartName = name.trim();
        Map<String, String> availableCharts = getChartMapping();

        if (!availableCharts.containsKey(chartName)) {
            String closestMatch = findClosestMatch(chartName, availableCharts.keySet());
            return String.format("未找到图类型 [%s]，可用的图类型有：%s%s",
                    chartName,
                    availableCharts.keySet(),
                    closestMatch != null ? "。您是否想查询：" + closestMatch + "？" : "");
        }

        try {
            String content = loadResourceContent(chartName);
            if (content == null) {
                return String.format("图类型 [%s] 的说明书文件不存在，请联系管理员添加文档：%s.md", chartName, chartName);
            }

            // 添加文档头部说明
            String chineseName = availableCharts.get(chartName);
            return String.format("# %s (%s) 语法说明书\n\n%s", chineseName, chartName, content);

        } catch (IOException e) {
            return String.format("读取图类型 [%s] 说明书失败：%s。请检查文件是否存在且可读。", chartName, e.getMessage());
        }
    }

    /**
     * 使用当前类的 ClassLoader 加载资源文件
     */
    private String loadResourceContent(String chartName) throws IOException {
        String fileName = resolveFileName(chartName);
        String resourcePath = MERMAID_DOCS_PATH + fileName + ".md";

        // 使用当前类的 ClassLoader
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);

        if (inputStream == null) {
            return null;
        }

        // 读取内容
        try (InputStream is = inputStream;
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private Map<String, String> getChartMapping() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("flowchart", "流程图");
        map.put("sequenceDiagram", "时序图");
        map.put("classDiagram", "类图");
        map.put("stateDiagram", "状态图");
        map.put("entityRelationshipDiagram", "实体关系图");
        map.put("userJourney", "用户旅程图");
        map.put("gantt", "甘特图");
        map.put("pie", "饼图");
        map.put("quadrantChart", "象限图");
        map.put("requirementDiagram", "需求图");
        map.put("gitgraph", "GitGraph图");
        map.put("c4", "C4架构图");
        map.put("mindmap", "思维导图");
        map.put("timeline", "时间线图");
        map.put("zenuml", "ZenUML时序图");
        map.put("sankey", "桑基图");
        map.put("xyChart", "XY坐标图");
        map.put("block", "块图");
        map.put("packet", "数据包图");
        map.put("kanban", "看板图");
        map.put("architecture", "架构图");
        map.put("architecture-beta", "架构图");       // 别名
        map.put("radar", "雷达图");
        map.put("radar-beta", "雷达图");              // 别名
        map.put("treemap", "树状图");
        map.put("treemap-beta", "树状图");            // 别名
        map.put("venn", "维恩图");
        map.put("venn-beta", "维恩图");               // 别名
        map.put("ishikawa", "石川图（因果图）");
        map.put("ishikawa-beta", "石川图（因果图）");   // 别名
        map.put("treeView", "树形视图");
        map.put("treeView-beta", "树形视图");          // 别名
        map.put("eventmodeling", "事件建模图");
        map.put("examples", "示例集合");
        map.put("wardley", "沃德利战略图");
        map.put("wardley-beta", "沃德利战略图");       // 别名
        map.put("swimlanes", "泳道图");
        map.put("swimlane-beta", "泳道图");            // 别名
        map.put("cynefin", "Cynefin框架图");
        map.put("cynefin-beta", "Cynefin框架图");       // 别名
        map.put("railroad", "铁路图");
        map.put("railroad-beta", "铁路图");            // 别名
        map.put("railroad-ebnf-beta", "铁路图(EBNF)");  // 别名
        map.put("railroad-abnf-beta", "铁路图(ABNF)");  // 别名
        map.put("railroad-peg-beta", "铁路图(PEG)");    // 别名
        return map;
    }

    /**
     * 将图类型关键字解析为对应的资源文件名。
     * 例如 radar-beta → radar（对应的 md 文件是 radar.md）
     */
    private static final Map<String, String> KEY_TO_FILENAME = new LinkedHashMap<>();
    static {
        KEY_TO_FILENAME.put("architecture-beta", "architecture");
        KEY_TO_FILENAME.put("radar-beta", "radar");
        KEY_TO_FILENAME.put("treemap-beta", "treemap");
        KEY_TO_FILENAME.put("venn-beta", "venn");
        KEY_TO_FILENAME.put("ishikawa-beta", "ishikawa");
        KEY_TO_FILENAME.put("treeView-beta", "treeView");
        KEY_TO_FILENAME.put("wardley-beta", "wardley");
        KEY_TO_FILENAME.put("swimlane-beta", "swimlanes");
        KEY_TO_FILENAME.put("cynefin-beta", "cynefin");
        KEY_TO_FILENAME.put("railroad-beta", "railroad");
        KEY_TO_FILENAME.put("railroad-ebnf-beta", "railroad");
        KEY_TO_FILENAME.put("railroad-abnf-beta", "railroad");
        KEY_TO_FILENAME.put("railroad-peg-beta", "railroad");
    }

    private String resolveFileName(String chartName) {
        return KEY_TO_FILENAME.getOrDefault(chartName, chartName);
    }

    /**
     * 简单的字符串匹配，找到最相似的图表名称
     */
    private String findClosestMatch(String input, Iterable<String> candidates) {
        String closest = null;
        int minDistance = Integer.MAX_VALUE;

        for (String candidate : candidates) {
            int distance = levenshteinDistance(input.toLowerCase(), candidate.toLowerCase());
            if (distance < minDistance && distance <= 3) {
                minDistance = distance;
                closest = candidate;
            }
        }
        return closest;
    }

    /**
     * 计算编辑距离（Levenshtein Distance）
     */
    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }

        return dp[a.length()][b.length()];
    }
}