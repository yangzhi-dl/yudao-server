package cn.iocoder.yudao.module.ai.core.workflow.engine.state;

/**
 * 状态模式：状态管理器
 * <p>
 * 封装工作流状态转换逻辑，保证状态转换的合法性。
 *
 * @author yudao
 */
public class WorkflowStateManager {

    private WorkflowState currentState;

    public WorkflowStateManager(Integer statusCode) {
        this.currentState = fromCode(statusCode);
    }

    public WorkflowState getCurrentState() {
        return currentState;
    }

    public Integer getStatusCode() {
        return currentState.code();
    }

    /**
     * 发布
     */
    public WorkflowStateManager publish() {
        currentState = currentState.publish();
        return this;
    }

    /**
     * 下架
     */
    public WorkflowStateManager offline() {
        currentState = currentState.offline();
        return this;
    }

    /**
     * 重新编辑
     */
    public WorkflowStateManager reedit() {
        currentState = currentState.reedit();
        return this;
    }

    /**
     * 是否允许编辑画布
     */
    public boolean canEdit() {
        return currentState.canEdit();
    }

    /**
     * 是否允许运行
     */
    public boolean canRun() {
        return currentState.canRun();
    }

    private static WorkflowState fromCode(Integer code) {
        if (code == null) {
            return new DraftState();
        }
        return switch (code) {
            case 0 -> new DraftState();
            case 1 -> new PublishedState();
            case 2 -> new OfflineState();
            default -> new DraftState();
        };
    }
}