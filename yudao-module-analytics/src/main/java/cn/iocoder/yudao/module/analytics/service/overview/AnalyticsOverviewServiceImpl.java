package cn.iocoder.yudao.module.analytics.service.overview;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.analytics.controller.admin.overview.vo.AnalyticsOverviewSummaryRespVO;
import cn.iocoder.yudao.module.analytics.controller.admin.overview.vo.AnalyticsOverviewTenantStatsRespVO;
import cn.iocoder.yudao.module.analytics.dal.mysql.overview.AnalyticsOverviewMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 平台分析总览 Service 实现类
 *
 * 所有统计均为跨租户的全局统计，通过 @TenantIgnore 关闭租户过滤后，按 tenant_id 聚类。
 */
@Service
@Slf4j
public class AnalyticsOverviewServiceImpl implements AnalyticsOverviewService {

    @Resource
    private AnalyticsOverviewMapper overviewMapper;

    /** 各业务表名 */
    private static final String TABLE_SYSTEM_USERS = "system_users";
    private static final String TABLE_SYSTEM_DEPT = "system_dept";
    private static final String TABLE_SYSTEM_POST = "system_post";
    private static final String TABLE_AI_MODEL = "ai_chat_models";
    private static final String TABLE_AI_WIKI = "ai_wiki";
    private static final String TABLE_AI_AGENT = "ai_chat_agents";
    private static final String TABLE_AI_DOCUMENT = "ai_document";
    private static final String TABLE_INFRA_FILE = "infra_file";
    private static final String TABLE_INFRA_JOB = "infra_job";

    @Override
    @TenantIgnore
    public AnalyticsOverviewSummaryRespVO getSummary() {
        AnalyticsOverviewSummaryRespVO summary = new AnalyticsOverviewSummaryRespVO();
        summary.setUserCount(defaultCount(overviewMapper.selectCount(TABLE_SYSTEM_USERS)));
        summary.setDeptCount(defaultCount(overviewMapper.selectCount(TABLE_SYSTEM_DEPT)));
        summary.setPostCount(defaultCount(overviewMapper.selectCount(TABLE_SYSTEM_POST)));
        summary.setModelCount(defaultCount(overviewMapper.selectCount(TABLE_AI_MODEL)));
        summary.setKnowledgeCount(defaultCount(overviewMapper.selectCount(TABLE_AI_WIKI)));
        summary.setAgentCount(defaultCount(overviewMapper.selectCount(TABLE_AI_AGENT)));
        summary.setDocumentCount(defaultCount(overviewMapper.selectCount(TABLE_AI_DOCUMENT)));
        summary.setTokenUsage(defaultCount(overviewMapper.selectTokenTotal()));
        summary.setFileCount(defaultCount(overviewMapper.selectCount(TABLE_INFRA_FILE)));
        summary.setJobCount(defaultCount(overviewMapper.selectCount(TABLE_INFRA_JOB)));
        summary.setTenantCount(defaultCount(overviewMapper.selectCount("system_tenant")));
        return summary;
    }

    @Override
    @TenantIgnore
    public List<AnalyticsOverviewTenantStatsRespVO> getTenantStats() {
        // 1. 租户列表（保证没有数据的租户也展示）
        Map<Long, String> tenantNames = new LinkedHashMap<>();
        for (Map<String, Object> tenant : overviewMapper.selectTenants()) {
            tenantNames.put(toLong(tenant.get("tenantId")), String.valueOf(tenant.get("tenantName")));
        }
        // 2. 各维度按租户聚类
        Map<Long, Long> userMap = groupCount(overviewMapper.selectCountGroupByTenant(TABLE_SYSTEM_USERS));
        Map<Long, Long> deptMap = groupCount(overviewMapper.selectCountGroupByTenant(TABLE_SYSTEM_DEPT));
        Map<Long, Long> modelMap = groupCount(overviewMapper.selectCountGroupByTenant(TABLE_AI_MODEL));
        Map<Long, Long> wikiMap = groupCount(overviewMapper.selectCountGroupByTenant(TABLE_AI_WIKI));
        Map<Long, Long> agentMap = groupCount(overviewMapper.selectCountGroupByTenant(TABLE_AI_AGENT));
        Map<Long, Long> documentMap = groupCount(overviewMapper.selectCountGroupByTenant(TABLE_AI_DOCUMENT));
        Map<Long, Long> tokenMap = groupToken(overviewMapper.selectTokenGroupByTenant());
        // 3. 组装 VO 列表
        List<AnalyticsOverviewTenantStatsRespVO> result = new ArrayList<>();
        tenantNames.forEach((tenantId, tenantName) -> {
            AnalyticsOverviewTenantStatsRespVO vo = new AnalyticsOverviewTenantStatsRespVO();
            vo.setTenantId(tenantId);
            vo.setTenantName(tenantName);
            vo.setUserCount(userMap.getOrDefault(tenantId, 0L));
            vo.setDeptCount(deptMap.getOrDefault(tenantId, 0L));
            vo.setModelCount(modelMap.getOrDefault(tenantId, 0L));
            vo.setKnowledgeCount(wikiMap.getOrDefault(tenantId, 0L));
            vo.setAgentCount(agentMap.getOrDefault(tenantId, 0L));
            vo.setDocumentCount(documentMap.getOrDefault(tenantId, 0L));
            vo.setTokenUsage(tokenMap.getOrDefault(tenantId, 0L));
            result.add(vo);
        });
        return result;
    }

    // ========== 工具方法 ==========

    private Long defaultCount(Long count) {
        return count == null ? 0L : count;
    }

    private Map<Long, Long> groupCount(List<Map<String, Object>> rows) {
        Map<Long, Long> result = new LinkedHashMap<>();
        if (rows == null) {
            return result;
        }
        for (Map<String, Object> row : rows) {
            result.put(toLong(row.get("tenantId")), toLong(row.get("count")));
        }
        return result;
    }

    private Map<Long, Long> groupToken(List<Map<String, Object>> rows) {
        Map<Long, Long> result = new LinkedHashMap<>();
        if (rows == null) {
            return result;
        }
        for (Map<String, Object> row : rows) {
            result.put(toLong(row.get("tenantId")), toLong(row.get("token")));
        }
        return result;
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