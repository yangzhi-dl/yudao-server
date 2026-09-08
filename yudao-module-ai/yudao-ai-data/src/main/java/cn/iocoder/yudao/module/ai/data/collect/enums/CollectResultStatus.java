package cn.iocoder.yudao.module.ai.data.collect.enums;

import lombok.Getter;

/**
 * 采集结果状态。
 */
@Getter
public enum CollectResultStatus {

    SUCCESS(0, "成功"),
    FAIL(1, "失败"),
    SKIP(2, "跳过");

    private final int value;
    private final String name;

    CollectResultStatus(int value, String name) {
        this.value = value;
        this.name = name;
    }

}
