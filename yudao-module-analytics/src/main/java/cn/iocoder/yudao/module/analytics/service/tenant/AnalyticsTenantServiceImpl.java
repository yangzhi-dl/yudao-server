package cn.iocoder.yudao.module.analytics.service.tenant;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.analytics.controller.admin.tenant.vo.AnalyticsTenantSummaryRespVO;
import cn.iocoder.yudao.module.analytics.controller.admin.vo.AnalyticsKeyCountRespVO;
import cn.iocoder.yudao.module.analytics.controller.admin.vo.AnalyticsNameCountRespVO;
import cn.iocoder.yudao.module.analytics.controller.admin.vo.AnalyticsTrendRespVO;
import cn.iocoder.yudao.module.analytics.dal.mysql.tenant.AnalyticsTenantMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 租户运营分析 Service 实现类
 *
 * 通过 @TenantIgnore 关闭租户过滤，统计全部租户（平台管理员视角）。
 */
@Service
@Slf4j
public class AnalyticsTenantServiceImpl implements AnalyticsTenantService {

    @Resource
    private AnalyticsTenantMapper analyticsTenantMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @TenantIgnore
    public AnalyticsTenantSummaryRespVO getSummary(Integer days) {
        AnalyticsTenantSummaryRespVO summary = new AnalyticsTenantSummaryRespVO();
        summary.setTotalCount(defaultCount(analyticsTenantMapper.selectCount()));
        summary.setEnabledCount(defaultCount(analyticsTenantMapper.selectCountByStatus(0)));
        summary.setDisabledCount(defaultCount(analyticsTenantMapper.selectCountByStatus(1)));
        summary.setExpiringSoonCount(defaultCount(analyticsTenantMapper.selectExpiringSoonCount()));
        summary.setStatusDist(groupByKey(analyticsTenantMapper.selectCountGroupByStatus()));
        summary.setPackageDist(groupByName(analyticsTenantMapper.selectCountGroupByPackage()));

        LocalDate endDate = LocalDate.now().plusDays(1);
        LocalDate startDate = endDate.minusDays(days);
        summary.setTrend(fillTrend(analyticsTenantMapper.selectDailyTrend(startDate.atStartOfDay()), startDate, endDate));

        summary.setExpiringTenants(buildExpiring(analyticsTenantMapper.selectExpiringTenants()));
        return summary;
    }

    // ========== 工具方法 ==========

    private List<AnalyticsTenantSummaryRespVO.AnalyticsTenantExpiringRespVO> buildExpiring(
            List<Map<String, Object>> rows) {
        List<AnalyticsTenantSummaryRespVO.AnalyticsTenantExpiringRespVO> list = new ArrayList<>();
        if (rows == null) {
            return list;
        }
        for (Map<String, Object> row : rows) {
            AnalyticsTenantSummaryRespVO.AnalyticsTenantExpiringRespVO vo =
                    new AnalyticsTenantSummaryRespVO.AnalyticsTenantExpiringRespVO();
            vo.setTenantId(toLong(row.get("tenantId")));
            vo.setTenantName(asString(row.get("tenantName")));
            vo.setContactName(asString(row.get("contactName")));
            vo.setExpireTime((LocalDateTime) row.get("expireTime"));
            list.add(vo);
        }
        return list;
    }

    private List<AnalyticsKeyCountRespVO> groupByKey(List<Map<String, Object>> rows) {
        List<AnalyticsKeyCountRespVO> list = new ArrayList<>();
        if (rows == null) {
            return list;
        }
        for (Map<String, Object> row : rows) {
            list.add(new AnalyticsKeyCountRespVO(toInt(row.get("key")), toLong(row.get("cnt"))));
        }
        return list;
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

    /** 按日期补全缺失天数，返回趋势列表 */
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

    private Long defaultCount(Long count) {
        return count == null ? 0L : count;
    }

    private Integer toInt(Object value) {
        return value == null ? null : ((Number) value).intValue();
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