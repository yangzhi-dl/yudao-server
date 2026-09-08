package cn.iocoder.yudao.module.ai.core.chat.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiChatTopicDO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ArchiveTopicOperationDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ChatRenameTopicDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ChatTopicPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.TopicRange;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.ChatTopicVO;

import java.time.LocalDateTime;
import java.util.List;

public interface AiChatTopicService {

    PageResult<ChatTopicVO> chatTopicPageQuery(ChatTopicPageQueryDTO topicPageQueryDto);

    void insertTopic(AiChatTopicDO aiChatTopicDO);

    Boolean delTopic(List<Long> ids);

    Boolean clearAllTopicByIgnoreIds(List<Long> ignoreIds);

    List<Long> getArchiveTopicId();

    Boolean renameTopic(ChatRenameTopicDTO renameTopicDto);

    Boolean archiveTopic(ArchiveTopicOperationDTO dto);

    List<TopicRange> selectTopicsByCreateTimeRange(LocalDateTime startTime, LocalDateTime endTime);

}
