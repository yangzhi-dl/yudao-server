package cn.iocoder.yudao.module.ai.core.chat.controller.app;

import cn.iocoder.yudao.module.ai.core.chat.model.dto.ChatDialoguePageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ChatDialogueStarPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.ChatDialogueVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.ChatStarDialogueVO;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatDialogueService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@Slf4j
@RequestMapping("/ai/dialogue")
public class DialogueManageController {

    private final AiChatDialogueService aiChatDialogueService;

    public DialogueManageController(AiChatDialogueService aiChatDialogueService) {
        this.aiChatDialogueService = aiChatDialogueService;
    }

    @PostMapping("/page")
    public CommonResult<PageResult<ChatDialogueVO>> page(@RequestBody ChatDialoguePageQueryDTO chatDialoguePageQueryDto) {
        PageResult<ChatDialogueVO> pageResult = aiChatDialogueService.chatDialoguePageQuery(chatDialoguePageQueryDto);
        return success(pageResult);
    }

    @PostMapping("/star/{status}")
    public CommonResult<Boolean> switchStar(@PathVariable Boolean status, @RequestParam("id") Long id) {
        return success(aiChatDialogueService.switchStar(id, status));
    }

    @PostMapping("/star/page")
    public CommonResult<PageResult<ChatStarDialogueVO>> starPage(@RequestBody ChatDialogueStarPageQueryDTO dto) {
        PageResult<ChatStarDialogueVO> pageResult = aiChatDialogueService.chatDialogueStarPageQuery(dto);
        return success(pageResult);
    }

    @PostMapping("/feedback/{status}")
    public CommonResult<Boolean> exchangeFeedback(@PathVariable Integer status, @RequestParam("id") Long id) {
        return success(aiChatDialogueService.exchangeFeedback(id, status));
    }

    @GetMapping("/delete/{lastId}")
    public CommonResult<Boolean> delDialogue(@PathVariable Long lastId) {
        return success(aiChatDialogueService.delDialogue(lastId));
    }

}
