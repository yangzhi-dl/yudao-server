package cn.iocoder.yudao.module.analytics.service.ability;

import cn.iocoder.yudao.module.analytics.controller.admin.ability.vo.AnalyticsAbilitySummaryRespVO;

/**
 * AI 能力分析 Service 接口
 */
public interface AnalyticsAbilityService {

    /**
     * 获得 AI 能力汇总统计
     */
    AnalyticsAbilitySummaryRespVO getSummary();

}