package cn.iocoder.yudao.module.hub.core.market.service.impl;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAgentDetailRespVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAgentPageReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAgentRespVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketFilterOptionsRespVO;
import cn.iocoder.yudao.module.hub.core.market.dal.dataobject.HubMarketAgentDO;
import cn.iocoder.yudao.module.hub.core.market.dal.dataobject.HubMarketAgentDetailDO;
import cn.iocoder.yudao.module.hub.core.market.dal.mysql.HubMarketAgentDetailMapper;
import cn.iocoder.yudao.module.hub.core.market.dal.mysql.HubMarketAgentMapper;
import cn.iocoder.yudao.module.hub.core.market.enums.HubMarketPublishStatusEnum;
import cn.iocoder.yudao.module.hub.core.market.model.entity.HubMarketFaqItem;
import cn.iocoder.yudao.module.hub.core.market.model.entity.HubMarketGalleryItem;
import cn.iocoder.yudao.module.hub.core.market.model.entity.HubMarketProcessItem;
import cn.iocoder.yudao.module.hub.core.market.model.entity.HubMarketTextItem;
import cn.iocoder.yudao.module.hub.core.market.service.HubMarketAgentService;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.system.dal.dataobject.dict.DictDataDO;
import cn.iocoder.yudao.module.system.service.dict.DictDataService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hub.core.market.enums.ErrorCodeConstants.MARKET_AGENT_NOT_EXISTS;

@Service
@Validated
public class HubMarketAgentServiceImpl implements HubMarketAgentService {

    /** 市场图片预签名地址有效期，单位为秒。 */
    private static final Integer IMAGE_URL_EXPIRATION_SECONDS = 3000;
    /** 当前市场复用的智能体分类字典。 */
    private static final String AGENT_CATEGORY_DICT_TYPE = "ai_agent_category";
    /** 当前市场复用的智能体标签字典。 */
    private static final String AGENT_TAG_DICT_TYPE = "ai_agent_tag";

    @Resource
    private HubMarketAgentMapper marketAgentMapper;

    @Resource
    private HubMarketAgentDetailMapper marketAgentDetailMapper;

    @Resource
    private FileApi fileApi;

    @Resource
    private DictDataService dictDataService;

    @Override
    public PageResult<HubMarketAgentRespVO> getPublishedAgentPage(HubMarketAgentPageReqVO pageReqVO) {
        PageResult<HubMarketAgentDO> pageResult = marketAgentMapper.selectPublishedPage(
                pageReqVO, HubMarketPublishStatusEnum.PUBLISHED.getStatus());
        Map<Long, String> dictLabelMap = getDictLabelMap(pageResult.getList());
        List<HubMarketAgentRespVO> responseList = pageResult.getList().stream()
                .map(agent -> buildAgentRespVO(agent, dictLabelMap))
                .toList();
        return new PageResult<>(responseList, pageResult.getTotal());
    }

    @Override
    public HubMarketAgentDetailRespVO getPublishedAgent(Long id) {
        HubMarketAgentDO marketAgent = marketAgentMapper.selectPublishedById(
                id, HubMarketPublishStatusEnum.PUBLISHED.getStatus());
        if (marketAgent == null) {
            throw exception(MARKET_AGENT_NOT_EXISTS);
        }
        Map<Long, String> dictLabelMap = getDictLabelMap(List.of(marketAgent));
        HubMarketAgentDetailRespVO response = BeanUtils.toBean(marketAgent,
                HubMarketAgentDetailRespVO.class);
        enrichAgentRespVO(response, marketAgent, dictLabelMap);
        HubMarketAgentDetailDO detail = marketAgentDetailMapper.selectByMarketAgentId(id);
        enrichDetailRespVO(response, detail);
        return response;
    }

    @Override
    public HubMarketFilterOptionsRespVO getFilterOptions() {
        HubMarketFilterOptionsRespVO response = new HubMarketFilterOptionsRespVO();
        response.setCategories(getDictOptions(AGENT_CATEGORY_DICT_TYPE));
        response.setTags(getDictOptions(AGENT_TAG_DICT_TYPE));
        return response;
    }

    private List<HubMarketFilterOptionsRespVO.OptionVO> getDictOptions(String dictType) {
        List<DictDataDO> dictDataList = dictDataService.getDictDataList(
                CommonStatusEnum.ENABLE.getStatus(), dictType);
        return BeanUtils.toBean(dictDataList, HubMarketFilterOptionsRespVO.OptionVO.class);
    }

