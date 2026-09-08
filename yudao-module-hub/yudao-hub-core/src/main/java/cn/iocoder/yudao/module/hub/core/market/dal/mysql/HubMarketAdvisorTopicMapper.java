package cn.iocoder.yudao.module.hub.core.market.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAdvisorTopicPageReqVO;
import cn.iocoder.yudao.module.hub.core.market.dal.dataobject.HubMarketAdvisorTopicDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HubMarketAdvisorTopicMapper extends BaseMapperX<HubMarketAdvisorTopicDO> {

    default PageResult<HubMarketAdvisorTopicDO> selectPageByUserAndAdvisor(
            HubMarketAdvisorTopicPageReqVO reqVO, Long userId, Long advisorAgentId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<HubMarketAdvisorTopicDO>()
                .eq(HubMarketAdvisorTopicDO::getUserId, userId)
                .eq(HubMarketAdvisorTopicDO::getAdvisorAgentId, advisorAgentId)
                .orderByDesc(HubMarketAdvisorTopicDO::getLastActiveTime)
                .orderByDesc(HubMarketAdvisorTopicDO::getId));
    }

    default HubMarketAdvisorTopicDO selectByTopicAndUserAndAdvisor(
            Long topicId, Long userId, Long advisorAgentId) {
        return selectOne(new LambdaQueryWrapperX<HubMarketAdvisorTopicDO>()
                .eq(HubMarketAdvisorTopicDO::getTopicId, topicId)
                .eq(HubMarketAdvisorTopicDO::getUserId, userId)
                .eq(HubMarketAdvisorTopicDO::getAdvisorAgentId, advisorAgentId));
    }

}
