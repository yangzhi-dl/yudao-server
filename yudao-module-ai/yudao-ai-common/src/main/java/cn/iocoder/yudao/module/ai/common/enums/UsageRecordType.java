package cn.iocoder.yudao.module.ai.common.enums;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * AI 使用记录类型枚举
 */
@RequiredArgsConstructor
@Getter
public enum UsageRecordType implements ArrayValuable<String> {

    WIKI(0, "WIKI", "知识库"),
    DOCUMENT(1, "DOCUMENT", "文档"),
    WORKFLOW(2, "WORKFLOW", "工作流"),
    AGENT(3, "AGENT", "智能体");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(UsageRecordType::getType).toArray(String[]::new);

    @EnumValue
    private final int code;
    private final String type;
    private final String name;

    public static String getNameByType(String type) {
        UsageRecordType e = CollUtil.findOne(CollUtil.newArrayList(values()),
                item -> ObjUtil.equal(item.type, type));
        return e == null ? null : e.getName();
    }

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
