package cn.iocoder.yudao.module.ai.core.chat.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.common.model.dto.BasePageListDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.UsageProfessionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/nexus/user/profession")
@Slf4j
public class UserAiProfessionController {

    @PostMapping("/page/usage")
    public CommonResult<PageResult<UsageProfessionVO>> getUsageRecordWorkflowPageList(@RequestBody BasePageListDTO dto) {
        return success(PageResult.empty());
    }

}
