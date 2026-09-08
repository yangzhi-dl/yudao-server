package cn.iocoder.yudao.module.analytics.service.user;

import cn.iocoder.yudao.module.analytics.controller.admin.user.vo.AnalyticsUserSummaryRespVO;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.analytics.controller.admin.user.vo.AnalyticsUserSummaryRespVO.AnalyticsLoginTrendRespVO;
import cn.iocoder.yudao.module.analytics.controller.admin.vo.AnalyticsKeyCountRespVO;
import cn.iocoder.yudao.module.analytics.controller.admin.vo.AnalyticsTrendRespVO;
import cn.iocoder.yudao.module.analytics.dal.mysql.user.AnalyticsUserMapper;
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
 * 用户与登录分析 Service 实现类
 *
 * 通过 @TenantIgnore 关闭租户过滤，统计全部租户（平台管理员视角）。
 */
@Service
@Slf4j
public class AnalyticsUserServiceImpl implements AnalyticsUserService {

    @Resource
    private AnalyticsUserMapper userMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @TenantIgnore
    public AnalyticsUserSummaryRespVO getSummary(Integer days) {
        AnalyticsUserSummaryRespVO summary = new AnalyticsUserSummaryRespVO();
        summary.setTotalUser(defaultCount(userMapper.selectUserCount()));
        summary.setEnabledUser(defaultCount(userMapper.selectUserCountByStatus(0)));
        summary.setDisabledUser(defaultCount(userMapper.selectUserCountByStatus(1)));
        summary.setOnlineUser(defaultCount(userMapper.selectOnlineUserCount()));
        summary.setLoginTypeDist(groupByKey(userMapper.selectLoginCountGroupByType()));
        summary.setResultDist(groupByKey(userMapper.selectLoginCountGroupByResult()));

        LocalDate endDate = LocalDate.now().plusDays(1);
        LocalDate startDate = endDate.minusDays(days);
        summary.setUserTrend(fillUserTrend(userMapper.selectUserDailyTrend(startDate.atStartOfDay()), startDate, endDate));
        summary.setLoginTrend(fillLoginTrend(userMapper.selectLoginDailyTrend(startDate.atStartOfDay()), startDate, endDate));
        return summary;
    }

    // ========== 工具方法 ==========

    private List<AnalyticsTrendRespVO> fillUserTrend(List<Map<String, Object>> rows, LocalDate start, LocalDate end) {
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

    private List<AnalyticsLoginTrendRespVO> fillLoginTrend(List<Map<String, Object>> rows, LocalDate start, LocalDate end) {
        Map<String, Map<String, Object>> dateMap = new LinkedHashMap<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                dateMap.put(asString(row.get("date")), row);
            }
        }
        List<AnalyticsLoginTrendRespVO> result = new ArrayList<>();
        for (LocalDate d = start; d.isBefore(end); d = d.plusDays(1)) {
            String date = d.format(FMT);
            AnalyticsLoginTrendRespVO vo = new AnalyticsLoginTrendRespVO();
            vo.setDate(date);
            Map<String, Object> row = dateMap.get(date);
            vo.setLoginCount(row == null ? 0L : toLong(row.get("loginCnt")));
            vo.setSuccessCount(row == null ? 0L : toLong(row.get("successCnt")));
            result.add(vo);
        }
        return result;
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