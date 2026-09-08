package cn.iocoder.yudao.module.ai.core.workflow.engine.command;

import java.util.ArrayList;
import java.util.List;

/**
 * 命令模式：命令历史管理器
 * <p>
 * 维护 undo/redo 栈，支持多级撤销和重做。
 *
 * @author yudao
 */
public class WorkflowCommandHistory {

    private final List<WorkflowCommand> undoStack = new ArrayList<>();
    private final List<WorkflowCommand> redoStack = new ArrayList<>();
    private final int maxHistory;

    public WorkflowCommandHistory(int maxHistory) {
        this.maxHistory = maxHistory;
    }

    public WorkflowCommandHistory() {
        this(50);
    }

    /**
     * 执行命令并记录到历史
     */
    public void execute(WorkflowCommand command) {
        command.execute();
        pushUndo(command);
        redoStack.clear();
    }

    /**
     * 撤销
     */
    public boolean undo() {
        if (undoStack.isEmpty()) {
            return false;
        }
        WorkflowCommand command = undoStack.removeLast();
        command.undo();
        redoStack.add(command);
        return true;
    }

    /**
     * 重做
     */
    public boolean redo() {
        if (redoStack.isEmpty()) {
            return false;
        }
        WorkflowCommand command = redoStack.removeLast();
        command.execute();
        undoStack.add(command);
        return true;
    }

    /**
     * 是否可撤销
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * 是否可重做
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * 清空历史
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    private void pushUndo(WorkflowCommand command) {
        undoStack.add(command);
        while (undoStack.size() > maxHistory) {
            undoStack.removeFirst();
        }
    }
}