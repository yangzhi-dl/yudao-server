package cn.iocoder.yudao.module.ai.core.workflow.engine.command;

/**
 * 命令模式（Command Pattern）：工作流画布操作命令接口
 * <p>
 * 将画布编辑操作（添加/删除/移动节点、连线等）封装为可撤销的命令对象。
 *
 * @author yudao
 */
public interface WorkflowCommand {

    /**
     * 执行命令
     */
    void execute();

    /**
     * 撤销命令
     */
    void undo();

    /**
     * 命令描述
     */
    String description();
}