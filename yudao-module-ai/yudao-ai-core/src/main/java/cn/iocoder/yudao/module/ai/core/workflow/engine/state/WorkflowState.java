package cn.iocoder.yudao.module.ai.core.workflow.engine.state;

/**
 * 状态模式（State Pattern）：工作流生命周期状态接口
 * <p>
 * 每个状态封装了该状态下允许的操作，状态转换由具体状态类处理。
 *
 * @author yudao
 */
public interface WorkflowState {

    /**
     * 状态值
     */
    Integer code();

    /**
     * 状态名称
     */
    String name();

    /**
     * 发布：草稿 → 已发布
     */
    default WorkflowState publish() {
        throw new IllegalStateException("当前状态 [" + name() + "] 不允许发布");
    }

    /**
     * 下架：已发布 → 已下架
     */
    default WorkflowState offline() {
        throw new IllegalStateException("当前状态 [" + name() + "] 不允许下架");
    }

    /**
     * 重新编辑：已下架 → 草稿
     */
    default WorkflowState reedit() {
        throw new IllegalStateException("当前状态 [" + name() + "] 不允许重新编辑");
    }

    /**
     * 是否允许编辑画布
     */
    default boolean canEdit() {
        return false;
    }

    /**
     * 是否允许运行
     */
    default boolean canRun() {
        return true;
    }
}