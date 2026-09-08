package cn.iocoder.yudao.module.hub.core.market.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAgentDetailRespVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAgentPageReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAgentRespVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketFilterOptionsRespVO;

/**
 * Hub 智能体市场服务。
 */
public interface HubMarketAgentService {

    /**
     * 获得已发布智能体分页。
     */
    PageResult<HubMarketAgentRespVO> getPublishedAgentPage(HubMarketAgentPageReqVO pageReqVO);

    /**
     * 获得已发布智能体详情。
     */
    HubMarketAgentDetailRespVO getPublishedAgent(Long id);

    /**
     * 获得市场公开筛选项。
     */
    HubMarketFilterOptionsRespVO getFilterOptions();

}
