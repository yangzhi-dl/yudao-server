package cn.iocoder.yudao.module.hub.core.market.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hub.core.market.dal.dataobject.HubMarketAgentDetailDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HubMarketAgentDetailMapper extends BaseMapperX<HubMarketAgentDetailDO> {

    /**
     * 根据市场记录编号查询详情。
     */
    default HubMarketAgentDetailDO selectByMarketAgentId(Long marketAgentId) {
        return selectOne(new LambdaQueryWrapperX<HubMarketAgentDetailDO>()
                .eq(HubMarketAgentDetailDO::getMarketAgentId, marketAgentId));
    }

}
