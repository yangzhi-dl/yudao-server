package cn.iocoder.yudao.module.ai.data.collect.enums;

import lombok.Getter;

/**
 * 采集结果转换状态。
 */
@Getter
public enum ConvertStatus {

    PENDING(0, "待转换"),
    CONVERTING(1, "转换中"),
    CONVERTED(2, "已转换"),
    NO_NEED(3, "无需转换"),
    FAILED(4, "转换失败");

    private final int value;
    private final String name;

    ConvertStatus(int value, String name) {
        this.value = value;
        this.name = name;
    }

}
