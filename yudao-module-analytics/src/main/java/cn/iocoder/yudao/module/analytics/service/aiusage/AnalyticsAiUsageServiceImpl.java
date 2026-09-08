package cn.iocoder.yudao.module.analytics.service.aiusage;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.analytics.controller.admin.aiusage.vo.AnalyticsAiUsageSummaryRespVO;
import cn.iocoder.yudao.module.analytics.controller.admin.aiusage.vo.AnalyticsAiUsageSummaryRespVO.AnalyticsAiTokenTrendRespVO;
import cn.iocoder.yudao.module.analytics.controller.admin.vo.AnalyticsNameCountRespVO;
import cn.iocoder.yudao.module.analytics.controller.admin.vo.AnalyticsTrendRespVO;
import cn.iocoder.yudao.module.analytics.dal.mysql.aiusage.AnalyticsAiUsageMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 使用分析 Service 实现类
 *
 * 通过 @TenantIgnore 关闭租户过滤，统计全部租户（平台管理员视角）。
 */
@Service
@Slf4j
public class AnalyticsAiUsageServiceImpl implements AnalyticsAiUsageService {

    @Resource
    private AnalyticsAiUsageMapper aiUsageMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int TOP_LIMIT = 10;

    @Override
    @TenantIgnore
    public AnalyticsAiUsageSummaryRespVO getSummary(Integer days) {
        AnalyticsAiUsageSummaryRespVO summary = new AnalyticsAiUsageSummaryRespVO();
        summary.setTotalDialogue(defaultCount(aiUsageMapper.selectDialogueCount()));
        summary.setTotalTopic(defaultCount(aiUsageMapper.selectTopicCount()));
        summary.setTotalCall(defaultCount(aiUsageMapper.selectTokenCallCount()));
        summary.setTotalToken(defaultCount(aiUsageMapper.selectTokenTotal()));
        summary.setModelTokenTop(groupByName(aiUsageMapper.selectModelTokenTop(TOP_LIMIT)));
        summary.setModelCallTop(groupByName(aiUsageMapper.selectModelCallTop(TOP_LIMIT)));

        LocalDate endDate = LocalDate.now().plusDays(1);
        LocalDate startDate = endDate.minusDays(days);
        summary.setDialogueTrend(fillTrend(aiUsageMapper.selectDialogueDailyTrend(startDate.atStartOfDay()), startDate, endDate));
        summary.setTopicTrend(fillTrend(aiUsageMapper.selectTopicDailyTrend(startDate.atStartOfDay()), startDate, endDate));
        summary.setTokenTrend(fillTokenTrend(aiUsageMapper.selectTokenDailyTrend(startDate.atStartOfDay()), startDate, endDate));
        return summary;
    }

    // ========== 工具方法 ==========

    private List<AnalyticsTrendRespVO> fillTrend(List<Map<String, Object>> rows, LocalDate start, LocalDate end) {
        Map<String, Long> dateMap = new LinkedHashMap<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                dateMap.put(asString(row.get("date")), toLong(row.get("cnt")));
            }
        }
        List<AnalyticsTrendRespVO> result = new ArrayList<>();
        for (LocalDate d = start; d.isBefore(end); d = d.plusDays(1)) {
            String date = d.format(FMT);
            result.add(new AnalyticsTrendRespVO(date, dateMap.getOrDefault(date, 0L)));
        }
        return result;
    }

    private List<AnalyticsAiTokenTrendRespVO> fillTokenTrend(List<Map<String, Object>> rows, LocalDate start, LocalDate end) {
        Map<String, Long> dateMap = new LinkedHashMap<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                dateMap.put(asString(row.get("date")), toLong(row.get("total")));
            }
        }
        List<AnalyticsAiTokenTrendRespVO> result = new ArrayList<>();
        for (LocalDate d = start; d.isBefore(end); d = d.plusDays(1)) {
            String date = d.format(FMT);
            AnalyticsAiTokenTrendRespVO vo = new AnalyticsAiTokenTrendRespVO();
            vo.setDate(date);
            vo.setToken(dateMap.getOrDefault(date, 0L));
            result.add(vo);
        }
        return result;
    }

    private List<AnalyticsNameCountRespVO> groupByName(List<Map<String, Object>> rows) {
        List<AnalyticsNameCountRespVO> list = new ArrayList<>();
        if (rows == null) {
            return list;
        }
        for (Map<String, Object> row : rows) {
            list.add(new AnalyticsNameCountRespVO(asString(row.get("name")), toLong(row.get("cnt"))));
        }
        return list;
    }

    private Long defaultCount(Long count) {
        return count == null ? 0L : count;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        return value instanceof Number ? ((Number) value).longValue() : Long.valueOf(value.toString());
    }

    private String asString(Object value) {
        return value == null ? "" : value.toString();
    }

}