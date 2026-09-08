package cn.iocoder.yudao.module.hub.core.market.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiAgentDO;
import cn.iocoder.yudao.module.ai.core.chat.enums.AgentType;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.MultiAgentSettings;
import cn.iocoder.yudao.module.ai.core.chat.service.AiAgentService;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubClusterDetailRespVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubClusterPageReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubClusterRespVO;
import cn.iocoder.yudao.module.hub.core.market.dal.dataobject.HubMarketAgentDO;
import cn.iocoder.yudao.module.hub.core.market.dal.dataobject.HubMarketAgentDetailDO;
import cn.iocoder.yudao.module.hub.core.market.dal.mysql.HubMarketAgentDetailMapper;
import cn.iocoder.yudao.module.hub.core.market.dal.mysql.HubMarketAgentMapper;
import cn.iocoder.yudao.module.hub.core.market.enums.HubMarketAgentTypeEnum;
import cn.iocoder.yudao.module.hub.core.market.enums.HubMarketPublishStatusEnum;
import cn.iocoder.yudao.module.hub.core.market.model.entity.HubMarketTextItem;
import cn.iocoder.yudao.module.hub.core.market.service.HubClusterService;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.system.dal.dataobject.dict.DictDataDO;
import cn.iocoder.yudao.module.system.service.dict.DictDataService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hub.core.market.enums.ErrorCodeConstants.MARKET_AGENT_NOT_EXISTS;

/**
 * Hub 集群展示只读取市场已发布内容和多智能体 settings 中的安全展示字段。
 */
@Service
@Validated
public class HubClusterServiceImpl implements HubClusterService {

    /** 市场封面预签名地址有效期，单位为秒。 */
    private static final Integer IMAGE_URL_EXPIRATION_SECONDS = 3000;

    @Resource
    private HubMarketAgentMapper marketAgentMapper;

    @Resource
    private HubMarketAgentDetailMapper marketAgentDetailMapper;

    @Resource
    private AiAgentService aiAgentService;

    @Resource
    private FileApi fileApi;

    @Resource
    private DictDataService dictDataService;

    @Override
    public PageResult<HubClusterRespVO> getPublishedClusterPage(HubClusterPageReqVO pageReqVO) {
        List<DisplayableCluster> clusters = marketAgentMapper.selectPublishedMultiList(
                        HubMarketPublishStatusEnum.PUBLISHED.getStatus())
                .stream()
                .map(this::toDisplayableCluster)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, String> dictLabelMap = getDictLabelMap(
                clusters.stream().map(DisplayableCluster::marketAgent).toList());
        List<HubClusterRespVO> responseList = clusters.stream()
                .map(cluster -> buildClusterRespVO(cluster, dictLabelMap))
                .toList();
        return new PageResult<>(getPageItems(responseList, pageReqVO),
                Long.valueOf(responseList.size()));
    }

    @Override
    public HubClusterDetailRespVO getPublishedCluster(Long marketId) {
        HubMarketAgentDO marketAgent = marketAgentMapper.selectPublishedById(
                marketId, HubMarketPublishStatusEnum.PUBLISHED.getStatus());
        DisplayableCluster cluster = toDisplayableCluster(marketAgent);
        if (cluster == null) {
            // 对无效来源或无有效成员的市场记录同样按不存在处理，避免公开其内部状态。
            throw exception(MARKET_AGENT_NOT_EXISTS);
        }
        Map<Long, String> dictLabelMap = getDictLabelMap(List.of(marketAgent));
        HubClusterRespVO summary = buildClusterRespVO(cluster, dictLabelMap);
        HubClusterDetailRespVO response = new HubClusterDetailRespVO();
        copySummary(summary, response);

        HubMarketAgentDetailDO detail = marketAgentDetailMapper.selectByMarketAgentId(marketId);
        if (detail == null) {
            response.setTaskItems(List.of());
            response.setCapabilityItems(List.of());
            return response;
        }
        response.setIntroduction(detail.getIntroduction());
        response.setTaskItems(toContentItems(detail.getScenarios()));
        response.setCapabilityItems(toContentItems(detail.getFeatures()));
        response.setTrialDescription(detail.getTrialDescription());
        response.setDeploymentDescription(detail.getDeploymentDescription());
        return response;
    }

    /**
     * 仅当市场记录、来源多智能体及至少一个可展示成员均有效时才纳入前台。
     */
    private DisplayableCluster toDisplayableCluster(HubMarketAgentDO marketAgent) {
        if (marketAgent == null || !Objects.equals(marketAgent.getAgentType(),
                HubMarketAgentTypeEnum.MULTI.getType())) {
            return null;
        }
        AiAgentDO sourceAgent = aiAgentService.selectAccessibleAgentById(marketAgent.getAgentId());
        if (!isValidSourceAgent(marketAgent, sourceAgent)) {
            return null;
        }
        List<HubClusterRespVO.MemberVO> members = toDisplayMembers(sourceAgent.getSettings());
        if (members.isEmpty()) {
            return null;
        }
        return new DisplayableCluster(marketAgent, members);
    }

