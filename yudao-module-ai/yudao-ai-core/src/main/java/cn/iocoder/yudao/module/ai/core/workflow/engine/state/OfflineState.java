package cn.iocoder.yudao.module.ai.core.workflow.engine.state;

/**
 * 状态模式：已下架状态
 *
 * @author yudao
 */
public class OfflineState implements WorkflowState {

    @Override
    public Integer code() {
        return 2;
    }

    @Override
    public String name() {
        return "已下架";
    }

    @Override
    public WorkflowState reedit() {
        return new DraftState();
    }

    @Override
    public boolean canEdit() {
        return false;
    }

    @Override
    public boolean canRun() {
        return false;
    }
}