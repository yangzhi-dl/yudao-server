package cn.iocoder.yudao.module.ai.core.workflow.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.core.workflow.dal.dataobject.AiWorkflowRunDO;
import cn.iocoder.yudao.module.ai.core.workflow.dal.mysql.AiWorkflowRunMapper;
import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowExecutor;
import cn.iocoder.yudao.module.ai.core.workflow.model.dto.WorkflowRunDTO;
import cn.iocoder.yudao.module.ai.core.workflow.model.vo.WorkflowDetailVO;
import cn.iocoder.yudao.module.ai.core.workflow.model.vo.WorkflowRunVO;
import cn.iocoder.yudao.module.ai.core.workflow.service.AiWorkflowRunService;
import cn.iocoder.yudao.module.ai.core.workflow.service.AiWorkflowService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

/**
 * AI 工作流运行 Service 实现
 */
@Slf4j
@Service
public class AiWorkflowRunServiceImpl implements AiWorkflowRunService {

    private final WorkflowExecutor workflowExecutor;
    private final AiWorkflowRunMapper workflowRunMapper;
    private final AiWorkflowService workflowService;

    public AiWorkflowRunServiceImpl(WorkflowExecutor workflowExecutor,
                                    AiWorkflowRunMapper workflowRunMapper,
                                    AiWorkflowService workflowService) {
        this.workflowExecutor = workflowExecutor;
        this.workflowRunMapper = workflowRunMapper;
        this.workflowService = workflowService;
    }

    @Override
    public SseEmitter run(WorkflowRunDTO dto) {
        return workflowExecutor.run(dto);
    }

    @Override
    public Boolean stop(Long runId) {
        return workflowExecutor.stop(runId);
    }

    @Override
    public PageResult<WorkflowRunVO> pageQuery(Long workflowId, int page, int pageSize) {
        Page<AiWorkflowRunDO> result = workflowRunMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapperX<AiWorkflowRunDO>()
                        .eqIfPresent(AiWorkflowRunDO::getWorkflowId, workflowId)
                        .eq(AiWorkflowRunDO::getDeleted, false)
                        .orderByDesc(AiWorkflowRunDO::getId));
        List<WorkflowRunVO> list = result.getRecords().stream().map(this::convert).toList();
        return new PageResult<>(list, result.getTotal());
    }

    @Override
    public WorkflowRunVO selectRunById(Long id) {
        AiWorkflowRunDO runDO = workflowRunMapper.selectById(id);
        return runDO == null ? null : convert(runDO);
    }

    @Override
    public List<WorkflowRunVO> listRecent(Long workflowId, int limit) {
        return workflowRunMapper.selectListByWorkflowId(workflowId, limit).stream().map(this::convert).toList();
    }

    private WorkflowRunVO convert(AiWorkflowRunDO runDO) {
        WorkflowRunVO vo = new WorkflowRunVO();
        BeanUtils.copyProperties(runDO, vo);
        vo.setInputs(parseJsonField(runDO.getInputs()));
        vo.setOutputs(parseJsonField(runDO.getOutputs()));
        vo.setNodeLogs(parseJsonField(runDO.getNodeLogs()));
        vo.setFileInfos(parseJsonList(runDO.getFileInfos()));
        try {
            WorkflowDetailVO workflow = workflowService.selectWorkflowById(runDO.getWorkflowId());
            if (workflow != null) {
                vo.setWorkflowName(workflow.getName());
            }
        } catch (Exception e) {
            log.warn("[workflow][run({})] 查询工作流信息失败", runDO.getWorkflowId());
        }
        return vo;
    }

    /**
     * 将 JSON 字段统一解析为 List：兼容已解析的 List、以及以 String 形式读回两种场景
     */
    @SuppressWarnings("unchecked")
    private List<Object> parseJsonList(Object value) {
        switch (value) {
            case null -> {
                return null;
            }
            case List _ -> {
                return (List<Object>) value;
            }
            case String json when StringUtils.hasText(json) -> {
                try {
                    return JsonUtils.parseArray(json, Object.class);
                } catch (Exception e) {
                    log.warn("[workflow][run] 解析 JSON 列表字段失败: {}", e.getMessage());
                }
            }
            default -> {
            }
        }
        return null;
    }

    /**
     * 将 JSON 字段统一解析为 Map：兼容已解析的 Map、以及以 String 形式读回两种场景
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonField(Object value) {
        switch (value) {
            case null -> {
                return null;
            }
            case Map _ -> {
                return (Map<String, Object>) value;
            }
            case String json when StringUtils.hasText(json) -> {
                try {
                    return JsonUtils.parseObject(json, new TypeReference<>() {
                    });
                } catch (Exception e) {
                    log.warn("[workflow][run] 解析 JSON 字段失败: {}", e.getMessage());
                }
            }
            default -> {
            }
        }
        return null;
    }
}
