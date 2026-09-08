package cn.iocoder.yudao.module.hub.core.market.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiAgentDO;
import cn.iocoder.yudao.module.ai.core.chat.enums.AiApiType;
import cn.iocoder.yudao.module.ai.core.chat.enums.AiModelType;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.SendMessageDTO;
import cn.iocoder.yudao.module.ai.core.chat.service.AiAgentService;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatService;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceContextRespVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceMessagePageReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceMessageRespVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceSendReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceTopicPageReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceTopicRenameReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceTopicRespVO;
import cn.iocoder.yudao.module.hub.core.market.dal.dataobject.HubMarketAgentDO;
import cn.iocoder.yudao.module.hub.core.market.dal.dataobject.HubMarketExperienceTopicDO;
import cn.iocoder.yudao.module.hub.core.market.dal.mysql.HubMarketAgentMapper;
import cn.iocoder.yudao.module.hub.core.market.enums.HubMarketAgentTypeEnum;
import cn.iocoder.yudao.module.hub.core.market.enums.HubMarketPublishStatusEnum;
import cn.iocoder.yudao.module.hub.core.market.service.HubMarketExperienceService;
import cn.iocoder.yudao.module.hub.core.market.service.HubMarketExperienceTopicService;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hub.core.market.enums.ErrorCodeConstants.MARKET_AGENT_NOT_EXISTS;
import static cn.iocoder.yudao.module.hub.core.market.enums.ErrorCodeConstants.MARKET_AGENT_SOURCE_INVALID;
import static cn.iocoder.yudao.module.hub.core.market.enums.ErrorCodeConstants.MARKET_AGENT_TRIAL_DISABLED;
import static cn.iocoder.yudao.module.hub.core.market.enums.ErrorCodeConstants.MARKET_EXPERIENCE_MESSAGE_INVALID;

@Service
@Validated
public class HubMarketExperienceServiceImpl implements HubMarketExperienceService {

    /** 市场封面预签名地址有效期，单位为秒。 */
    private static final Integer IMAGE_URL_EXPIRATION_SECONDS = 3000;

    @Resource
    private HubMarketAgentMapper marketAgentMapper;

    @Resource
    private AiAgentService aiAgentService;

    @Resource
    private AiChatService aiChatService;

    @Resource
    private HubMarketExperienceTopicService experienceTopicService;

    @Resource
    private FileApi fileApi;

    @Override
    public HubMarketExperienceContextRespVO getExperienceContext(Long marketAgentId) {
        HubMarketAgentDO marketAgent = getValidMarketAgent(marketAgentId);
        return buildExperienceContext(marketAgent);
    }

    @Override
    public SseEmitter sendMessage(Long uuid, HubMarketExperienceSendReqVO reqVO) {
        if (reqVO.getTopicId() == null && reqVO.getLastId() != null) {
            throw exception(MARKET_EXPERIENCE_MESSAGE_INVALID);
        }
        HubMarketAgentDO marketAgent = getValidMarketAgent(reqVO.getMarketId());
        HubMarketExperienceTopicDO experienceTopic = experienceTopicService.prepareTopic(
                marketAgent, reqVO.getTopicId(), reqVO.getLastId(), reqVO.getQuestion());

        SendMessageDTO messageDTO = new SendMessageDTO();
        messageDTO.setTopicId(experienceTopic.getTopicId());
        messageDTO.setLastId(reqVO.getLastId());
        messageDTO.setEndpointId(marketAgent.getAgentId());
        messageDTO.setApiType(getApiType(marketAgent.getAgentType()));
        messageDTO.setModelType(AiModelType.LANGUAGE);
        messageDTO.setFileIds(reqVO.getFileIds());
        messageDTO.setQuestion(reqVO.getQuestion());
        return aiChatService.conversationWithValidatedAgentPermission(uuid, messageDTO);
    }

    @Override
    public PageResult<HubMarketExperienceTopicRespVO> getTopicPage(
            HubMarketExperienceTopicPageReqVO reqVO) {
        HubMarketAgentDO marketAgent = getValidMarketAgent(reqVO.getMarketId());
        return experienceTopicService.getTopicPage(marketAgent, reqVO);
    }

    @Override
    public PageResult<HubMarketExperienceMessageRespVO> getMessagePage(
            HubMarketExperienceMessagePageReqVO reqVO) {
        HubMarketAgentDO marketAgent = getValidMarketAgent(reqVO.getMarketId());
        return experienceTopicService.getMessagePage(marketAgent, reqVO);
    }

    @Override
    public void renameTopic(HubMarketExperienceTopicRenameReqVO reqVO) {
        HubMarketAgentDO marketAgent = getValidMarketAgent(reqVO.getMarketId());
        experienceTopicService.renameTopic(marketAgent, reqVO);
    }

    @Override
    public void deleteTopic(Long marketAgentId, Long topicId) {
        HubMarketAgentDO marketAgent = getValidMarketAgent(marketAgentId);
        experienceTopicService.deleteTopic(marketAgent, topicId);
    }

    private HubMarketAgentDO getValidMarketAgent(Long marketAgentId) {
        HubMarketAgentDO marketAgent = marketAgentMapper.selectPublishedById(
                marketAgentId, HubMarketPublishStatusEnum.PUBLISHED.getStatus());
        if (marketAgent == null) {
            throw exception(MARKET_AGENT_NOT_EXISTS);
        }
        if (!Boolean.TRUE.equals(marketAgent.getTrialEnabled())) {
            throw exception(MARKET_AGENT_TRIAL_DISABLED);
        }
        AiAgentDO sourceAgent = aiAgentService.selectAccessibleAgentById(marketAgent.getAgentId());
        validateSourceAgent(marketAgent, sourceAgent);
        return marketAgent;
    }

    private void validateSourceAgent(HubMarketAgentDO marketAgent, AiAgentDO sourceAgent) {
        if (sourceAgent == null || sourceAgent.getType() == null
                || !Objects.equals(marketAgent.getAgentType(), sourceAgent.getType().getValue())) {
            throw exception(MARKET_AGENT_SOURCE_INVALID);
        }
        if (marketAgent.getSourceTenantId() != null
                && !Objects.equals(marketAgent.getSourceTenantId(), sourceAgent.getTenantId())) {
            throw exception(MARKET_AGENT_SOURCE_INVALID);
        }
    }

    private HubMarketExperienceContextRespVO buildExperienceContext(HubMarketAgentDO marketAgent) {
        HubMarketExperienceContextRespVO response = new HubMarketExperienceContextRespVO();
        response.setMarketId(marketAgent.getId());
        response.setAgentType(marketAgent.getAgentType());
        response.setName(marketAgent.getName());
        response.setSummary(marketAgent.getSummary());
        response.setProviderName(marketAgent.getProviderName());
        if (marketAgent.getCoverId() != null) {
            response.setCoverUrl(fileApi.presignGetUrl(
                    marketAgent.getCoverId(), IMAGE_URL_EXPIRATION_SECONDS));
        }
        return response;
    }

    private AiApiType getApiType(Integer agentType) {
        if (Objects.equals(agentType, HubMarketAgentTypeEnum.SINGLE.getType())) {
            return AiApiType.AGENT;
        }
        if (Objects.equals(agentType, HubMarketAgentTypeEnum.MULTI.getType())) {
            return AiApiType.MULTI_AGENT;
        }
        throw exception(MARKET_AGENT_SOURCE_INVALID);
    }

}
