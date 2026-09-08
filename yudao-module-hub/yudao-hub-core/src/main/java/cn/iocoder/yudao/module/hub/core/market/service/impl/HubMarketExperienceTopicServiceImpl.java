package cn.iocoder.yudao.module.hub.core.market.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiChatTopicDO;
import cn.iocoder.yudao.module.ai.core.chat.dal.mysql.AiChatTopicMapper;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ChatDialoguePageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ChatRenameTopicDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.ChatDialogueVO;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatDialogueService;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatTopicService;
import cn.iocoder.yudao.module.ai.core.chat.utils.ChatContentUtil;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceMessagePageReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceMessageRespVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceTopicPageReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceTopicRenameReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceTopicRespVO;
import cn.iocoder.yudao.module.hub.core.market.dal.dataobject.HubMarketAgentDO;
import cn.iocoder.yudao.module.hub.core.market.dal.dataobject.HubMarketExperienceTopicDO;
import cn.iocoder.yudao.module.hub.core.market.dal.mysql.HubMarketExperienceTopicMapper;
import cn.iocoder.yudao.module.hub.core.market.service.HubMarketExperienceTopicService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.hub.core.market.enums.ErrorCodeConstants.MARKET_EXPERIENCE_MESSAGE_INVALID;
import static cn.iocoder.yudao.module.hub.core.market.enums.ErrorCodeConstants.MARKET_EXPERIENCE_TOPIC_INVALID;

@Service
@Validated
public class HubMarketExperienceTopicServiceImpl implements HubMarketExperienceTopicService {

    @Resource
    private HubMarketExperienceTopicMapper experienceTopicMapper;

    @Resource
    private AiChatTopicService aiChatTopicService;

    @Resource
    private AiChatTopicMapper aiChatTopicMapper;

    @Resource
    private AiChatDialogueService aiChatDialogueService;

    @Resource
    private ChatContentUtil chatContentUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HubMarketExperienceTopicDO prepareTopic(HubMarketAgentDO marketAgent, Long topicId,
                                                    Long lastId, String question) {
        if (topicId == null) {
            return createTopic(marketAgent, question);
        }
        HubMarketExperienceTopicDO experienceTopic = validateTopic(marketAgent, topicId);
        if (lastId != null && !aiChatDialogueService.isDialogueInTopic(lastId, topicId)) {
            throw exception(MARKET_EXPERIENCE_MESSAGE_INVALID);
        }
        refreshLastActiveTime(experienceTopic);
        return experienceTopic;
    }

    @Override
    public PageResult<HubMarketExperienceTopicRespVO> getTopicPage(
            HubMarketAgentDO marketAgent, HubMarketExperienceTopicPageReqVO reqVO) {
        PageResult<HubMarketExperienceTopicDO> relationPage = experienceTopicMapper
                .selectPageByUserAndMarket(reqVO, getLoginUserId(), marketAgent);
        if (relationPage.getList().isEmpty()) {
            return new PageResult<>(List.of(), relationPage.getTotal());
        }

        List<Long> topicIds = new ArrayList<>(relationPage.getList().size());
        for (HubMarketExperienceTopicDO relation : relationPage.getList()) {
            topicIds.add(relation.getTopicId());
        }
        Map<Long, AiChatTopicDO> topicMap = new HashMap<>();
        String loginUserId = String.valueOf(getLoginUserId());
        for (AiChatTopicDO topic : aiChatTopicMapper.selectBatchIds(topicIds)) {
            if (Objects.equals(loginUserId, topic.getCreator())) {
                topicMap.put(topic.getId(), topic);
            }
        }

        List<HubMarketExperienceTopicRespVO> result = new ArrayList<>();
        for (HubMarketExperienceTopicDO relation : relationPage.getList()) {
            AiChatTopicDO topic = topicMap.get(relation.getTopicId());
            if (topic == null) {
                continue;
            }
            HubMarketExperienceTopicRespVO response = new HubMarketExperienceTopicRespVO();
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
            HubMarketAgentDO marketAgent, HubMarketExperienceMessagePageReqVO reqVO) {
        validateTopic(marketAgent, reqVO.getTopicId());
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
    public void renameTopic(HubMarketAgentDO marketAgent,
                            HubMarketExperienceTopicRenameReqVO reqVO) {
        validateTopic(marketAgent, reqVO.getTopicId());
        aiChatTopicService.renameTopic(new ChatRenameTopicDTO(
                reqVO.getTopicId(), reqVO.getTitle().trim()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTopic(HubMarketAgentDO marketAgent, Long topicId) {
        HubMarketExperienceTopicDO experienceTopic = validateTopic(marketAgent, topicId);
        aiChatTopicService.delTopic(List.of(topicId));
        experienceTopicMapper.deleteById(experienceTopic.getId());
    }

    private HubMarketExperienceTopicDO createTopic(HubMarketAgentDO marketAgent, String question) {
        AiChatTopicDO aiChatTopic = AiChatTopicDO.builder()
                .title(chatContentUtil.limitLengthWithEllipsis(question))
                .deleted(false)
                .isArchived(false)
                .build();
        aiChatTopicService.insertTopic(aiChatTopic);

        HubMarketExperienceTopicDO experienceTopic = new HubMarketExperienceTopicDO();
        experienceTopic.setMarketAgentId(marketAgent.getId());
        experienceTopic.setTopicId(aiChatTopic.getId());
        experienceTopic.setUserId(getLoginUserId());
        experienceTopic.setSourceAgentId(marketAgent.getAgentId());
        experienceTopic.setSourceTenantId(marketAgent.getSourceTenantId());
        experienceTopic.setAgentType(marketAgent.getAgentType());
        experienceTopic.setLastActiveTime(LocalDateTime.now());
        experienceTopic.setTenantId(TenantContextHolder.getRequiredTenantId());
        experienceTopicMapper.insert(experienceTopic);
        return experienceTopic;
    }

    private HubMarketExperienceTopicDO validateTopic(HubMarketAgentDO marketAgent, Long topicId) {
        HubMarketExperienceTopicDO experienceTopic = experienceTopicMapper
                .selectByTopicAndUserAndMarket(topicId, getLoginUserId(), marketAgent.getId());
        AiChatTopicDO aiChatTopic = aiChatTopicMapper.selectById(topicId);
        if (experienceTopic == null
                || aiChatTopic == null
                || !Objects.equals(aiChatTopic.getCreator(), String.valueOf(getLoginUserId()))
                || !Objects.equals(experienceTopic.getSourceAgentId(), marketAgent.getAgentId())
                || !Objects.equals(experienceTopic.getSourceTenantId(), marketAgent.getSourceTenantId())
                || !Objects.equals(experienceTopic.getAgentType(), marketAgent.getAgentType())) {
            throw exception(MARKET_EXPERIENCE_TOPIC_INVALID);
        }
        return experienceTopic;
    }

    private void refreshLastActiveTime(HubMarketExperienceTopicDO experienceTopic) {
        HubMarketExperienceTopicDO update = new HubMarketExperienceTopicDO();
        update.setId(experienceTopic.getId());
        update.setLastActiveTime(LocalDateTime.now());
        experienceTopicMapper.updateById(update);
        experienceTopic.setLastActiveTime(update.getLastActiveTime());
    }

}
