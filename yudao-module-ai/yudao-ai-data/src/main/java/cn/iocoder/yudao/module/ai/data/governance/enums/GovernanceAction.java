package cn.iocoder.yudao.module.ai.data.governance.enums;

import lombok.Getter;

/**
 * 数据治理动作类型。
 */
@Getter
public enum GovernanceAction {

    CREATE(0, "创建"),
    EDIT(1, "人工编辑"),
    AI_CLEAN(2, "AI 清洗"),
    DISCARD(3, "废弃"),
    ARCHIVE(4, "归档");

    private final int value;
    private final String name;

    GovernanceAction(int value, String name) {
        this.value = value;
        this.name = name;
    }

}
