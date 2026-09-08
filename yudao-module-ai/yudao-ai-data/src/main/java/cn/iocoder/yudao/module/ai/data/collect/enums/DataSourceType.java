package cn.iocoder.yudao.module.ai.data.collect.enums;

import lombok.Getter;

/**
 * 采集源类型。
 */
@Getter
public enum DataSourceType {

    FILESYSTEM(1, "文件系统"),
    HTTP(2, "HTTP 下载"),
    CRAWLER(3, "网页提取器");

    private final int value;
    private final String name;

    DataSourceType(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static DataSourceType fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (DataSourceType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }

}
