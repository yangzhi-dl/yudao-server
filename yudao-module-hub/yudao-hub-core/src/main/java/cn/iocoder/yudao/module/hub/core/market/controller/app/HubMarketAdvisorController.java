package cn.iocoder.yudao.module.hub.core.market.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAdvisorContextRespVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAdvisorMessagePageReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAdvisorTopicPageReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAdvisorTopicRenameReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAdvisorTopicRespVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketAdvisorSendReqVO;
import cn.iocoder.yudao.module.hub.core.market.controller.app.vo.HubMarketExperienceMessageRespVO;
import cn.iocoder.yudao.module.hub.core.market.service.HubMarketAdvisorService;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "用户前台 - Hub 市场选型助手")
@RestController
@RequestMapping("/hub/market/advisor")
@Validated
public class HubMarketAdvisorController {

    @Resource
    private HubMarketAdvisorService marketAdvisorService;

    @GetMapping("/context")
    @Operation(summary = "获得市场选型助手上下文")
    public CommonResult<HubMarketAdvisorContextRespVO> getAdvisorContext() {
        return success(marketAdvisorService.getAdvisorContext());
    }

    @PostMapping(value = "/chat/receive/answer/{uuid}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "发送市场选型助手消息")
    public SseEmitter sendMessage(@PathVariable("uuid") @NotNull(message = "会话唯一标识不能为空") Long uuid,
                                  @Valid @RequestBody HubMarketAdvisorSendReqVO reqVO) {
        return marketAdvisorService.sendMessage(uuid, reqVO);
    }

    @GetMapping("/topic/page")
    @Operation(summary = "获得当前用户的市场选型助手会话分页")
    public CommonResult<PageResult<HubMarketAdvisorTopicRespVO>> getTopicPage(
            @Valid HubMarketAdvisorTopicPageReqVO reqVO) {
        return success(marketAdvisorService.getTopicPage(reqVO));
    }

    @GetMapping("/message/page")
    @Operation(summary = "获得市场选型助手会话消息分页")
    public CommonResult<PageResult<HubMarketExperienceMessageRespVO>> getMessagePage(
            @Valid HubMarketAdvisorMessagePageReqVO reqVO) {
        return success(marketAdvisorService.getMessagePage(reqVO));
    }

    @PutMapping("/topic/rename")
    @Operation(summary = "重命名市场选型助手会话")
    public CommonResult<Boolean> renameTopic(@Valid @RequestBody HubMarketAdvisorTopicRenameReqVO reqVO) {
        marketAdvisorService.renameTopic(reqVO);
        return success(true);
    }

    @DeleteMapping("/topic/delete")
    @Operation(summary = "删除市场选型助手会话")
    public CommonResult<Boolean> deleteTopic(
            @RequestParam("topicId") @NotNull(message = "AI 会话主题编号不能为空") Long topicId) {
        marketAdvisorService.deleteTopic(topicId);
        return success(true);
    }

}
