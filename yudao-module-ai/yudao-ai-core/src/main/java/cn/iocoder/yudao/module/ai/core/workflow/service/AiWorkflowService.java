package cn.iocoder.yudao.module.ai.core.workflow.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.core.workflow.dal.dataobject.AiWorkflowDO;
import cn.iocoder.yudao.module.ai.core.workflow.model.dto.CreateWorkflowDTO;
import cn.iocoder.yudao.module.ai.core.workflow.model.dto.UpdateWorkflowDTO;
import cn.iocoder.yudao.module.ai.core.workflow.model.dto.WorkflowPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.workflow.model.vo.WorkflowDetailVO;
import cn.iocoder.yudao.module.ai.core.workflow.model.vo.WorkflowVO;

import java.util.List;

/**
 * AI 工作流定义 Service
 */
public interface AiWorkflowService {

    PageResult<WorkflowVO> pageQuery(WorkflowPageQueryDTO dto);

    WorkflowDetailVO selectWorkflowById(Long id);

    /**
     * 查询运行用工作流（优先已发布快照；未发布则用当前画布）
     */
    AiWorkflowDO selectRunnableWorkflow(Long id);

    /**
     * 创建工作流，返回新工作流 ID
     */
    Long insert(CreateWorkflowDTO dto);

    Boolean updateData(UpdateWorkflowDTO dto);

    /**
     * 复制工作流（深拷贝画布）
     */
    Long copy(Long id);

    /**
     * 发布：将当前画布快照到 publishedGraph，版本 +1，状态置为已发布
     */
    Boolean publish(Long id);

    /**
     * 下架
     */
    Boolean offline(Long id);

    Boolean delete(List<Long> ids);

    Boolean increaseUsageCount(Long id);
}
