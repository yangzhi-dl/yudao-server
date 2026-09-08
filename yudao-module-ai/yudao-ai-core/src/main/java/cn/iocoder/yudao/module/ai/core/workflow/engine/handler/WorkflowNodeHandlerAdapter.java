package cn.iocoder.yudao.module.ai.core.workflow.engine.handler;

import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowGraphNode;
import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowRunContext;

import java.util.Map;

/**
 * 适配器接口：将原有的 WorkflowNodeHandler 适配到责任链管道
 *
 * @author yudao
 */
public interface WorkflowNodeHandlerAdapter {

    /**
     * 支持的节点类型
     */
    String[] types();

    /**
     * 执行节点
     */
    Map<String, Object> execute(WorkflowGraphNode node, WorkflowRunContext ctx, Map<String, Object> memory);

    /**
     * 节点提示词注入（可选实现，默认不注入）。
     * <p>
     * 当该节点作为某大模型节点的下游（承接点）时，由执行器在运行该大模型前收集本方法返回的
     * 「提示词模板」。模板中可用 {@code %s} 作为用户系统提示词的占位符，
     * 最终提示词为：模板前缀 + 用户系统提示词 + 模板后缀（如 {@code xxxx %s xxx}）。
     * 返回 {@code null} 表示无需注入。
     *
     * @param self        当前节点（读取自身配置生成提示词，如知识库信息/判断分支清单）
     * @param predecessor 前驱大模型节点（读取其 outputDefs 字段名与描述，动态生成 JSON 字段说明）
     */
    default String buildPredecessorSystemPrompt(WorkflowGraphNode self, WorkflowGraphNode predecessor) {
        return null;
    }
}