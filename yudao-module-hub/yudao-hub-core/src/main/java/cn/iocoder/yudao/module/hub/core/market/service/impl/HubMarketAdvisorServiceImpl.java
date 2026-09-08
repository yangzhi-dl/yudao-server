package cn.iocoder.yudao.module.hub.core.market.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiAgentDO;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiChatTopicDO;
import cn.iocoder.yudao.module.ai.core.chat.dal.mysql.AiChatTopicMapper;
import cn.iocoder.yudao.module.ai.core.chat.enums.AgentType;
import cn.iocoder.yudao.module.ai.core.chat.enums.AiApiType;
import cn.iocoder.yudao.module.ai.core.chat.enums.AiModelType;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ChatDialoguePageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ChatRenameTopicDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.SendMessageDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.ChatDialogueVO;
import cn.iocoder.yudao.module.ai.core.chat.service.AiAgentService;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatDialogueService;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatService;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatTopicService;
import cn.iocoder.yudao.module.ai.core.chat.utils.ChatContentUtil;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAdvisorContextRespVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAdvisorMessagePageReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAdvisorTopicPageReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAdvisorTopicRenameReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAdvisorTopicRespVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAdvisorSendReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceMessageRespVO;
import cn.iocoder.yudao.module.hub.core.market.dal.dataobject.HubMarketAdvisorTopicDO;
import cn.iocoder.yudao.module.hub.core.market.dal.mysql.HubMarketAdvisorTopicMapper;
import cn.iocoder.yudao.module.hub.core.market.service.HubMarketAdvisorService;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.hub.core.market.enums.ErrorCodeConstants.MARKET_ADVISOR_MESSAGE_INVALID;
import static cn.iocoder.yudao.module.hub.core.market.enums.ErrorCodeConstants.MARKET_ADVISOR_NOT_CONFIGURED;
import static cn.iocoder.yudao.module.hub.core.market.enums.ErrorCodeConstants.MARKET_ADVISOR_SOURCE_INVALID;
import static cn.iocoder.yudao.module.hub.core.market.enums.ErrorCodeConstants.MARKET_ADVISOR_TOPIC_INVALID;

/**
 * 市场选型助手实现。
 *
 * <p>助手编号由系统参数 {@value #ADVISOR_AGENT_ID_CONFIG_KEY} 提供，避免将一个仅供市场
 * 入口使用的智能体发布为市场商品。</p>
 */
@Service
@Validated
public class HubMarketAdvisorServiceImpl implements HubMarketAdvisorService {

    public static final String ADVISOR_AGENT_ID_CONFIG_KEY = "hub.market.advisor-agent-id";
    private static final Integer IMAGE_URL_EXPIRATION_SECONDS = 3000;

    @Resource
    private ConfigApi configApi;
    @Resource
    private AiAgentService aiAgentService;
    @Resource
    private AiChatService aiChatService;
    @Resource
    private AiChatTopicService aiChatTopicService;
    @Resource
    private AiChatTopicMapper aiChatTopicMapper;
    @Resource
    private AiChatDialogueService aiChatDialogueService;
    @Resource
    private ChatContentUtil chatContentUtil;
    @Resource
    private HubMarketAdvisorTopicMapper advisorTopicMapper;
    @Resource
    private FileApi fileApi;

    @Override
    public HubMarketAdvisorContextRespVO getAdvisorContext() {
        return buildContext(getAdvisorAgent());
    }

    @Override
    public SseEmitter sendMessage(Long uuid, HubMarketAdvisorSendReqVO reqVO) {
        if (reqVO.getTopicId() == null && reqVO.getLastId() != null) {
            throw exception(MARKET_ADVISOR_MESSAGE_INVALID);
        }
        AiAgentDO advisorAgent = getAdvisorAgent();
        HubMarketAdvisorTopicDO advisorTopic = prepareTopic(advisorAgent, reqVO);

        SendMessageDTO messageDTO = new SendMessageDTO();
        messageDTO.setTopicId(advisorTopic.getTopicId());
        messageDTO.setLastId(reqVO.getLastId());
        messageDTO.setEndpointId(advisorAgent.getId());
        messageDTO.setApiType(getApiType(advisorAgent.getType()));
        messageDTO.setModelType(AiModelType.LANGUAGE);
        messageDTO.setQuestion(reqVO.getQuestion());
        return aiChatService.conversationWithValidatedAgentPermission(uuid, messageDTO);
    }

