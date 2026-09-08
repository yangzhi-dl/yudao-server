package cn.iocoder.yudao.module.ai.core.chat.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.AgentPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.CreateAgentDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.UpdateAgentDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.AgentDetailVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.AgentVO;
import cn.iocoder.yudao.module.ai.core.chat.service.AiAgentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Slf4j
@RestController
@RequestMapping("/ai/agent")
public class AiAgentController {

    private final AiAgentService aiAgentService;

    public AiAgentController(AiAgentService aiAgentService) {
        this.aiAgentService = aiAgentService;
    }

    @PostMapping("/page")
    public CommonResult<PageResult<AgentVO>> page(@RequestBody AgentPageQueryDTO dto) {
        PageResult<AgentVO> pageResult = aiAgentService.pageQuery(dto);
        return success(pageResult);
    }

    @PostMapping("/create")
    public CommonResult<Boolean> create(@RequestBody CreateAgentDTO dto) {
        return success(aiAgentService.insert(dto));
    }

    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody UpdateAgentDTO dto) {
        return success(aiAgentService.updateData(dto));
    }

    @PostMapping("/delete")
    public CommonResult<Boolean> delete(@RequestBody List<Long> ids) {
        return success(aiAgentService.delete(ids));
    }

    @GetMapping("/detail/{id}")
    public CommonResult<AgentDetailVO> detail(@PathVariable Long id) {
        return success(aiAgentService.selectAgentById(id));
    }

}
