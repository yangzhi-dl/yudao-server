package cn.iocoder.yudao.module.ai.core.workflow.engine.state;

/**
 * 状态模式：已发布状态
 *
 * @author yudao
 */
public class PublishedState implements WorkflowState {

    @Override
    public Integer code() {
        return 1;
    }

    @Override
    public String name() {
        return "已发布";
    }

    @Override
    public WorkflowState offline() {
        return new OfflineState();
    }
}