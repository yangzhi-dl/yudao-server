package cn.iocoder.yudao.module.ai.common.enums;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * AI 任务历史状态枚举
 */
@RequiredArgsConstructor
@Getter
public enum TaskHistoryStatus implements ArrayValuable<String> {

    /** 待执行 **/
    PENDING(0, "PENDING", "待执行"),

    /** 执行中 **/
    RUNNING(1, "RUNNING", "执行中"),

    /** 成功 **/
    SUCCESS(2, "SUCCESS", "成功"),

    /** 部分失败 **/
    PARTIAL_FAILURE(3, "PARTIAL_FAILURE", "部分失败"),

    /** 失败 **/
    FAILURE(4, "FAILURE", "失败"),

    /** 取消 **/
    CANCELLED(5, "CANCELLED", "已取消");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(TaskHistoryStatus::getStatus).toArray(String[]::new);

    @EnumValue
    private final int code;
    private final String status;
    private final String name;

    public static String getNameByStatus(String status) {
        TaskHistoryStatus e = CollUtil.findOne(CollUtil.newArrayList(values()),
                item -> ObjUtil.equal(item.status, status));
        return e == null ? null : e.getName();
    }

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
