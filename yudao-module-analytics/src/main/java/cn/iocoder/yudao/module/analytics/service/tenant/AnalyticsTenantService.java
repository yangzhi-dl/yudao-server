package cn.iocoder.yudao.module.analytics.service.tenant;

import cn.iocoder.yudao.module.analytics.controller.admin.tenant.vo.AnalyticsTenantSummaryRespVO;

/**
 * 租户运营分析 Service 接口
 */
public interface AnalyticsTenantService {

    /**
     * 获得租户运营汇总统计
     */
    AnalyticsTenantSummaryRespVO getSummary(Integer days);

}