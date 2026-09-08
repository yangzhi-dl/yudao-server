package cn.iocoder.yudao.module.hub.core.market.dal.mysql;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAgentPageReqVO;
import cn.iocoder.yudao.module.hub.core.market.dal.dataobject.HubMarketAgentDO;
import cn.iocoder.yudao.module.hub.core.market.enums.HubMarketAgentTypeEnum;
import cn.iocoder.yudao.module.hub.core.market.enums.HubMarketSortTypeEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Objects;

@Mapper
public interface HubMarketAgentMapper extends BaseMapperX<HubMarketAgentDO> {

    /**
     * 查询已发布的多智能体市场记录。
     *
     * 集群页还需要校验来源智能体和可展示成员配置，因此不能直接将本查询结果公开。
     */
    default List<HubMarketAgentDO> selectPublishedMultiList(Integer publishStatus) {
        return selectList(new LambdaQueryWrapperX<HubMarketAgentDO>()
                .eq(HubMarketAgentDO::getPublishStatus, publishStatus)
                .eq(HubMarketAgentDO::getAgentType, HubMarketAgentTypeEnum.MULTI.getType())
                .orderByDesc(HubMarketAgentDO::getRecommended)
                .orderByAsc(HubMarketAgentDO::getSort)
                .orderByDesc(HubMarketAgentDO::getPublishTime)
                .orderByDesc(HubMarketAgentDO::getId));
    }

    /**
     * 分页查询已发布的市场智能体。
     */
    default PageResult<HubMarketAgentDO> selectPublishedPage(HubMarketAgentPageReqVO reqVO,
                                                              Integer publishStatus) {
        LambdaQueryWrapperX<HubMarketAgentDO> wrapper = new LambdaQueryWrapperX<HubMarketAgentDO>()
                .eq(HubMarketAgentDO::getPublishStatus, publishStatus)
                .eqIfPresent(HubMarketAgentDO::getCategoryId, reqVO.getCategoryId())
                .eqIfPresent(HubMarketAgentDO::getAgentType, reqVO.getAgentType())
                .eqIfPresent(HubMarketAgentDO::getRecommended, reqVO.getRecommended());
        if (StrUtil.isNotBlank(reqVO.getKeyword())) {
            wrapper.and(query -> query
                    .like(HubMarketAgentDO::getName, reqVO.getKeyword())
                    .or()
                    .like(HubMarketAgentDO::getSummary, reqVO.getKeyword())
                    .or()
                    .like(HubMarketAgentDO::getProviderName, reqVO.getKeyword()));
        }
        if (reqVO.getTagId() != null) {
            wrapper.apply("JSON_CONTAINS(tag_ids, CAST({0} AS JSON))", reqVO.getTagId());
        }
        if (Objects.equals(reqVO.getSortType(), HubMarketSortTypeEnum.LATEST.getType())) {
            wrapper.orderByDesc(HubMarketAgentDO::getPublishTime)
                    .orderByDesc(HubMarketAgentDO::getId);
        } else {
            wrapper.orderByDesc(HubMarketAgentDO::getRecommended)
                    .orderByAsc(HubMarketAgentDO::getSort)
                    .orderByDesc(HubMarketAgentDO::getPublishTime)
                    .orderByDesc(HubMarketAgentDO::getId);
        }
        return selectPage(reqVO, wrapper);
    }

    /**
     * 根据市场记录编号查询已发布智能体。
     */
    default HubMarketAgentDO selectPublishedById(Long id, Integer publishStatus) {
        return selectOne(new LambdaQueryWrapperX<HubMarketAgentDO>()
                .eq(HubMarketAgentDO::getId, id)
                .eq(HubMarketAgentDO::getPublishStatus, publishStatus));
    }

}