    private HubMarketAgentRespVO buildAgentRespVO(HubMarketAgentDO marketAgent,
                                                   Map<Long, String> dictLabelMap) {
        HubMarketAgentRespVO response = BeanUtils.toBean(marketAgent, HubMarketAgentRespVO.class);
        enrichAgentRespVO(response, marketAgent, dictLabelMap);
        return response;
    }

    /**
     * 补充文件地址和字典显示名称，响应中不暴露来源智能体和来源租户。
     */
    private void enrichAgentRespVO(HubMarketAgentRespVO response, HubMarketAgentDO marketAgent,
                                   Map<Long, String> dictLabelMap) {
        if (marketAgent.getCoverId() != null) {
            response.setCoverUrl(fileApi.presignGetUrl(
                    marketAgent.getCoverId(), IMAGE_URL_EXPIRATION_SECONDS));
        }
        response.setCategoryName(dictLabelMap.get(marketAgent.getCategoryId()));
        response.setTagNames(getTagNames(marketAgent.getTagIds(), dictLabelMap));
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
                .filter(name -> name != null && !name.isBlank())
                .toList();
    }

    private void enrichDetailRespVO(HubMarketAgentDetailRespVO response,
                                    HubMarketAgentDetailDO detail) {
        if (detail == null) {
            setEmptyDetail(response);
            return;
        }
        response.setIntroduction(detail.getIntroduction());
        response.setFeatures(toTextItemVOList(detail.getFeatures()));
        response.setScenarios(toTextItemVOList(detail.getScenarios()));
        response.setAdvantages(toTextItemVOList(detail.getAdvantages()));
        response.setUsageProcess(toProcessItemVOList(detail.getUsageProcess()));
        response.setCaseDescription(detail.getCaseDescription());
        response.setGallery(toGalleryItemVOList(detail.getGallery()));
        response.setTrialDescription(detail.getTrialDescription());
        response.setDeploymentDescription(detail.getDeploymentDescription());
        response.setFaq(toFaqItemVOList(detail.getFaq()));
    }

    private void setEmptyDetail(HubMarketAgentDetailRespVO response) {
        response.setFeatures(List.of());
        response.setScenarios(List.of());
        response.setAdvantages(List.of());
        response.setUsageProcess(List.of());
        response.setGallery(List.of());
        response.setFaq(List.of());
    }

    private List<HubMarketAgentDetailRespVO.TextItemVO> toTextItemVOList(
            List<HubMarketTextItem> itemList) {
        if (itemList == null || itemList.isEmpty()) {
            return List.of();
        }
        return BeanUtils.toBean(itemList, HubMarketAgentDetailRespVO.TextItemVO.class);
    }

    private List<HubMarketAgentDetailRespVO.ProcessItemVO> toProcessItemVOList(
            List<HubMarketProcessItem> itemList) {
        if (itemList == null || itemList.isEmpty()) {
            return List.of();
        }
        return itemList.stream().map(item -> {
            HubMarketAgentDetailRespVO.ProcessItemVO response = BeanUtils.toBean(
                    item, HubMarketAgentDetailRespVO.ProcessItemVO.class);
            if (item.getImageId() != null) {
                response.setImageUrl(fileApi.presignGetUrl(
                        item.getImageId(), IMAGE_URL_EXPIRATION_SECONDS));
            }
            return response;
        }).toList();
    }

    private List<HubMarketAgentDetailRespVO.GalleryItemVO> toGalleryItemVOList(
            List<HubMarketGalleryItem> itemList) {
        if (itemList == null || itemList.isEmpty()) {
            return List.of();
        }
        return itemList.stream().map(item -> {
            HubMarketAgentDetailRespVO.GalleryItemVO response = BeanUtils.toBean(
                    item, HubMarketAgentDetailRespVO.GalleryItemVO.class);
            if (item.getFileId() != null) {
                response.setImageUrl(fileApi.presignGetUrl(
                        item.getFileId(), IMAGE_URL_EXPIRATION_SECONDS));
            }
            return response;
        }).toList();
    }

    private List<HubMarketAgentDetailRespVO.FaqItemVO> toFaqItemVOList(
            List<HubMarketFaqItem> itemList) {
        if (itemList == null || itemList.isEmpty()) {
            return List.of();
        }
        return BeanUtils.toBean(itemList, HubMarketAgentDetailRespVO.FaqItemVO.class);
    }

}
