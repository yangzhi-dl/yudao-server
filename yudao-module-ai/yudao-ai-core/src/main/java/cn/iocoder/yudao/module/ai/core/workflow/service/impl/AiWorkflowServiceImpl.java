package cn.iocoder.yudao.module.ai.core.workflow.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.core.workflow.dal.dataobject.AiWorkflowDO;
import cn.iocoder.yudao.module.ai.core.workflow.dal.mysql.AiWorkflowMapper;
import cn.iocoder.yudao.module.ai.core.workflow.dal.mysql.AiWorkflowRunMapper;
import cn.iocoder.yudao.module.ai.core.workflow.engine.memento.WorkflowSnapshot;
import cn.iocoder.yudao.module.ai.core.workflow.engine.memento.WorkflowSnapshotManager;
import cn.iocoder.yudao.module.ai.core.workflow.engine.state.WorkflowStateManager;
import cn.iocoder.yudao.module.ai.core.workflow.enums.WorkflowStatus;
import cn.iocoder.yudao.module.ai.core.workflow.model.dto.CreateWorkflowDTO;
import cn.iocoder.yudao.module.ai.core.workflow.model.dto.UpdateWorkflowDTO;
import cn.iocoder.yudao.module.ai.core.workflow.model.dto.WorkflowPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.workflow.model.vo.WorkflowDetailVO;
import cn.iocoder.yudao.module.ai.core.workflow.model.vo.WorkflowVO;
import cn.iocoder.yudao.module.ai.core.workflow.service.AiWorkflowService;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.common.constans.ErrorCodeConstants.WORKFLOW_NAME_EXISTS;
import static cn.iocoder.yudao.module.ai.common.constans.ErrorCodeConstants.WORKFLOW_NOT_EXISTS;

/**
 * AI 工作流定义 Service 实现
 */
@Slf4j
@Service
public class AiWorkflowServiceImpl implements AiWorkflowService {

    private final AiWorkflowMapper workflowMapper;
    private final AiWorkflowRunMapper workflowRunMapper;

    /** 工作流快照管理器（备忘录模式），workflowId -> snapshotManager */
    private final ConcurrentHashMap<Long, WorkflowSnapshotManager> snapshotManagers = new ConcurrentHashMap<>();

    public AiWorkflowServiceImpl(AiWorkflowMapper workflowMapper, AiWorkflowRunMapper workflowRunMapper) {
        this.workflowMapper = workflowMapper;
        this.workflowRunMapper = workflowRunMapper;
    }

    @Override
    public PageResult<WorkflowVO> pageQuery(WorkflowPageQueryDTO dto) {
        Page<AiWorkflowDO> page = workflowMapper.selectPage(new Page<>(dto.getPage(), dto.getPageSize()),
                new LambdaQueryWrapperX<AiWorkflowDO>()
                        .likeIfPresent(AiWorkflowDO::getName, dto.getName())
                        .eqIfPresent(AiWorkflowDO::getStatus, dto.getStatus())
                        .eq(AiWorkflowDO::getDeleted, false)
                        .orderByDesc(AiWorkflowDO::getId));
        List<WorkflowVO> list = page.getRecords().stream().map(this::convert).toList();
        return new PageResult<>(list, page.getTotal());
    }

    private WorkflowVO convert(AiWorkflowDO workflow) {
        WorkflowVO vo = new WorkflowVO();
        BeanUtils.copyProperties(workflow, vo);
        return vo;
    }

    @Override
    public WorkflowDetailVO selectWorkflowById(Long id) {
        AiWorkflowDO workflow = getWorkflow(id);
        WorkflowDetailVO vo = new WorkflowDetailVO();
        BeanUtils.copyProperties(workflow, vo);
        return vo;
    }

    @Override
    public AiWorkflowDO selectRunnableWorkflow(Long id) {
        AiWorkflowDO workflow = getWorkflow(id);
        if (workflow.getPublishedGraph() != null) {
            AiWorkflowDO runnable = new AiWorkflowDO();
            BeanUtils.copyProperties(workflow, runnable);
            runnable.setGraph(workflow.getPublishedGraph());
            return runnable;
        }
        return workflow;
    }

