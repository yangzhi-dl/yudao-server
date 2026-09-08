package cn.iocoder.yudao.module.ai.core.workflow.engine;

import cn.iocoder.yudao.module.ai.common.enums.TaskPriority;
import cn.iocoder.yudao.module.ai.common.task.SmartTaskScheduler;
import cn.iocoder.yudao.module.ai.core.workflow.dal.dataobject.AiWorkflowDO;
import cn.iocoder.yudao.module.ai.core.workflow.dal.dataobject.AiWorkflowRunDO;
import cn.iocoder.yudao.module.ai.core.workflow.dal.mysql.AiWorkflowRunMapper;
import cn.iocoder.yudao.module.ai.core.workflow.engine.factory.WorkflowNodeHandlerFactory;
import cn.iocoder.yudao.module.ai.core.workflow.engine.handler.NodeExecutionPipeline;
import cn.iocoder.yudao.module.ai.core.workflow.engine.handler.WorkflowNodeHandlerAdapter;
import cn.iocoder.yudao.module.ai.core.workflow.engine.node.EndNodeHandler;
import cn.iocoder.yudao.module.ai.core.workflow.engine.node.IfElseNodeHandler;
import cn.iocoder.yudao.module.ai.core.workflow.engine.node.ResourceNodeHandler;
import cn.iocoder.yudao.module.ai.core.workflow.engine.node.StartNodeHandler;
import cn.iocoder.yudao.module.ai.core.workflow.engine.state.WorkflowStateManager;
import cn.iocoder.yudao.module.ai.core.workflow.enums.WorkflowRunStatus;
import cn.iocoder.yudao.module.ai.core.workflow.model.dto.WorkflowRunDTO;
import cn.iocoder.yudao.module.ai.core.workflow.service.AiWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工作流执行器（自研引擎）
 * <p>
 * 采用多种设计模式：
 * <ul>
 *   <li>模板方法模式：execute() 定义执行骨架</li>
 *   <li>策略模式：WorkflowNodeHandlerAdapter 实现作为节点策略</li>
 *   <li>责任链模式：NodeExecutionPipeline 管理拦截器链</li>
 *   <li>工厂模式：WorkflowNodeHandlerFactory 管理节点处理器</li>
 *   <li>观察者模式：SSE 事件推送</li>
 *   <li>状态模式：WorkflowStateManager 管理生命周期</li>
 * </ul>
 *
 * @author yudao
 */
@Slf4j
@Component
public class WorkflowExecutor {

    private final Map<Long, WorkflowRunContext> runningContexts = new ConcurrentHashMap<>();

    private final AiWorkflowService workflowService;
    private final AiWorkflowRunMapper workflowRunMapper;
    private final SmartTaskScheduler smartTaskScheduler;
    private final WorkflowNodeHandlerFactory handlerFactory;
    private final IfElseNodeHandler ifElseHandler;

    public WorkflowExecutor(AiWorkflowService workflowService,
                            AiWorkflowRunMapper workflowRunMapper,
                            SmartTaskScheduler smartTaskScheduler,
                            WorkflowNodeHandlerFactory handlerFactory,
                            IfElseNodeHandler ifElseHandler) {
        this.workflowService = workflowService;
        this.workflowRunMapper = workflowRunMapper;
        this.smartTaskScheduler = smartTaskScheduler;
        this.handlerFactory = handlerFactory;
        this.ifElseHandler = ifElseHandler;
    }

    public SseEmitter run(WorkflowRunDTO dto) {
        SseEmitter emitter = new SseEmitter(0L);
        emitter.onTimeout(() -> log.warn("[workflow] SSE 连接超时，workflowId={}", dto.getWorkflowId()));
        smartTaskScheduler.submit(() -> execute(dto, emitter), TaskPriority.NORMAL, "workflow");
        return emitter;
    }

    public Boolean stop(Long runId) {
        WorkflowRunContext ctx = runningContexts.get(runId);
        if (ctx == null) {
            log.warn("[workflow] 停止失败：运行记录不存在或已结束，runId={}", runId);
            return false;
        }
        ctx.requestStop();
        ctx.sendEvent("run_stopped", Map.of("runId", runId));
        return true;
    }

