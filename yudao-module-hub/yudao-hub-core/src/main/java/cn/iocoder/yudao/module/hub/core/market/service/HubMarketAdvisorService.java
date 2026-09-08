package cn.iocoder.yudao.module.hub.core.market.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAdvisorContextRespVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAdvisorMessagePageReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAdvisorTopicPageReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAdvisorTopicRenameReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAdvisorTopicRespVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAdvisorSendReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceMessageRespVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 市场选型助手服务。 */
public interface HubMarketAdvisorService {

    HubMarketAdvisorContextRespVO getAdvisorContext();

    SseEmitter sendMessage(Long uuid, HubMarketAdvisorSendReqVO reqVO);

    PageResult<HubMarketAdvisorTopicRespVO> getTopicPage(HubMarketAdvisorTopicPageReqVO reqVO);

    PageResult<HubMarketExperienceMessageRespVO> getMessagePage(HubMarketAdvisorMessagePageReqVO reqVO);

    void renameTopic(HubMarketAdvisorTopicRenameReqVO reqVO);

    void deleteTopic(Long topicId);

}