    private boolean isValidSourceAgent(HubMarketAgentDO marketAgent, AiAgentDO sourceAgent) {
        if (sourceAgent == null || sourceAgent.getType() != AgentType.MULTI_AGENT) {
            return false;
        }
        return marketAgent.getSourceTenantId() == null
                || Objects.equals(marketAgent.getSourceTenantId(), sourceAgent.getTenantId());
    }

    /**
     * 不复制 settings 中的英文名、模型、提示词、工具或技能；只取名称和职责。
     */
    private List<HubClusterRespVO.MemberVO> toDisplayMembers(
            List<MultiAgentSettings> settings) {
        if (settings == null || settings.isEmpty()) {
            return List.of();
        }
        return settings.stream()
                .filter(Objects::nonNull)
                .filter(setting -> StringUtils.hasText(setting.getName())
                        && StringUtils.hasText(setting.getDescription()))
                .map(setting -> {
                    HubClusterRespVO.MemberVO response = new HubClusterRespVO.MemberVO();
                    response.setName(setting.getName().trim());
                    response.setDescription(setting.getDescription().trim());
                    return response;
                })
                .toList();
    }

    private HubClusterRespVO buildClusterRespVO(DisplayableCluster cluster,
                                                 Map<Long, String> dictLabelMap) {
        HubMarketAgentDO marketAgent = cluster.marketAgent();
        HubClusterRespVO response = new HubClusterRespVO();
        response.setMarketId(marketAgent.getId());
        response.setName(marketAgent.getName());
        response.setSummary(marketAgent.getSummary());
        if (marketAgent.getCoverId() != null) {
            response.setCoverUrl(fileApi.presignGetUrl(
                    marketAgent.getCoverId(), IMAGE_URL_EXPIRATION_SECONDS));
        }
        response.setCategoryName(dictLabelMap.get(marketAgent.getCategoryId()));
        response.setTagNames(getTagNames(marketAgent.getTagIds(), dictLabelMap));
        response.setTrialEnabled(Boolean.TRUE.equals(marketAgent.getTrialEnabled()));
        response.setMemberCount(cluster.members().size());
        response.setMembers(cluster.members());
        return response;
    }

    private void copySummary(HubClusterRespVO source, HubClusterDetailRespVO target) {
        target.setMarketId(source.getMarketId());
        target.setName(source.getName());
        target.setSummary(source.getSummary());
        target.setCoverUrl(source.getCoverUrl());
        target.setCategoryName(source.getCategoryName());
        target.setTagNames(source.getTagNames());
        target.setTrialEnabled(source.getTrialEnabled());
        target.setMemberCount(source.getMemberCount());
        target.setMembers(source.getMembers());
    }

    private Map<Long, String> getDictLabelMap(List<HubMarketAgentDO> marketAgents) {
        Set<Long> dictIds = new HashSet<>();
        for (HubMarketAgentDO marketAgent : marketAgents) {
            if (marketAgent.getCategoryId() != null) {
                dictIds.add(marketAgent.getCategoryId());
            }
            if (marketAgent.getTagIds() != null) {
                dictIds.addAll(marketAgent.getTagIds());
            }
        }
        if (dictIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> result = new LinkedHashMap<>();
        for (DictDataDO dictData : dictDataService.getDictDataByIds(new ArrayList<>(dictIds))) {
            result.put(dictData.getId(), dictData.getLabel());
        }
        return result;
    }

    private List<String> getTagNames(List<Long> tagIds, Map<Long, String> dictLabelMap) {
        if (tagIds == null || tagIds.isEmpty()) {
            return List.of();
        }
        return tagIds.stream()
                .map(dictLabelMap::get)
                .filter(StringUtils::hasText)
                .toList();
    }

    private List<HubClusterDetailRespVO.ContentItemVO> toContentItems(
            List<HubMarketTextItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream()
                .filter(Objects::nonNull)
                .filter(item -> StringUtils.hasText(item.getTitle())
                        || StringUtils.hasText(item.getDescription()))
                .map(item -> {
                    HubClusterDetailRespVO.ContentItemVO response =
                            new HubClusterDetailRespVO.ContentItemVO();
                    response.setTitle(trimToNull(item.getTitle()));
                    response.setDescription(trimToNull(item.getDescription()));
                    return response;
                })
                .toList();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private <T> List<T> getPageItems(List<T> items, HubClusterPageReqVO pageReqVO) {
        long offset = (long) (pageReqVO.getPageNo() - 1) * pageReqVO.getPageSize();
        if (offset >= items.size()) {
            return List.of();
        }
        int fromIndex = (int) offset;
        int toIndex = Math.min(fromIndex + pageReqVO.getPageSize(), items.size());
        return items.subList(fromIndex, toIndex);
    }

    private record DisplayableCluster(HubMarketAgentDO marketAgent,
                                      List<HubClusterRespVO.MemberVO> members) {
    }

}
