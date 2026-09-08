package cn.iocoder.yudao.module.analytics.service.ability;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.analytics.controller.admin.ability.vo.AnalyticsAbilityKeyCountRespVO;
import cn.iocoder.yudao.module.analytics.controller.admin.ability.vo.AnalyticsAbilitySummaryRespVO;
import cn.iocoder.yudao.module.analytics.dal.mysql.ability.AnalyticsAbilityMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI 能力分析 Service 实现类
 *
 * 所有统计均为跨租户的全局统计，通过 @TenantIgnore 关闭租户过滤。
 */
@Service
@Slf4j
public class AnalyticsAbilityServiceImpl implements AnalyticsAbilityService {

    @Resource
    private AnalyticsAbilityMapper abilityMapper;

    /** 各业务表名 */
    private static final String TABLE_AI_MODEL = "ai_chat_models";
    private static final String TABLE_AI_AGENT = "ai_chat_agents";
    private static final String TABLE_AI_TOOL = "ai_chat_tools";
    private static final String TABLE_AI_SKILL = "ai_skill";
    private static final String TABLE_AI_WIKI = "ai_wiki";
    private static final String TABLE_AI_DOCUMENT = "ai_document";
    private static final String TABLE_AI_WORKFLOW = "ai_workflow";

    @Override
    @TenantIgnore
    public AnalyticsAbilitySummaryRespVO getSummary() {
        AnalyticsAbilitySummaryRespVO summary = new AnalyticsAbilitySummaryRespVO();
        summary.setModelCount(defaultCount(abilityMapper.selectCount(TABLE_AI_MODEL)));
        summary.setAgentCount(defaultCount(abilityMapper.selectCount(TABLE_AI_AGENT)));
        summary.setToolCount(defaultCount(abilityMapper.selectCount(TABLE_AI_TOOL)));
        summary.setSkillCount(defaultCount(abilityMapper.selectCount(TABLE_AI_SKILL)));
        summary.setKnowledgeCount(defaultCount(abilityMapper.selectCount(TABLE_AI_WIKI)));
        summary.setDocumentCount(defaultCount(abilityMapper.selectCount(TABLE_AI_DOCUMENT)));
        summary.setWorkflowCount(defaultCount(abilityMapper.selectCount(TABLE_AI_WORKFLOW)));
        summary.setTokenUsage(defaultCount(abilityMapper.selectTokenTotal()));
        // 各类分布
        summary.setModelApiTypeDist(groupBy(TABLE_AI_MODEL, "`type`"));
        summary.setModelTypeDist(groupBy(TABLE_AI_MODEL, "model_type"));
        summary.setModelOnlineDist(groupBy(TABLE_AI_MODEL, "is_online"));
        summary.setAgentTypeDist(groupBy(TABLE_AI_AGENT, "`type`"));
        summary.setToolTypeDist(groupBy(TABLE_AI_TOOL, "`type`"));
        return summary;
    }

    // ========== 工具方法 ==========

    private Long defaultCount(Long count) {
        return count == null ? 0L : count;
    }

    private List<AnalyticsAbilityKeyCountRespVO> groupBy(String tableName, String column) {
        List<AnalyticsAbilityKeyCountRespVO> result = new ArrayList<>();
        List<Map<String, Object>> rows = abilityMapper.selectCountGroupByColumn(tableName, column);
        if (rows == null) {
            return result;
        }
        for (Map<String, Object> row : rows) {
            result.add(new AnalyticsAbilityKeyCountRespVO(toInt(row.get("key")), toLong(row.get("count"))));
        }
        return result;
    }

    private Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.valueOf(value.toString());
    }

    private Long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.valueOf(value.toString());
    }

}