package cn.iocoder.yudao.module.analytics.service.aiusage;

import cn.iocoder.yudao.module.analytics.controller.admin.aiusage.vo.AnalyticsAiUsageSummaryRespVO;

/**
 * AI 使用分析 Service 接口
 */
public interface AnalyticsAiUsageService {

    /**
     * 获得 AI 使用汇总统计
     */
    AnalyticsAiUsageSummaryRespVO getSummary(Integer days);

}