package cn.iocoder.yudao.module.hub.core.market.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceTopicPageReqVO;
import cn.iocoder.yudao.module.hub.core.market.dal.dataobject.HubMarketAgentDO;
import cn.iocoder.yudao.module.hub.core.market.dal.dataobject.HubMarketExperienceTopicDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HubMarketExperienceTopicMapper extends BaseMapperX<HubMarketExperienceTopicDO> {

    /**
     * 分页查询当前用户在指定市场智能体下的体验会话。
     */
    default PageResult<HubMarketExperienceTopicDO> selectPageByUserAndMarket(
            HubMarketExperienceTopicPageReqVO reqVO, Long userId, HubMarketAgentDO marketAgent) {
        LambdaQueryWrapperX<HubMarketExperienceTopicDO> wrapper =
                new LambdaQueryWrapperX<HubMarketExperienceTopicDO>()
                        .eq(HubMarketExperienceTopicDO::getUserId, userId)
                        .eq(HubMarketExperienceTopicDO::getMarketAgentId, marketAgent.getId())
                        .eq(HubMarketExperienceTopicDO::getSourceAgentId, marketAgent.getAgentId())
                        .eq(HubMarketExperienceTopicDO::getAgentType, marketAgent.getAgentType())
                        .orderByDesc(HubMarketExperienceTopicDO::getLastActiveTime)
                        .orderByDesc(HubMarketExperienceTopicDO::getId);
        if (marketAgent.getSourceTenantId() == null) {
            wrapper.isNull(HubMarketExperienceTopicDO::getSourceTenantId);
        } else {
            wrapper.eq(HubMarketExperienceTopicDO::getSourceTenantId,
                    marketAgent.getSourceTenantId());
        }
        return selectPage(reqVO, wrapper);
    }

    /**
     * 查询当前用户在指定市场智能体下的体验话题关联。
     */
    default HubMarketExperienceTopicDO selectByTopicAndUserAndMarket(Long topicId, Long userId,
                                                                     Long marketAgentId) {
        return selectOne(new LambdaQueryWrapperX<HubMarketExperienceTopicDO>()
                .eq(HubMarketExperienceTopicDO::getTopicId, topicId)
                .eq(HubMarketExperienceTopicDO::getUserId, userId)
                .eq(HubMarketExperienceTopicDO::getMarketAgentId, marketAgentId));
    }

}
