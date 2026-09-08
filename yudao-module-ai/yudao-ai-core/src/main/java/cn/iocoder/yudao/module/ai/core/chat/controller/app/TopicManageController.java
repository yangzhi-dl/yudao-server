package cn.iocoder.yudao.module.ai.core.chat.controller.app;

import cn.iocoder.yudao.module.ai.core.chat.model.dto.ArchiveTopicOperationDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ChatRenameTopicDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ChatTopicPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.ChatTopicVO;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatTopicService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@Slf4j
@RequestMapping("/ai/topic")
public class TopicManageController {

    private final AiChatTopicService aiChatTopicService;

    public TopicManageController(AiChatTopicService aiChatTopicService) {
        this.aiChatTopicService = aiChatTopicService;
    }

    @PostMapping("/page")
    public CommonResult<PageResult<ChatTopicVO>> page(@RequestBody ChatTopicPageQueryDTO chatTopicPageQueryDto) {
        PageResult<ChatTopicVO> pageResult = aiChatTopicService.chatTopicPageQuery(chatTopicPageQueryDto);
        return success(pageResult);
    }

    @PostMapping("/delete")
    public CommonResult<Boolean> delTopic(@RequestBody List<Long> ids) {
        return success(aiChatTopicService.delTopic(ids));
    }

    @PostMapping("/archive")
    public CommonResult<Boolean> archiveTopic(@RequestBody ArchiveTopicOperationDTO dto) {
        return success(aiChatTopicService.archiveTopic(dto));
    }

    @PostMapping("/rename")
    public CommonResult<Boolean> renameTopic(@RequestBody ChatRenameTopicDTO renameTopicDto) {
        return success(aiChatTopicService.renameTopic(renameTopicDto));
    }

}
