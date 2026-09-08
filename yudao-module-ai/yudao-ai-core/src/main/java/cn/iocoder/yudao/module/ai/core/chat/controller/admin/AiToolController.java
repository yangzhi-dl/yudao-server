package cn.iocoder.yudao.module.ai.core.chat.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.CreateToolDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ToolPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.UpdateToolDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.McpConfig;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ToolFunction;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.ToolDetailVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.ToolPageVO;
import cn.iocoder.yudao.module.ai.core.chat.service.AiToolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Slf4j
@RestController
@RequestMapping("/ai/tool")
public class AiToolController {

    private final AiToolService aiToolService;

    public AiToolController(AiToolService aiToolService) {
        this.aiToolService = aiToolService;
    }

    @PostMapping("/page")
    public CommonResult<PageResult<ToolPageVO>> page(@RequestBody ToolPageQueryDTO dto) {
        PageResult<ToolPageVO> pageResult = aiToolService.pageQuery(dto);
        return success(pageResult);
    }

    @GetMapping("/system/list")
    public CommonResult<List<ToolPageVO>> getAllSystemTools() {
        return success(aiToolService.getAllSystemTools());
    }

    @PostMapping("/create")
    public CommonResult<Boolean> create(@RequestBody CreateToolDTO dto) {
        return success(aiToolService.createTool(dto));
    }

    @GetMapping("/detail/{id}")
    public CommonResult<ToolDetailVO> detail(@PathVariable Long id) {
        return success(aiToolService.detail(id));
    }

    @PostMapping("/delete")
    public CommonResult<Boolean> delete(@RequestBody List<Long> ids) {
        return success(aiToolService.delete(ids));
    }

    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody UpdateToolDTO dto) {
        return success(aiToolService.updateTool(dto));
    }

    @PostMapping("/link/mcp")
    public CommonResult<List<ToolFunction>> linkMcp(@RequestBody McpConfig config) {
        return success(aiToolService.getMcpServiceToolsByConfig(config));
    }

    @GetMapping("/ping/{id}")
    public CommonResult<Boolean> pingTool(@PathVariable Long id) {
        return success(aiToolService.pingTool(id));
    }

}
