package cn.iocoder.yudao.module.ai.core.chat.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.TokenStatisticsVO;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/nexus/user/statistics")
@Slf4j
public class UserStatisticsPageController {

    public final AiChatTokenService aiChatTokenService;

    public UserStatisticsPageController(AiChatTokenService aiChatTokenService) {
        this.aiChatTokenService = aiChatTokenService;
    }

    @GetMapping("/token/{year}")
    public CommonResult<TokenStatisticsVO> userStatisticsToken(@PathVariable Integer year) {
        return success(aiChatTokenService.selectDailyTokenByYearAndUser(year));
    }

}
