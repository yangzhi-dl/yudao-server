package cn.iocoder.yudao.module.hub.core.market.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceMessagePageReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceMessageRespVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceTopicPageReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceTopicRenameReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceTopicRespVO;
import cn.iocoder.yudao.module.hub.core.market.dal.dataobject.HubMarketAgentDO;
import cn.iocoder.yudao.module.hub.core.market.dal.dataobject.HubMarketExperienceTopicDO;

/**
 * Hub 智能体市场体验会话关联服务。
 */
public interface HubMarketExperienceTopicService {

    /**
     * 创建新体验话题，或校验并刷新已有体验话题。
     *
     * @param marketAgent 已完成有效性校验的市场智能体
     * @param topicId AI 会话主题编号，首次发送时为空
     * @param lastId 上一条对话编号，首次发送时为空
     * @param question 用户问题，用于生成新话题标题
     * @return 体验会话关联
     */
    HubMarketExperienceTopicDO prepareTopic(HubMarketAgentDO marketAgent, Long topicId,
                                             Long lastId, String question);

    /**
     * 分页查询当前用户的市场体验会话。
     *
     * @param marketAgent 已完成有效性校验的市场智能体
     * @param reqVO 分页参数
     * @return 体验会话分页结果
     */
    PageResult<HubMarketExperienceTopicRespVO> getTopicPage(
            HubMarketAgentDO marketAgent, HubMarketExperienceTopicPageReqVO reqVO);

    /**
     * 分页查询指定体验会话的消息。
     *
     * @param marketAgent 已完成有效性校验的市场智能体
     * @param reqVO 分页参数
     * @return 体验消息分页结果
     */
    PageResult<HubMarketExperienceMessageRespVO> getMessagePage(
            HubMarketAgentDO marketAgent, HubMarketExperienceMessagePageReqVO reqVO);

    /**
     * 重命名当前用户的体验会话。
     *
     * @param marketAgent 已完成有效性校验的市场智能体
     * @param reqVO 重命名参数
     */
    void renameTopic(HubMarketAgentDO marketAgent, HubMarketExperienceTopicRenameReqVO reqVO);

    /**
     * 删除当前用户的体验会话及其消息。
     *
     * @param marketAgent 已完成有效性校验的市场智能体
     * @param topicId AI 会话主题编号
     */
    void deleteTopic(HubMarketAgentDO marketAgent, Long topicId);

}
