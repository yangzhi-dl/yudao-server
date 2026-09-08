package cn.iocoder.yudao.module.hub.core.market.tools;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.core.tools.factory.AITool;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAgentDetailRespVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAgentPageReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAgentRespVO;
import cn.iocoder.yudao.module.hub.core.market.service.HubMarketAgentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 供市场选型助手调用的实时目录工具。
 *
 * <p>工具只通过市场公开服务读取已发布智能体，不暴露来源租户、来源智能体编号等内部字段。</p>
 */
@Component
@RequiredArgsConstructor
public class MarketAgentCatalogTool implements AITool {

    /** 单次读取数量与 PageParam 的最大值保持一致；通过翻页确保目录完整。 */
    private static final int CATALOG_PAGE_SIZE = 200;
    private static final int MAX_RECOMMENDATION_RANK = 3;

    private final HubMarketAgentService marketAgentService;

    @Override
    public Object getToolInstance() {
        return this;
    }

    @Override
    public String getName() {
        return "market-agent-catalog";
    }

    @Override
    public String getTitle() {
        return "市场目录查询";
    }

    @Override
    public String getDescription() {
        return "实时查询已发布的智能体市场目录，供选型助手按用户业务需求进行推荐。";
    }

    @Tool(description = "读取当前智能体市场中全部已发布智能体的精简选型目录。"
            + "目录包含智能体编号、名称、摘要、分类、标签、适用场景标题、核心能力标题和试用状态。"
            + "必须先调用本工具，再根据用户需求自行理解并选择最多 3 个最匹配的智能体；"
            + "不得只按名称中的相同关键词判断，也不得编造目录中不存在的智能体。")
    public String searchPublishedMarketAgents() {
        return JsonUtils.toJsonString(toSelectionCatalog(getPublishedCatalog()));
    }

    @Tool(description = "在精简市场目录不足以判断时，获取单个智能体的完整介绍、功能、适用场景和试用状态。"
            + "只能传入市场目录查询工具返回的市场编号；不要对每个目录项都调用本工具。")
    public String getPublishedMarketAgentDetail(
            @ToolParam(description = "市场智能体编号，必须来自市场目录查询结果") Long marketId) {
        if (marketId == null) {
            return "市场智能体编号不能为空。";
        }
        HubMarketAgentDetailRespVO detail = marketAgentService.getPublishedAgent(marketId);
        StringBuilder result = new StringBuilder();
        result.append("名称：").append(detail.getName()).append('\n');
        appendIfPresent(result, "摘要", detail.getSummary());
        appendIfPresent(result, "分类", detail.getCategoryName());
        if (detail.getTagNames() != null && !detail.getTagNames().isEmpty()) {
            result.append("标签：").append(String.join("、", detail.getTagNames())).append('\n');
        }
        appendIfPresent(result, "介绍", detail.getIntroduction());
        appendTextItems(result, "适用场景", detail.getScenarios());
        appendTextItems(result, "核心功能", detail.getFeatures());
        result.append("是否可试用：").append(Boolean.TRUE.equals(detail.getTrialEnabled()) ? "是" : "否")
                .append('\n');
        result.append("详情页：[/hub/market/").append(detail.getId()).append("](/hub/market/")
                .append(detail.getId()).append(")");
        return result.toString();
    }

    @Tool(description = "为已选定的市场智能体生成前端推荐卡片。"
            + "仅在已完成市场目录查询后调用；每一个最终推荐的智能体都必须调用一次，最多推荐 3 个。"
            + "返回的数据将直接展示给用户，因此匹配原因必须具体说明该智能体为什么符合用户需求。")
    public String buildRecommendationCard(
            @ToolParam(description = "市场智能体编号，必须来自市场目录查询结果") Long marketId,
            @ToolParam(description = "该智能体与用户需求的具体匹配原因，限 80 字以内") String matchReason,
            @ToolParam(description = "推荐排序，从 1 开始，最大为 3") Integer rank) {
        if (marketId == null) {
            return "市场智能体编号不能为空。";
        }
        HubMarketAgentDetailRespVO detail = marketAgentService.getPublishedAgent(marketId);
        MarketRecommendationCard card = new MarketRecommendationCard();
        card.setType("market-recommendation");
        card.setMarketId(detail.getId());
        card.setRank(normalizeRecommendationRank(rank));
        card.setName(detail.getName());
        card.setSummary(detail.getSummary());
        card.setCoverUrl(detail.getCoverUrl());
        card.setCategoryName(detail.getCategoryName());
        card.setTagNames(detail.getTagNames());
        card.setTrialEnabled(Boolean.TRUE.equals(detail.getTrialEnabled()));
        card.setAgentType(detail.getAgentType());
        card.setMatchReason(StringUtils.hasText(matchReason) ? matchReason.trim() : "与您的业务需求相匹配。");
        return JsonUtils.toJsonString(card);
    }

