package cn.iocoder.yudao.module.ai.core.workflow.engine.state;

/**
 * 状态模式：草稿状态
 *
 * @author yudao
 */
public class DraftState implements WorkflowState {

    @Override
    public Integer code() {
        return 0;
    }

    @Override
    public String name() {
        return "草稿";
    }

    @Override
    public WorkflowState publish() {
        return new PublishedState();
    }

    @Override
    public boolean canEdit() {
        return true;
    }
}