    @Override
    public PageResult<HubMarketAdvisorTopicRespVO> getTopicPage(HubMarketAdvisorTopicPageReqVO reqVO) {
        AiAgentDO advisorAgent = getAdvisorAgent();
        PageResult<HubMarketAdvisorTopicDO> relationPage = advisorTopicMapper.selectPageByUserAndAdvisor(
                reqVO, getLoginUserId(), advisorAgent.getId());
        if (relationPage.getList().isEmpty()) {
            return new PageResult<>(List.of(), relationPage.getTotal());
        }
        List<Long> topicIds = relationPage.getList().stream()
                .map(HubMarketAdvisorTopicDO::getTopicId).toList();
        Map<Long, AiChatTopicDO> topicMap = new HashMap<>();
        String loginUserId = String.valueOf(getLoginUserId());
        for (AiChatTopicDO topic : aiChatTopicMapper.selectBatchIds(topicIds)) {
            if (Objects.equals(loginUserId, topic.getCreator())) {
                topicMap.put(topic.getId(), topic);
            }
        }
        List<HubMarketAdvisorTopicRespVO> result = new ArrayList<>();
        for (HubMarketAdvisorTopicDO relation : relationPage.getList()) {
            AiChatTopicDO topic = topicMap.get(relation.getTopicId());
            if (topic == null) {
                continue;
            }
            HubMarketAdvisorTopicRespVO response = new HubMarketAdvisorTopicRespVO();
            response.setTopicId(topic.getId());
            response.setTitle(topic.getTitle());
            response.setCreateTime(topic.getCreateTime());
            response.setLastActiveTime(relation.getLastActiveTime());
            result.add(response);
        }
        return new PageResult<>(result, relationPage.getTotal());
    }

