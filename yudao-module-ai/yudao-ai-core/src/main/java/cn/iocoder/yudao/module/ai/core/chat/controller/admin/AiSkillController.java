package cn.iocoder.yudao.module.ai.core.chat.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.CreateSkillDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.CreateSkillResourceDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.SkillPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.UpdateSkillDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.SkillDetailVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.SkillResourceVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.SkillVO;
import cn.iocoder.yudao.module.ai.core.chat.service.AiSkillResourceService;
import cn.iocoder.yudao.module.ai.core.chat.service.AiSkillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Slf4j
@RestController
@RequestMapping("/ai/skill")
public class AiSkillController {

    private final AiSkillService aiSkillService;

    private final AiSkillResourceService aiSkillResourceService;

    public AiSkillController(AiSkillService aiSkillService, AiSkillResourceService aiSkillResourceService) {
        this.aiSkillService = aiSkillService;
        this.aiSkillResourceService = aiSkillResourceService;
    }

    @PostMapping("/page")
    public CommonResult<PageResult<SkillVO>> page(@RequestBody SkillPageQueryDTO dto) {
        PageResult<SkillVO> pageResult = aiSkillService.pageQuery(dto);
        return success(pageResult);
    }

    @PostMapping("/create")
    public CommonResult<Boolean> create(@RequestBody CreateSkillDTO dto) {
        return success(aiSkillService.insert(dto));
    }

    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody UpdateSkillDTO dto) {
        return success(aiSkillService.updateData(dto));
    }

    @PostMapping("/delete")
    public CommonResult<Boolean> delete(@RequestBody List<Long> ids) {
        return success(aiSkillService.delete(ids));
    }

    @GetMapping("/detail/{id}")
    public CommonResult<SkillDetailVO> detail(@PathVariable Long id) {
        return success(aiSkillService.selectSkillById(id));
    }

    // ==================== 资源管理 ====================

    @GetMapping("/resource/list/{skillId}")
    public CommonResult<List<SkillResourceVO>> resourceList(@PathVariable Long skillId) {
        return success(aiSkillResourceService.selectListBySkillId(skillId));
    }

    @PostMapping("/resource/create")
    public CommonResult<Boolean> resourceCreate(@RequestBody CreateSkillResourceDTO dto) {
        return success(aiSkillResourceService.insert(dto));
    }

    @PostMapping("/resource/delete")
    public CommonResult<Boolean> resourceDelete(@RequestBody List<Long> ids) {
        return success(aiSkillResourceService.delete(ids));
    }

}