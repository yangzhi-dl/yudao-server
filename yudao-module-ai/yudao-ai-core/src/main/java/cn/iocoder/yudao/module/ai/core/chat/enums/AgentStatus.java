package cn.iocoder.yudao.module.ai.core.chat.enums;

public enum AgentStatus {
    RUNNING("running", "运行中"),
    IDLE("idle", "空闲"),
    MAINTENANCE("maintenance", "维护中"),
    ERROR("error", "异常");

    public final String code;
    public final String desc;

    AgentStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
