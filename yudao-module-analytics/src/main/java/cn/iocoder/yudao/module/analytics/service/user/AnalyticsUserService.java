package cn.iocoder.yudao.module.analytics.service.user;

import cn.iocoder.yudao.module.analytics.controller.admin.user.vo.AnalyticsUserSummaryRespVO;

/**
 * 用户与登录分析 Service 接口
 */
public interface AnalyticsUserService {

    /**
     * 获得用户与登录汇总统计
     */
    AnalyticsUserSummaryRespVO getSummary(Integer days);

}