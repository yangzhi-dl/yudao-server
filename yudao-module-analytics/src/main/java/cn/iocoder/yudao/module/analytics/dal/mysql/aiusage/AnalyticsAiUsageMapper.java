package cn.iocoder.yudao.module.analytics.dal.mysql.aiusage;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI 使用分析 Mapper
 *
 * 注意：涉及跨租户统计，所有方法均在 Service 层通过 @TenantIgnore 忽略租户过滤。
 */
@Mapper
public interface AnalyticsAiUsageMapper {

    Long selectDialogueCount();

    Long selectTopicCount();

    Long selectTokenTotal();

    Long selectTokenCallCount();

    List<Map<String, Object>> selectDialogueDailyTrend(@Param("beginTime") LocalDateTime beginTime);

    List<Map<String, Object>> selectTopicDailyTrend(@Param("beginTime") LocalDateTime beginTime);

    List<Map<String, Object>> selectTokenDailyTrend(@Param("beginTime") LocalDateTime beginTime);

    List<Map<String, Object>> selectModelTokenTop(@Param("limit") Integer limit);

    List<Map<String, Object>> selectModelCallTop(@Param("limit") Integer limit);

}