package cn.iocoder.yudao.module.ai.common.enums;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * AI 任务历史类型枚举
 */
@RequiredArgsConstructor
@Getter
public enum TaskHistoryType implements ArrayValuable<String> {

    /** 文档文件 **/
    DOCUMENT_FILE(0, "DOCUMENT_FILE", "文档文件"),
    /** 爬虫内容 **/
    CRAWL_CONTEXT(1, "CRAWL_CONTEXT", "爬虫内容"),
    /** 历史对话内容导出 **/
    CONVERSATION_EXPORT(2, "CONVERSATION_EXPORT", "历史对话导出"),
    /** 每日统计任务 **/
    DAILY_STATISTICS(3, "DAILY_STATISTICS", "每日统计"),
    /** 每小时统计任务 **/
    HOURLY_STATISTICS(4, "HOURLY_STATISTICS", "每小时统计"),
    /** 每分钟统计任务 **/
    MINUTELY_STATISTICS(5, "MINUTELY_STATISTICS", "每分钟统计"),
    /** 每秒统计任务 **/
    SECONDLY_STATISTICS(6, "SECONDLY_STATISTICS", "每秒统计"),
    /** 每月统计任务 **/
    MONTHLY_STATISTICS(7, "MONTHLY_STATISTICS", "每月统计"),
    /** 未知任务 **/
    DEFAULT_TASK(8, "DEFAULT_TASK", "未知任务");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(TaskHistoryType::getType).toArray(String[]::new);

    @EnumValue
    private final int code;
    private final String type;
    private final String name;

    public static String getNameByType(String type) {
        TaskHistoryType e = CollUtil.findOne(CollUtil.newArrayList(values()),
                item -> ObjUtil.equal(item.type, type));
        return e == null ? null : e.getName();
    }

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