    private void execute(WorkflowRunDTO dto, SseEmitter emitter) {
        AiWorkflowRunDO runDO = null;
        WorkflowRunContext ctx = null;
        try {
            AiWorkflowDO workflow = workflowService.selectRunnableWorkflow(dto.getWorkflowId());

            // 状态模式检查：仅已发布或草稿状态可运行
            WorkflowStateManager stateManager = new WorkflowStateManager(workflow.getStatus());
            if (!stateManager.canRun()) {
                throw new IllegalStateException("工作流当前状态不允许运行");
            }

            runDO = AiWorkflowRunDO.builder()
                    .workflowId(dto.getWorkflowId())
                    .status(WorkflowRunStatus.RUNNING.status)
                    .inputs(dto.getInputs())
                    .build();
            workflowRunMapper.insert(runDO);

            ctx = new WorkflowRunContext();
            ctx.setRunId(runDO.getId());
            ctx.setWorkflow(workflow);
            ctx.setInputs(dto.getInputs() == null ? Map.of() : dto.getInputs());
            ctx.setEmitter(emitter);
            runningContexts.put(runDO.getId(), ctx);

            ctx.sendEvent("run_started", Map.of(
                    "runId", runDO.getId(),
                    "workflowId", dto.getWorkflowId(),
                    "workflowName", workflow.getName()));

            GraphResult result = executeGraph(ctx);

            boolean stopped = ctx.getStopped().get();
            runDO.setStatus(stopped ? WorkflowRunStatus.STOPPED.status : WorkflowRunStatus.SUCCESS.status);
            // 输入：开始节点解析后的内容；输出：结束节点整理后的最终结果
            if (result.startOutputs() != null && !result.startOutputs().isEmpty()) {
                runDO.setInputs(result.startOutputs());
            }
            runDO.setOutputs(result.finalOutputs());
            runDO.setNodeLogs(ctx.getNodeLogs());
            runDO.setFileInfos(ctx.getPresentedFiles());
            runDO.setElapsedMs(ctx.elapsedMs());
            workflowRunMapper.updateById(runDO);
            if (!stopped) {
                workflowService.increaseUsageCount(dto.getWorkflowId());
                ctx.sendEvent("run_finished", Map.of(
                        "runId", runDO.getId(),
                        "status", runDO.getStatus(),
                        "outputs", result.finalOutputs(),
                        "elapsedMs", ctx.elapsedMs()));
            } else {
                ctx.sendEvent("run_stopped", Map.of("runId", runDO.getId()));
            }
        } catch (Exception e) {
            boolean stopped = ctx != null && ctx.getStopped().get();
            log.error("[workflow] 运行失败，workflowId={}", dto.getWorkflowId(), e);
            if (runDO != null) {
                runDO.setStatus(stopped ? WorkflowRunStatus.STOPPED.status : WorkflowRunStatus.FAILED.status);
                runDO.setError(truncateError(e.getMessage()));
                if (ctx != null) {
                    runDO.setNodeLogs(ctx.getNodeLogs());
                    runDO.setFileInfos(ctx.getPresentedFiles());
                    runDO.setElapsedMs(ctx.elapsedMs());
                }
                workflowRunMapper.updateById(runDO);
            }
            if (ctx != null) {
                if (stopped) {
                    ctx.sendEvent("run_stopped", Map.of("runId", runDO == null ? null : runDO.getId()));
                } else {
                    ctx.sendEvent("run_error", Map.of(
                            "runId", runDO == null ? null : runDO.getId(),
                            "error", truncateError(e.getMessage())));
                }
            }
        } finally {
            if (runDO != null) {
                runningContexts.remove(runDO.getId());
            }
            try {
                emitter.complete();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 解析画布并执行（模板方法骨架）
     */
    private GraphResult executeGraph(WorkflowRunContext ctx) {
        WorkflowGraph graph = WorkflowGraph.parse(ctx.getWorkflow().getGraph());
        List<WorkflowGraphNode> startNodes = graph.getStartNodes();
        if (startNodes.isEmpty()) {
            throw new IllegalArgumentException("画布数据为空或格式不正确，无法运行");
        }

        Map<String, Object> memory = new LinkedHashMap<>(ctx.getInputs());
        Deque<WorkflowGraphNode> queue = new ArrayDeque<>(startNodes);
        Set<String> executed = new HashSet<>();
        Map<String, Object> lastOutput = Map.of();
        Map<String, Object> startOutputs = null;
        Map<String, Object> endOutputs = null;
        NodeExecutionPipeline pipeline = handlerFactory.getPipeline();

        while (!queue.isEmpty()) {
            WorkflowGraphNode node = queue.poll();
            if (ctx.getStopped().get()) {
                break;
            }
            if (!executed.add(node.getId())) {
                continue;
            }

            // 获取处理器并判断是否为支持"资源挂载"的 no-op 节点（tool/skill/model 的可视化挂载点）。
            // 资源节点的日志不应作为独立节点返回，而是由 Agent 节点在真实调用工具/技能时，以内部步骤形式实时推送。
            WorkflowNodeHandlerAdapter handler = handlerFactory.getHandler(node.getType());
            boolean isResourceNoop = handler instanceof ResourceNodeHandler;

            if (!isResourceNoop) {
                ctx.sendEvent("node_started", Map.of(
                        "runId", ctx.getRunId(),
                        "nodeId", node.getId(),
                        "nodeName", node.getName(),
                        "nodeType", node.getType()));
            }

            // 判断节点：提前解析命中的分支(sourceHandle)，随 node_finished 上报，供前端只点亮该分支的连线
            boolean isIfElse = IfElseNodeHandler.isIfElse(node.getType());
            boolean ifElseHasConditions = isIfElse && IfElseNodeHandler.hasConditions(node);
            String routedBranch = ifElseHasConditions ? ifElseHandler.resolveBranch(node, memory) : null;

            try {
                // 提示词注入：把下游（知识库/判断）节点通过 buildPredecessorSystemPrompt 提供的
                // 「提示词模板」收集给当前大模型，用户的系统提示词置于模板中间
                String injectedTemplate = collectSuccessorPrompt(graph, node);
                if (injectedTemplate != null && node.getData() != null) {
                    node.getData().put("injectedPromptTemplate", injectedTemplate);
                }

                // 工厂模式获取处理器，责任链模式执行
                NodeExecutionPipeline.NodeExecutionContext execCtx =
                        NodeExecutionPipeline.NodeExecutionContext.of(node, ctx, handler);

                Map<String, Object> outputs = pipeline.execute(execCtx, memory);

                if (outputs != null) {
                    outputs.forEach((key, value) -> memory.put(node.getId() + "." + key, value));
                    lastOutput = outputs;
                }

                // 记录开始节点输出（运行入参）与结束节点输出（最终出参）
                if (handler instanceof StartNodeHandler && startOutputs == null) {
                    startOutputs = outputs;
                }
                if (handler instanceof EndNodeHandler) {
                    endOutputs = outputs;
                }

                if (!isResourceNoop) {
                    Map<String, Object> finishedEvent = new LinkedHashMap<>();
                    finishedEvent.put("runId", ctx.getRunId());
                    finishedEvent.put("nodeId", node.getId());
                    finishedEvent.put("nodeName", node.getName());
                    finishedEvent.put("nodeType", node.getType());
                    finishedEvent.put("output", outputs == null ? Map.of() : outputs);
                    if (routedBranch != null) {
                        finishedEvent.put("branch", routedBranch);
                    }
                    ctx.sendEvent("node_finished", finishedEvent);
                }
            } catch (Exception e) {
                throw e;
            }

            for (WorkflowGraphEdge edge : node.getOutEdges()) {
                if (ifElseHasConditions) {
                    // 判断节点：按前面已解析的命中分支(sourceHandle)路由，只进入对应出边
                    if (routedBranch == null) {
                        // 未命中任何分支：当前路径终止，不进入任何出边
                        break;
                    }
                    if (!routedBranch.equals(edge.getSourceHandle())) {
                        continue;
                    }
                } else {
                    // 普通节点：沿用线条件（edge.data.condition）过滤出边
                    String condition = edge.getCondition();
                    if (condition != null && !condition.isBlank()
                            && !WorkflowVariableResolver.evaluateCondition(condition, memory)) {
                        continue;
                    }
                }
                WorkflowGraphNode target = graph.getNode(edge.getTarget());
                if (target != null && !executed.contains(target.getId())) {
                    queue.add(target);
                }
            }
        }

        // 输出：优先结束节点整理的结果；若结束节点未配置或未命中，退化为最后一个节点输出 / 累计内存
        Map<String, Object> finalOutputs = (endOutputs != null && !endOutputs.isEmpty())
                ? endOutputs
                : (lastOutput.isEmpty() ? memory : lastOutput);
        return new GraphResult(startOutputs, endOutputs, finalOutputs);
    }

    /**
     * 收集当前节点的所有下游邻居节点，用于传给节点处理器的 {@code buildPredecessorSystemPrompt} 提示词注入。
     */
    private List<WorkflowGraphNode> successorsOf(WorkflowGraph graph, WorkflowGraphNode node) {
        if (graph == null || node == null || node.getOutEdges() == null) {
            return List.of();
        }
        return node.getOutEdges().stream()
                .map(edge -> graph.getNode(edge.getTarget()))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    /**
     * 汇总下游节点（知识库/判断等）提供的「提示词模板」，组装为待注入的模板，供大模型执行时
     * 把用户系统提示词置于中间（前缀 + %s + 后缀）。无任何下游注入时返回 {@code null}。
     */
    private String collectSuccessorPrompt(WorkflowGraph graph, WorkflowGraphNode node) {
        StringBuilder prefix = new StringBuilder();
        StringBuilder suffix = new StringBuilder();
        boolean any = false;
        for (WorkflowGraphNode successor : successorsOf(graph, node)) {
            final WorkflowNodeHandlerAdapter successorHandler;
            try {
                successorHandler = handlerFactory.getHandler(successor.getType());
            } catch (Exception e) {
                continue;
            }
            if (successorHandler == null) {
                continue;
            }
            String template;
            try {
                template = successorHandler.buildPredecessorSystemPrompt(successor, node);
            } catch (Exception e) {
                template = null;
            }
            if (template == null || template.isBlank()) {
                continue;
            }
            int idx = template.indexOf("%s");
            if (idx < 0) {
                prefix.append(template).append("\n");
            } else {
                prefix.append(template, 0, idx);
                suffix.append(template, idx + 2, template.length());
                any = true;
            }
        }
        if (!any) {
            return null;
        }
        return prefix + "%s" + suffix;
    }

    /**
     * 一次图执行的汇总结果：开始节点输出（入参）、结束节点输出、最终出参
     */
    private record GraphResult(Map<String, Object> startOutputs,
                               Map<String, Object> endOutputs,
                               Map<String, Object> finalOutputs) {
    }

    private String truncateError(String error) {
        if (error == null) {
            return "未知错误";
        }
        return error.length() > 500 ? error.substring(0, 500) : error;
    }
}