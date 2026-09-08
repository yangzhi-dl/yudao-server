package cn.iocoder.yudao.module.hub.core.market.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceContextRespVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceMessagePageReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceMessageRespVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceSendReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceTopicPageReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceTopicRenameReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceTopicRespVO;
import cn.iocoder.yudao.module.hub.core.market.service.HubMarketExperienceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户前台 - Hub 智能体市场体验")
@RestController
@RequestMapping("/hub/market/experience")
@Validated
public class HubMarketExperienceController {

    @Resource
    private HubMarketExperienceService marketExperienceService;

    @GetMapping("/context")
    @Operation(summary = "获得智能体市场体验上下文")
    @Parameter(name = "id", description = "市场记录编号", required = true, example = "1")
    public CommonResult<HubMarketExperienceContextRespVO> getExperienceContext(
            @RequestParam("id") @NotNull(message = "市场记录编号不能为空") Long id) {
        return success(marketExperienceService.getExperienceContext(id));
    }

    @PostMapping(value = "/chat/receive/answer/{uuid}",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "发送智能体市场体验消息")
    public SseEmitter sendMessage(
            @PathVariable("uuid") @NotNull(message = "会话唯一标识不能为空") Long uuid,
            @Valid @RequestBody HubMarketExperienceSendReqVO reqVO) {
        return marketExperienceService.sendMessage(uuid, reqVO);
    }

    @GetMapping("/topic/page")
    @Operation(summary = "获得当前用户的智能体体验会话分页")
    public CommonResult<PageResult<HubMarketExperienceTopicRespVO>> getTopicPage(
            @Valid HubMarketExperienceTopicPageReqVO reqVO) {
        return success(marketExperienceService.getTopicPage(reqVO));
    }

    @GetMapping("/message/page")
    @Operation(summary = "获得智能体体验会话消息分页")
    public CommonResult<PageResult<HubMarketExperienceMessageRespVO>> getMessagePage(
            @Valid HubMarketExperienceMessagePageReqVO reqVO) {
        return success(marketExperienceService.getMessagePage(reqVO));
    }

    @PutMapping("/topic/rename")
    @Operation(summary = "重命名智能体体验会话")
    public CommonResult<Boolean> renameTopic(
            @Valid @RequestBody HubMarketExperienceTopicRenameReqVO reqVO) {
        marketExperienceService.renameTopic(reqVO);
        return success(true);
    }

    @DeleteMapping("/topic/delete")
    @Operation(summary = "删除智能体体验会话")
    public CommonResult<Boolean> deleteTopic(
            @RequestParam("marketId") @NotNull(message = "市场记录编号不能为空") Long marketId,
            @RequestParam("topicId") @NotNull(message = "AI 会话主题编号不能为空") Long topicId) {
        marketExperienceService.deleteTopic(marketId, topicId);
        return success(true);
    }

}
