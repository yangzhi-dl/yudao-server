package cn.iocoder.yudao.module.analytics.dal.mysql.tenant;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 租户运营分析 Mapper
 *
 * 注意：涉及跨租户统计，所有方法均在 Service 层通过 @TenantIgnore 忽略租户过滤。
 */
@Mapper
public interface AnalyticsTenantMapper {

    Long selectCount();

    Long selectCountByStatus(@Param("status") Integer status);

    Long selectExpiringSoonCount();

    List<Map<String, Object>> selectCountGroupByStatus();

    List<Map<String, Object>> selectCountGroupByPackage();

    List<Map<String, Object>> selectDailyTrend(@Param("beginTime") LocalDateTime beginTime);

    List<Map<String, Object>> selectExpiringTenants();

}