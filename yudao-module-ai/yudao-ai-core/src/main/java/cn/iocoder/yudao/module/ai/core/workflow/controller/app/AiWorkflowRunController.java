package cn.iocoder.yudao.module.ai.core.workflow.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.core.workflow.model.dto.WorkflowRunDTO;
import cn.iocoder.yudao.module.ai.core.workflow.model.vo.WorkflowRunVO;
import cn.iocoder.yudao.module.ai.core.workflow.service.AiWorkflowRunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * AI 工作流运行 Controller（测试问答、运行记录）
 *
 * @author yudao
 */
@Slf4j
@RestController
@RequestMapping("/ai/workflow/run")
public class AiWorkflowRunController {

    private final AiWorkflowRunService aiWorkflowRunService;

    public AiWorkflowRunController(AiWorkflowRunService aiWorkflowRunService) {
        this.aiWorkflowRunService = aiWorkflowRunService;
    }

    /**
     * 运行工作流（SSE 流式：run_started / node_started / text_delta / node_finished / run_finished / run_error）
     */
    @PostMapping(value = "/test", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter run(@RequestBody WorkflowRunDTO dto) {
        return aiWorkflowRunService.run(dto);
    }

    /**
     * 停止运行
     */
    @GetMapping("/stop/{runId}")
    public CommonResult<Boolean> stop(@PathVariable Long runId) {
        return success(aiWorkflowRunService.stop(runId));
    }

    /**
     * 运行记录分页
     */
    @PostMapping("/page")
    public CommonResult<PageResult<WorkflowRunVO>> page(@RequestBody WorkflowRunPageQueryDTO dto) {
        return success(aiWorkflowRunService.pageQuery(dto.getWorkflowId(), dto.getPage(), dto.getPageSize()));
    }

    /**
     * 运行记录详情
     */
    @GetMapping("/detail/{id}")
    public CommonResult<WorkflowRunVO> detail(@PathVariable Long id) {
        return success(aiWorkflowRunService.selectRunById(id));
    }

    /**
     * 最近运行记录
     */
    @GetMapping("/recent/{workflowId}")
    public CommonResult<List<WorkflowRunVO>> recent(@PathVariable Long workflowId,
                                                    @RequestParam(defaultValue = "10") int limit) {
        return success(aiWorkflowRunService.listRecent(workflowId, limit));
    }

}