    private int normalizeRecommendationRank(Integer rank) {
        if (rank == null || rank <= 0) {
            return 1;
        }
        return Math.min(rank, MAX_RECOMMENDATION_RANK);
    }

    private List<HubMarketAgentDetailRespVO> getPublishedCatalog() {
        List<HubMarketAgentRespVO> agents = new ArrayList<>();
        long total;
        int pageNo = 1;
        do {
            HubMarketAgentPageReqVO reqVO = new HubMarketAgentPageReqVO();
            reqVO.setPageNo(pageNo++);
            reqVO.setPageSize(CATALOG_PAGE_SIZE);
            PageResult<HubMarketAgentRespVO> pageResult = marketAgentService.getPublishedAgentPage(reqVO);
            agents.addAll(pageResult.getList());
            total = pageResult.getTotal();
        } while (agents.size() < total);
        return agents.stream()
                .map(HubMarketAgentRespVO::getId)
                .map(marketAgentService::getPublishedAgent)
                .toList();
    }

    /**
     * 目录结果会直接进入模型上下文，因此只保留首轮选型所需的字段。
     * 介绍、长说明和详情页链接会显著增加 Token；需要进一步核实时再调用详情工具。
     */
    private MarketSelectionCatalog toSelectionCatalog(List<HubMarketAgentDetailRespVO> agents) {
        List<HubMarketAgentDetailRespVO> sourceAgents = agents == null ? List.of() : agents;
        MarketSelectionCatalog catalog = new MarketSelectionCatalog();
        catalog.setTotal(sourceAgents.size());
        catalog.setAgents(sourceAgents.stream().map(agent -> {
            MarketSelectionItem item = new MarketSelectionItem();
            item.setId(agent.getId());
            item.setName(agent.getName());
            item.setSummary(agent.getSummary());
            item.setCategory(agent.getCategoryName());
            item.setTags(agent.getTagNames());
            item.setScenarios(getItemTitles(agent.getScenarios()));
            item.setCapabilities(getItemTitles(agent.getFeatures()));
            item.setTrialEnabled(Boolean.TRUE.equals(agent.getTrialEnabled()));
            return item;
        }).toList());
        return catalog;
    }

    private List<String> getItemTitles(List<HubMarketAgentDetailRespVO.TextItemVO> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream()
                .map(HubMarketAgentDetailRespVO.TextItemVO::getTitle)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .toList();
    }

    private void appendIfPresent(StringBuilder result, String label, String value) {
        if (StringUtils.hasText(value)) {
            result.append(label).append("：").append(value.trim()).append('\n');
        }
    }

    private void appendTextItems(StringBuilder result, String label,
                                 List<HubMarketAgentDetailRespVO.TextItemVO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        result.append(label).append("：");
        for (int index = 0; index < items.size(); index++) {
            HubMarketAgentDetailRespVO.TextItemVO item = items.get(index);
            if (index > 0) {
                result.append("；");
            }
            result.append(item.getTitle());
            if (StringUtils.hasText(item.getDescription())) {
                result.append("（").append(item.getDescription().trim()).append("）");
            }
        }
        result.append('\n');
    }

    /** 模型首轮选型使用的精简市场目录。 */
    @Data
    private static class MarketSelectionCatalog {

        private Integer total;
        private List<MarketSelectionItem> agents;

    }

    /** 单个市场智能体的选型字段，避免将详情长文重复传入模型上下文。 */
    @Data
    private static class MarketSelectionItem {

        private Long id;
        private String name;
        private String summary;
        private String category;
        private List<String> tags;
        private List<String> scenarios;
        private List<String> capabilities;
        private Boolean trialEnabled;

    }

    /** 选型助手工具结果中的前端展示卡片。 */
    @Data
    private static class MarketRecommendationCard {

        private String type;
        private Long marketId;
        private Integer rank;
        private String name;
        private String summary;
        private String coverUrl;
        private String categoryName;
        private List<String> tagNames;
        private Boolean trialEnabled;
        private Integer agentType;
        private String matchReason;

    }

}
