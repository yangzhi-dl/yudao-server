package cn.iocoder.yudao.module.hub.core.market.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubClusterDetailRespVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubClusterPageReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubClusterRespVO;

/**
 * Hub 前台智能体集群展示服务。
 */
public interface HubClusterService {

    /**
     * 获得可公开展示的智能体集群分页。
     */
    PageResult<HubClusterRespVO> getPublishedClusterPage(HubClusterPageReqVO pageReqVO);

    /**
     * 获得可公开展示的智能体集群详情。
     */
    HubClusterDetailRespVO getPublishedCluster(Long marketId);

}
