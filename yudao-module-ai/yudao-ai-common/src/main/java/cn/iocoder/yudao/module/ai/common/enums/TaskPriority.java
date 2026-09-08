package cn.iocoder.yudao.module.ai.common.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 任务优先级枚举
 */
@Getter
public enum TaskPriority implements ArrayValuable<Integer> {

    URGENT(1, "紧急"),
    HIGH(3, "高"),
    NORMAL(5, "普通"),
    LOW(8, "低"),
    BACKGROUND(10, "后台");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(TaskPriority::getValue).toArray(Integer[]::new);

    private final Integer value;
    private final String name;

    TaskPriority(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
