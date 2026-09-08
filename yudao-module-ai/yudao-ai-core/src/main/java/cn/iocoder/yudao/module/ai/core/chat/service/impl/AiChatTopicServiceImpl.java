package cn.iocoder.yudao.module.ai.core.chat.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.core.chat.dal.mysql.AiChatTopicMapper;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiChatTopicDO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ArchiveTopicOperationDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ChatRenameTopicDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ChatTopicPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ArchivedStatus;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.TopicRange;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.ChatTopicVO;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatDialogueService;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatTopicService;
import cn.iocoder.yudao.module.ai.common.model.entity.DeletedStatus;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Service
@Slf4j
public class AiChatTopicServiceImpl implements AiChatTopicService {

    private final AiChatDialogueService aiChatDialogueService;

    private final AiChatTopicMapper aiChatTopicMapper;

    public AiChatTopicServiceImpl(AiChatDialogueService aiChatDialogueService,
                                  AiChatTopicMapper aiChatTopicMapper) {
        this.aiChatDialogueService = aiChatDialogueService;
        this.aiChatTopicMapper = aiChatTopicMapper;
    }

    @Override
    public PageResult<ChatTopicVO> chatTopicPageQuery(ChatTopicPageQueryDTO pageQueryDto) {
        AiChatTopicDO aiChatTopicDO = AiChatTopicDO.builder().title(pageQueryDto.getTitle())
                .deleted(false).isArchived(pageQueryDto.getIsArchived()).build();
        String loginUserId = String.valueOf(getLoginUserId());
        aiChatTopicDO.setCreator(loginUserId);
        BeanUtils.copyProperties(pageQueryDto, aiChatTopicDO);
        Page<ChatTopicVO> chatTopicVOS = aiChatTopicMapper.pageQuery(new Page<>(pageQueryDto.getPage(), pageQueryDto.getPageSize()), aiChatTopicDO);
        return new PageResult<>(chatTopicVOS.getRecords(), chatTopicVOS.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertTopic(AiChatTopicDO aiChatTopicDO) {
        aiChatTopicMapper.insert(aiChatTopicDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delTopic(List<Long> ids) {
        if (!ids.isEmpty()) {
            DeletedStatus deletedStatus = DeletedStatus.builder()
                    .deleted(true).ids(ids).build();
            aiChatDialogueService.updateDeletedByTopicIds(ids);
            return aiChatTopicMapper.updateTopicDeleted(deletedStatus);
        }
        return false;
    }

    @Override
    public Boolean clearAllTopicByIgnoreIds(List<Long> ignoreIds) {
        String loginUserId = String.valueOf(getLoginUserId());
        return aiChatTopicMapper.clearAllTopicByIgnoreIds(loginUserId, ignoreIds);
    }

    @Override
    public List<Long> getArchiveTopicId() {
        String loginUserId = String.valueOf(getLoginUserId());
        return aiChatTopicMapper.getArchiveTopicId(loginUserId);
    }

    @Override
    public Boolean renameTopic(ChatRenameTopicDTO renameTopicDto) {
        return aiChatTopicMapper.updateById(AiChatTopicDO.builder()
                .id(renameTopicDto.getId())
                .title(renameTopicDto.getTitle()).build()) > 0;
    }

    @Override
    public Boolean archiveTopic(ArchiveTopicOperationDTO dto) {
        List<Long> ids = dto.getIds();
        if (!ids.isEmpty()) {
            ArchivedStatus deletedStatus = ArchivedStatus.builder()
                    .isArchived(dto.getIsArchived()).ids(ids).build();
            return aiChatTopicMapper.updateTopicArchived(deletedStatus);
        }
        return false;
    }

    @Override
    public List<TopicRange> selectTopicsByCreateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return aiChatTopicMapper.selectTopicsByCreateTimeRange(startTime, endTime);
    }

}
