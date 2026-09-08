package cn.iocoder.yudao.module.ai.common.enums;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * AI 任务调度类型枚举
 */
@RequiredArgsConstructor
@Getter
public enum TaskScheduleType implements ArrayValuable<String> {

    /** 立即执行 **/
    IMMEDIATELY(1, "IMMEDIATELY", "立即执行"),

    /** 定时执行 **/
    TIMED(2, "TIMED", "定时执行");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(TaskScheduleType::getType).toArray(String[]::new);

    @EnumValue
    private final int code;
    private final String type;
    private final String name;

    public static String getNameByType(String type) {
        TaskScheduleType e = CollUtil.findOne(CollUtil.newArrayList(values()),
                item -> ObjUtil.equal(item.type, type));
        return e == null ? null : e.getName();
    }

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
