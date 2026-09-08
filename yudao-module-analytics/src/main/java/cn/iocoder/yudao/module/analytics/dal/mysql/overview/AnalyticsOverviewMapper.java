package cn.iocoder.yudao.module.analytics.dal.mysql.overview;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 平台分析总览 Mapper
 *
 * 注意：涉及跨租户统计，所有方法均在 Service 层通过 @TenantIgnore 忽略租户过滤后，按 tenant_id 聚类。
 */
@Mapper
public interface AnalyticsOverviewMapper {

    /**
     * 统计指定表的记录总数（忽略逻辑删除）
     */
    Long selectCount(@Param("tableName") String tableName);

    /**
     * 按租户统计指定表记录数（返回 map：tenantId -> count）
     */
    List<Map<String, Object>> selectCountGroupByTenant(@Param("tableName") String tableName);

    /**
     * 统计 token 使用总量
     */
    Long selectTokenTotal();

    /**
     * 按租户统计 token 使用量（返回 map：tenantId -> token）
     */
    List<Map<String, Object>> selectTokenGroupByTenant();

    /**
     * 查询全部租户（返回 map：tenantId -> tenantName）
     */
    List<Map<String, Object>> selectTenants();

}