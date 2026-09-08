package cn.iocoder.yudao.module.ai.core.chat.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.CreateModelDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ModelPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.UpdateModelDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.ModelVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.SelectModelVO;
import cn.iocoder.yudao.module.ai.core.chat.service.AiModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/ai/model")
@Slf4j
public class AiModelController {

    private final AiModelService aiModelService;

    public AiModelController(AiModelService aiModelService) {
        this.aiModelService = aiModelService;
    }

    @GetMapping("/list")
    public CommonResult<List<SelectModelVO>> list() {
        return success(aiModelService.selectModelList(true));
    }

    @PostMapping("/page")
    public CommonResult<PageResult<ModelVO>> page(@RequestBody ModelPageQueryDTO modelPageQueryDTO) {
        PageResult<ModelVO> pageResult = aiModelService.pageQuery(modelPageQueryDTO);
        return success(pageResult);
    }

    @PostMapping("/delete")
    public CommonResult<Boolean> deleteModel(@RequestBody List<Long> ids) {
        return success(aiModelService.deleteModelById(ids));
    }

    @PostMapping("/update")
    public CommonResult<Boolean> updateModel(@RequestBody UpdateModelDTO model) {
        return success(aiModelService.updateModel(model));
    }

    @PostMapping("/create")
    public CommonResult<Boolean> createModel(@RequestBody CreateModelDTO model) {
        return success(aiModelService.insertModel(model));
    }

}