    private AiWorkflowDO getWorkflow(Long id) {
        AiWorkflowDO workflow = workflowMapper.selectById(id);
        if (workflow == null) {
            throw exception(WORKFLOW_NOT_EXISTS);
        }
        return workflow;
    }

    @Override
    public Long insert(CreateWorkflowDTO dto) {
        checkNameUnique(dto.getName(), null);
        AiWorkflowDO workflow = AiWorkflowDO.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .icon(dto.getIcon())
                .graph(dto.getGraph())
                .status(WorkflowStatus.DRAFT.status)
                .version(1)
                .usageCount(0L)
                .build();
        workflowMapper.insert(workflow);
        return workflow.getId();
    }

    @Override
    public Boolean updateData(UpdateWorkflowDTO dto) {
        checkNameUnique(dto.getName(), dto.getId());
        AiWorkflowDO update = AiWorkflowDO.builder().id(dto.getId()).build();
        if (StringUtils.hasText(dto.getName())) {
            update.setName(dto.getName());
        }
        update.setDescription(dto.getDescription());
        update.setIcon(dto.getIcon());
        if (dto.getGraph() != null) {
            update.setGraph(dto.getGraph());
        }
        workflowMapper.updateById(update);
        return true;
    }

    private void checkNameUnique(String name, Long excludeId) {
        if (!StringUtils.hasText(name)) {
            return;
        }
        AiWorkflowDO exists = workflowMapper.selectByName(name, excludeId);
        if (exists != null) {
            throw exception(WORKFLOW_NAME_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copy(Long id) {
        AiWorkflowDO source = getWorkflow(id);
        AiWorkflowDO copy = AiWorkflowDO.builder()
                .name(source.getName() + "-副本")
                .description(source.getDescription())
                .icon(source.getIcon())
                .graph(source.getGraph())
                .status(WorkflowStatus.DRAFT.status)
                .version(1)
                .usageCount(0L)
                .build();
        workflowMapper.insert(copy);
        return copy.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean publish(Long id) {
        AiWorkflowDO workflow = getWorkflow(id);
        if (workflow.getGraph() == null) {
            throw new ServiceException(500, "工作流画布为空，无法发布");
        }

        // 状态模式：检查状态转换合法性
        WorkflowStateManager stateManager = new WorkflowStateManager(workflow.getStatus());
        stateManager.publish();

        int newVersion = (workflow.getVersion() == null ? 0 : workflow.getVersion()) + 1;

        // 备忘录模式：保存快照
        WorkflowSnapshotManager snapshotManager = getOrCreateSnapshotManager(id);
        snapshotManager.saveSnapshot(workflow.getGraph(), "发布版本 v" + newVersion);

        AiWorkflowDO update = AiWorkflowDO.builder()
                .id(id)
                .publishedGraph(workflow.getGraph())
                .status(stateManager.getStatusCode())
                .version(newVersion)
                .build();
        workflowMapper.updateById(update);
        return true;
    }

    @Override
    public Boolean offline(Long id) {
        AiWorkflowDO workflow = getWorkflow(id);

        // 状态模式：检查状态转换合法性
        WorkflowStateManager stateManager = new WorkflowStateManager(workflow.getStatus());
        stateManager.offline();

        AiWorkflowDO update = AiWorkflowDO.builder()
                .id(id)
                .status(stateManager.getStatusCode())
                .build();
        workflowMapper.updateById(update);
        return true;
    }

    /**
     * 获取或创建快照管理器（备忘录模式）
     */
    public WorkflowSnapshotManager getOrCreateSnapshotManager(Long workflowId) {
        return snapshotManagers.computeIfAbsent(workflowId, k -> new WorkflowSnapshotManager());
    }

    /**
     * 获取工作流快照列表
     */
    public List<WorkflowSnapshot> getSnapshots(Long workflowId) {
        WorkflowSnapshotManager manager = snapshotManagers.get(workflowId);
        return manager == null ? List.of() : manager.getAllSnapshots();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<Long> ids) {
        workflowMapper.delete(ids);
        workflowRunMapper.deleteByWorkflowIds(ids);
        return true;
    }

    @Override
    public Boolean increaseUsageCount(Long id) {
        return workflowMapper.increaseUsageCount(id);
    }
}
