package cn.iocoder.yudao.module.ai.data.collect.enums;

import lombok.Getter;

/**
 * 采集任务状态。
 */
@Getter
public enum CollectTaskStatus {

    RUNNING(0, "进行中"),
    SUCCESS(1, "成功"),
    FAIL(2, "失败"),
    PARTIAL(3, "部分成功");

    private final int value;
    private final String name;

    CollectTaskStatus(int value, String name) {
        this.value = value;
        this.name = name;
    }

}
