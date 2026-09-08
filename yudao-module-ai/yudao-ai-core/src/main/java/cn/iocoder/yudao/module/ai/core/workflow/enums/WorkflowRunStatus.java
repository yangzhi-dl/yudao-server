package cn.iocoder.yudao.module.ai.core.workflow.enums;

/**
 * 工作流运行状态枚举
 */
public enum WorkflowRunStatus {

    /** 运行中 */
    RUNNING(0, "运行中"),
    /** 成功 */
    SUCCESS(1, "成功"),
    /** 失败 */
    FAILED(2, "失败"),
    /** 已停止 */
    STOPPED(3, "已停止");

    public final Integer status;
    public final String name;

    WorkflowRunStatus(Integer status, String name) {
        this.status = status;
        this.name = name;
    }

    public static WorkflowRunStatus of(Integer status) {
        if (status == null) {
            return RUNNING;
        }
        for (WorkflowRunStatus value : values()) {
            if (value.status.equals(status)) {
                return value;
            }
        }
        return RUNNING;
    }
}
