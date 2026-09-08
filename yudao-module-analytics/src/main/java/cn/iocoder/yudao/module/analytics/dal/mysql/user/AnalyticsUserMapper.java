package cn.iocoder.yudao.module.analytics.dal.mysql.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户与登录分析 Mapper
 *
 * 注意：涉及跨租户统计，所有方法均在 Service 层通过 @TenantIgnore 忽略租户过滤。
 */
@Mapper
public interface AnalyticsUserMapper {

    Long selectUserCount();

    Long selectUserCountByStatus(@Param("status") Integer status);

    /** 在线用户数：访问令牌未过期即视为在线 */
    Long selectOnlineUserCount();

    List<Map<String, Object>> selectUserDailyTrend(@Param("beginTime") LocalDateTime beginTime);

    /** 每日登录次数与成功次数（仅登录类日志） */
    List<Map<String, Object>> selectLoginDailyTrend(@Param("beginTime") LocalDateTime beginTime);

    List<Map<String, Object>> selectLoginCountGroupByType();

    List<Map<String, Object>> selectLoginCountGroupByResult();

}