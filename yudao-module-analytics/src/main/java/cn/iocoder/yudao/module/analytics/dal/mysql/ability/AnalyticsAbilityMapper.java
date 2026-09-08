package cn.iocoder.yudao.module.analytics.dal.mysql.ability;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * AI 能力分析 Mapper
 *
 * 注意：涉及跨租户统计，所有方法均在 Service 层通过 @TenantIgnore 忽略租户过滤。
 */
@Mapper
public interface AnalyticsAbilityMapper {

    /**
     * 统计指定表的记录总数（忽略逻辑删除）
     */
    Long selectCount(@Param("tableName") String tableName);

    /**
     * 统计 token 使用总量
     */
    Long selectTokenTotal();

    /**
     * 按指定枚举列统计分布（返回 map：key -> count）
     */
    List<Map<String, Object>> selectCountGroupByColumn(@Param("tableName") String tableName,
                                                       @Param("column") String column);

}