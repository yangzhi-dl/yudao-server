package cn.iocoder.yudao.module.ai.core.tools;

import cn.iocoder.yudao.module.ai.core.tools.factory.AITool;
import cn.iocoder.yudao.module.ai.search.model.SearchResponse;
import cn.iocoder.yudao.module.ai.search.service.SearchService;
import cn.iocoder.yudao.module.ai.search.service.SearchServiceManager;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WebSearchTool implements AITool {

    private final SearchServiceManager searchServiceManager;

    /** 工具是否启用，配置于 application.yaml 的 iims.tools.web-search.enabled，默认启用 */
    @Value("${iims.tools.web-search.enabled:true}")
    private boolean enabled;

    @Override
    public Object getToolInstance() {
        return this;
    }

    @Override
    public String getName() {
        return "web-search-tools";
    }

    @Override
    public String getTitle() {
        return "网络搜索引擎";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getDescription() {
        return "根据输入的关键词来检索对应的网络结果";
    }

    @Tool(description = "使用指定搜索引擎搜索网页信息")
    public String searchWebWithEngines(@ToolParam(description = "搜索的内容（不能输入关键词，例如：xxx xxx xxx）") String query,
                                       @ToolParam(description = "相似度") Double score, @ToolParam(description = "加载查询的数量") Integer loadingSize,
                                       @ToolParam(description = "搜索引擎：baidu, tavily (只能选择一个)") String engines) {
        try {
            // 获取服务并执行搜索
            SearchService searchService = searchServiceManager.getServiceByType(engines);
            if (searchService == null) {
                return "搜索失败：未找到可用的搜索引擎: " + engines;
            }

            SearchResponse response = searchService.search(query);
            if (response == null) {
                return "搜索失败：未获取到结果";
            }

            // 格式化返回结果
            return formatSearchResult(response, score, loadingSize);

        } catch (Exception e) {
            return "搜索失败：" + e.getMessage();
        }
    }

    private String formatSearchResult(SearchResponse response, Double score, Integer loadingSize) {
        StringBuilder sb = new StringBuilder();
        if (response.getResults() != null && !response.getResults().isEmpty()) {
            // 只返回前10条结果，避免内容过长
            List<SearchResponse.Result> resultStream = response.getResults().stream()
                    .filter(result -> {
                        Double resultScore = result.getScore();
                        if (resultScore == null) {
                            return true; // 或根据业务需求决定是否包含
                        }
                        return resultScore >= score;
                    }).toList();
            int limit = Math.min(loadingSize, resultStream.size());
            for (int i = 0; i < limit; i++) {
                SearchResponse.Result result = resultStream.get(i);
                sb.append(i + 1).append(". ").append(result.getTitle()).append("\n");
                sb.append(String.format("链接：[%s](%s)", i + 1, result.getUrl())).append("\n");
                sb.append("内容：").append(result.getContent()).append("\n");
                sb.append("\n");
            }

            if (resultStream.size() > limit) {
                sb.append("... 还有 ").append(resultStream.size() - limit).append(" 条结果未显示\n");
            }
        } else {
            sb.append("未找到相关结果\n");
        }

        String result = sb.toString();
        return StringUtils.isNoneBlank(result) ? result : "未找到相关结果";
    }
}