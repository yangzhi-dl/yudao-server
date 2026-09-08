package cn.iocoder.yudao.module.ai.core.workflow.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.core.workflow.engine.node.ApiNodeHandler;
import cn.iocoder.yudao.module.ai.core.workflow.model.dto.CreateWorkflowDTO;
import cn.iocoder.yudao.module.ai.core.workflow.model.dto.UpdateWorkflowDTO;
import cn.iocoder.yudao.module.ai.core.workflow.model.dto.WorkflowPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.workflow.model.vo.WorkflowDetailVO;
import cn.iocoder.yudao.module.ai.core.workflow.model.vo.WorkflowVO;
import cn.iocoder.yudao.module.ai.core.workflow.service.AiWorkflowService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * AI 工作流定义管理 Controller
 *
 * @author yudao
 */
@Slf4j
@RestController
@RequestMapping("/ai/workflow")
public class AiWorkflowController {

    private final AiWorkflowService aiWorkflowService;
    private final ApiNodeHandler apiNodeHandler;

    public AiWorkflowController(AiWorkflowService aiWorkflowService,
                                ApiNodeHandler apiNodeHandler) {
        this.aiWorkflowService = aiWorkflowService;
        this.apiNodeHandler = apiNodeHandler;
    }

    /**
     * 测试 API 节点请求（前端「Send」按钮）：按节点配置发起请求，返回状态码与响应体。
     * 不依赖工作流执行内存，仅做连通性/格式校验。
     */
    @PostMapping("/api-node/test")
    public CommonResult<Map<String, Object>> apiNodeTest(@RequestBody Map<String, Object> config) {
        return success(apiNodeHandler.sendRequest(new JSONObject(config), null));
    }

    @PostMapping("/page")
    public CommonResult<PageResult<WorkflowVO>> page(@RequestBody WorkflowPageQueryDTO dto) {
        return success(aiWorkflowService.pageQuery(dto));
    }

    @GetMapping("/detail/{id}")
    public CommonResult<WorkflowDetailVO> detail(@PathVariable Long id) {
        return success(aiWorkflowService.selectWorkflowById(id));
    }

    @PostMapping("/create")
    public CommonResult<Long> create(@RequestBody CreateWorkflowDTO dto) {
        return success(aiWorkflowService.insert(dto));
    }

    @PostMapping("/update")
    public CommonResult<Boolean> update(@RequestBody UpdateWorkflowDTO dto) {
        return success(aiWorkflowService.updateData(dto));
    }

    @PostMapping("/copy/{id}")
    public CommonResult<Long> copy(@PathVariable Long id) {
        return success(aiWorkflowService.copy(id));
    }

    @PostMapping("/publish/{id}")
    public CommonResult<Boolean> publish(@PathVariable Long id) {
        return success(aiWorkflowService.publish(id));
    }

    @PostMapping("/offline/{id}")
    public CommonResult<Boolean> offline(@PathVariable Long id) {
        return success(aiWorkflowService.offline(id));
    }

    @PostMapping("/delete")
    public CommonResult<Boolean> delete(@RequestBody List<Long> ids) {
        return success(aiWorkflowService.delete(ids));
    }

}
