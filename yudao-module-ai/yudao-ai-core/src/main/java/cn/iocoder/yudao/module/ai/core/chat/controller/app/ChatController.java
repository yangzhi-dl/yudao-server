package cn.iocoder.yudao.module.ai.core.chat.controller.app;

import cn.iocoder.yudao.module.ai.core.chat.model.dto.ExportChatHistoryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.RegenerateMessageDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.RegenerateSwitchDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.SendMessageDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ToolPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.ExportChatRecordVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.RegenerateVersionVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.SelectEndpointVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.SelectToolVO;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Slf4j
@RestController
@RequestMapping("/ai/chat")
public class ChatController {

    private final AiChatService aiChatService;

    public ChatController(AiChatService AiChatService) {
        this.aiChatService = AiChatService;
    }

    /**
     * 发送消息
     * @param uuid 唯一标识符号
     * @return SseEmitter
     */
    @PostMapping(value = "/receive/answer/{uuid}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter conversation(@PathVariable Long uuid, @RequestBody SendMessageDTO messageDto) {
        return aiChatService.conversation(uuid, messageDto);
    }

    @GetMapping("/endpoint/list")
    public CommonResult<List<SelectEndpointVO>> selectEndpointList() {
        return success(aiChatService.selectEndpointList());
    }

    @PostMapping("/tool/list")
    public CommonResult<PageResult<SelectToolVO>> selectToolList(@RequestBody ToolPageQueryDTO dto) {
        return success(aiChatService.selectToolList(dto));
    }

    /**
     * 重新生成回答
     * @param uuid 唯一标识符号
     * @param lastId 用户消息 ID（要重答的问题）
     * @param dto 端点配置（endpointId/apiType/modelType/wikiIds/selectedTools）
     * @return SseEmitter
     */
    @PostMapping(value = "/regenerate/answer/{uuid}/{lastId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter regenerate(@PathVariable Long uuid, @PathVariable Long lastId,
                                 @RequestBody RegenerateMessageDTO dto) {
        return aiChatService.regenerate(uuid, lastId, dto);
    }

    /**
     * 查询指定 assistant 消息的重新生成版本列表（第 1 个为当前生效版本）
     */
    @GetMapping("/regenerate/list/{dialogueId}")
    public CommonResult<List<RegenerateVersionVO>> regenerateList(@PathVariable Long dialogueId) {
        return success(aiChatService.regenerateList(dialogueId));
    }

    /**
     * 查询指定话题下所有消息的重新生成版本列表（前端按 dialogueId 分组）
     */
    @GetMapping("/regenerate/topic/list/{topicId}")
    public CommonResult<List<RegenerateVersionVO>> regenerateTopicList(@PathVariable Long topicId) {
        return success(aiChatService.regenerateListByTopic(topicId));
    }

    /**
     * 切换重新生成版本（互换主行与目标版本行内容）
     */
    @PostMapping("/regenerate/switch")
    public CommonResult<List<RegenerateVersionVO>> regenerateSwitch(@RequestBody RegenerateSwitchDTO dto) {
        return success(aiChatService.switchRegenerateVersion(dto.getDialogueId(), dto.getVersionId()));
    }

    @GetMapping("/stop/answer/{uuid}")
    public CommonResult<Boolean> stopConversation(@PathVariable Long uuid) {
        return success(aiChatService.stopConversation(uuid));
    }

    @GetMapping("/clear/history")
    public CommonResult<Boolean> clearHistory() {
        return success(aiChatService.clearHistory());
    }

    @PostMapping("/export/history")
    public CommonResult<ExportChatRecordVO> exportHistory(@RequestBody ExportChatHistoryDTO dto) {
        return success(aiChatService.exportHistory(dto));
    }

    @GetMapping("/export/record")
    public CommonResult<ExportChatRecordVO> exportChatRecord() {
        return success(aiChatService.exportChatRecord());
    }

}