    @Override
    public PageResult<HubMarketExperienceMessageRespVO> getMessagePage(
            HubMarketAdvisorMessagePageReqVO reqVO) {
        validateTopic(getAdvisorAgent(), reqVO.getTopicId());
        ChatDialoguePageQueryDTO query = new ChatDialoguePageQueryDTO();
        query.setTopicId(reqVO.getTopicId());
        query.setPage(reqVO.getPageNo());
        query.setPageSize(reqVO.getPageSize());
        PageResult<ChatDialogueVO> messagePage = aiChatDialogueService.chatDialoguePageQuery(query);
        return new PageResult<>(BeanUtils.toBean(
                messagePage.getList(), HubMarketExperienceMessageRespVO.class), messagePage.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renameTopic(HubMarketAdvisorTopicRenameReqVO reqVO) {
        validateTopic(getAdvisorAgent(), reqVO.getTopicId());
        aiChatTopicService.renameTopic(new ChatRenameTopicDTO(reqVO.getTopicId(), reqVO.getTitle().trim()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTopic(Long topicId) {
        HubMarketAdvisorTopicDO advisorTopic = validateTopic(getAdvisorAgent(), topicId);
        aiChatTopicService.delTopic(List.of(topicId));
        advisorTopicMapper.deleteById(advisorTopic.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    protected HubMarketAdvisorTopicDO prepareTopic(AiAgentDO advisorAgent,
                                                    HubMarketAdvisorSendReqVO reqVO) {
        if (reqVO.getTopicId() != null) {
            HubMarketAdvisorTopicDO advisorTopic = validateTopic(advisorAgent, reqVO.getTopicId());
            if (reqVO.getLastId() != null
                    && !aiChatDialogueService.isDialogueInTopic(reqVO.getLastId(), reqVO.getTopicId())) {
                throw exception(MARKET_ADVISOR_MESSAGE_INVALID);
            }
            refreshLastActiveTime(advisorTopic);
            return advisorTopic;
        }
        AiChatTopicDO aiChatTopic = AiChatTopicDO.builder()
                .title(chatContentUtil.limitLengthWithEllipsis(reqVO.getQuestion()))
                .deleted(false)
                .isArchived(false)
                .build();
        aiChatTopicService.insertTopic(aiChatTopic);

        HubMarketAdvisorTopicDO advisorTopic = new HubMarketAdvisorTopicDO();
        advisorTopic.setTopicId(aiChatTopic.getId());
        advisorTopic.setUserId(getLoginUserId());
        advisorTopic.setAdvisorAgentId(advisorAgent.getId());
        advisorTopic.setAdvisorTenantId(advisorAgent.getTenantId());
        advisorTopic.setAgentType(advisorAgent.getType().getValue());
        advisorTopic.setLastActiveTime(LocalDateTime.now());
        advisorTopic.setTenantId(TenantContextHolder.getRequiredTenantId());
        advisorTopicMapper.insert(advisorTopic);
        return advisorTopic;
    }

    private HubMarketAdvisorTopicDO validateTopic(AiAgentDO advisorAgent, Long topicId) {
        HubMarketAdvisorTopicDO advisorTopic = advisorTopicMapper.selectByTopicAndUserAndAdvisor(
                topicId, getLoginUserId(), advisorAgent.getId());
        AiChatTopicDO aiChatTopic = aiChatTopicMapper.selectById(topicId);
        if (advisorTopic == null
                || aiChatTopic == null
                || !Objects.equals(aiChatTopic.getCreator(), String.valueOf(getLoginUserId()))
                || !Objects.equals(advisorTopic.getAdvisorAgentId(), advisorAgent.getId())
                || !Objects.equals(advisorTopic.getAdvisorTenantId(), advisorAgent.getTenantId())
                || !Objects.equals(advisorTopic.getAgentType(), advisorAgent.getType().getValue())) {
            throw exception(MARKET_ADVISOR_TOPIC_INVALID);
        }
        return advisorTopic;
    }

    private void refreshLastActiveTime(HubMarketAdvisorTopicDO advisorTopic) {
        HubMarketAdvisorTopicDO update = new HubMarketAdvisorTopicDO();
        update.setId(advisorTopic.getId());
        update.setLastActiveTime(LocalDateTime.now());
        advisorTopicMapper.updateById(update);
        advisorTopic.setLastActiveTime(update.getLastActiveTime());
    }

    private AiAgentDO getAdvisorAgent() {
        String configValue = configApi.getConfigValueByKey(ADVISOR_AGENT_ID_CONFIG_KEY);
        if (!StringUtils.hasText(configValue)) {
            throw exception(MARKET_ADVISOR_NOT_CONFIGURED);
        }
        Long advisorAgentId;
        try {
            advisorAgentId = Long.valueOf(configValue.trim());
        } catch (NumberFormatException exception) {
            throw exception(MARKET_ADVISOR_NOT_CONFIGURED);
        }
        AiAgentDO advisorAgent = aiAgentService.selectAccessibleAgentById(advisorAgentId);
        if (advisorAgent == null || advisorAgent.getType() == null || advisorAgent.getModelId() == null) {
            throw exception(MARKET_ADVISOR_SOURCE_INVALID);
        }
        return advisorAgent;
    }

    private HubMarketAdvisorContextRespVO buildContext(AiAgentDO advisorAgent) {
        HubMarketAdvisorContextRespVO response = new HubMarketAdvisorContextRespVO();
        response.setAgentId(advisorAgent.getId());
        response.setAgentType(advisorAgent.getType().getValue());
        response.setName(advisorAgent.getName());
        response.setSummary(advisorAgent.getDescription());
        if (advisorAgent.getCover() != null) {
            response.setCoverUrl(fileApi.presignGetUrl(advisorAgent.getCover(), IMAGE_URL_EXPIRATION_SECONDS));
        }
        return response;
    }

    private AiApiType getApiType(AgentType agentType) {
        if (AgentType.REACT_AGENT.equals(agentType)) {
            return AiApiType.AGENT;
        }
        if (AgentType.MULTI_AGENT.equals(agentType)) {
            return AiApiType.MULTI_AGENT;
        }
        throw exception(MARKET_ADVISOR_SOURCE_INVALID);
    }

}
