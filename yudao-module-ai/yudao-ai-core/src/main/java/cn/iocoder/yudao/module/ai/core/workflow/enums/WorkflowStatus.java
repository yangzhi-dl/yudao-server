package cn.iocoder.yudao.module.ai.core.workflow.enums;

/**
 * 工作流状态枚举
 */
public enum WorkflowStatus {

    /** 草稿 */
    DRAFT(0, "草稿"),
    /** 已发布 */
    PUBLISHED(1, "已发布"),
    /** 已下架 */
    OFFLINE(2, "已下架");

    public final Integer status;
    public final String name;

    WorkflowStatus(Integer status, String name) {
        this.status = status;
        this.name = name;
    }

    public static WorkflowStatus of(Integer status) {
        if (status == null) {
            return DRAFT;
        }
        for (WorkflowStatus value : values()) {
            if (value.status.equals(status)) {
                return value;
            }
        }
        return DRAFT;
    }
}
