package cn.iocoder.yudao.module.hub.core.market.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceContextRespVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceMessagePageReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceMessageRespVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceSendReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceTopicPageReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceTopicRenameReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceTopicRespVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Hub 智能体市场体验服务。
 */
public interface HubMarketExperienceService {

    /**
     * 获得登录用户进入市场体验页所需的上下文。
     *
     * @param marketAgentId 市场记录编号
     * @return 市场体验上下文
     */
    HubMarketExperienceContextRespVO getExperienceContext(Long marketAgentId);

    /**
     * 发送市场智能体体验消息。
     *
     * @param uuid 会话唯一标识
     * @param reqVO 体验消息参数
     * @return SSE 发射器
     */
    SseEmitter sendMessage(Long uuid, HubMarketExperienceSendReqVO reqVO);

    /**
     * 分页查询当前用户在指定市场智能体下的体验会话。
     *
     * @param reqVO 分页参数
     * @return 体验会话分页结果
     */
    PageResult<HubMarketExperienceTopicRespVO> getTopicPage(
            HubMarketExperienceTopicPageReqVO reqVO);

    /**
     * 分页查询指定体验会话的消息。
     *
     * @param reqVO 分页参数
     * @return 体验消息分页结果
     */
    PageResult<HubMarketExperienceMessageRespVO> getMessagePage(
            HubMarketExperienceMessagePageReqVO reqVO);

    /**
     * 重命名当前用户的体验会话。
     *
     * @param reqVO 重命名参数
     */
    void renameTopic(HubMarketExperienceTopicRenameReqVO reqVO);

    /**
     * 删除当前用户的体验会话及其消息。
     *
     * @param marketAgentId 市场记录编号
     * @param topicId AI 会话主题编号
     */
    void deleteTopic(Long marketAgentId, Long topicId);

}
