package cn.iocoder.yudao.module.analytics.service.overview;

import cn.iocoder.yudao.module.analytics.controller.admin.overview.vo.AnalyticsOverviewSummaryRespVO;
import cn.iocoder.yudao.module.analytics.controller.admin.overview.vo.AnalyticsOverviewTenantStatsRespVO;

import java.util.List;

/**
 * 平台分析总览 Service 接口
 */
public interface AnalyticsOverviewService {

    /**
     * 获得平台汇总统计
     */
    AnalyticsOverviewSummaryRespVO getSummary();

    /**
     * 获得各租户的统计明细
     */
    List<AnalyticsOverviewTenantStatsRespVO> getTenantStats();

}