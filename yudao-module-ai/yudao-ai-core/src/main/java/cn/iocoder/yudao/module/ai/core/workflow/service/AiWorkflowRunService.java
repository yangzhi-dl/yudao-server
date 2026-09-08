package cn.iocoder.yudao.module.ai.core.workflow.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.core.workflow.model.dto.WorkflowRunDTO;
import cn.iocoder.yudao.module.ai.core.workflow.model.vo.WorkflowRunVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI 工作流运行 Service
 */
public interface AiWorkflowRunService {

    /**
     * 启动运行（SSE 流式）
     */
    SseEmitter run(WorkflowRunDTO dto);

    /**
     * 停止运行
     */
    Boolean stop(Long runId);

    /**
     * 运行记录分页（按工作流）
     */
    PageResult<WorkflowRunVO> pageQuery(Long workflowId, int page, int pageSize);

    /**
     * 运行记录详情
     */
    WorkflowRunVO selectRunById(Long id);

    /**
     * 最近运行记录（按工作流，limit 条）
     */
    List<WorkflowRunVO> listRecent(Long workflowId, int limit);
}
