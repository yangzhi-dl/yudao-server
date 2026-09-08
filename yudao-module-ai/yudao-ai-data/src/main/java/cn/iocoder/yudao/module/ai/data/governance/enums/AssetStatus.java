package cn.iocoder.yudao.module.ai.data.governance.enums;

import lombok.Getter;

/**
 * 数据资产状态机。
 * <p>
 * 状态流转：
 * <pre>
 * PENDING_CONVERSION（待转换）
 *   ├─> PENDING_GOVERNANCE（待治理，转换成功）
 *   └─> CONVERT_FAILED（转换失败）
 * PENDING_GOVERNANCE
 *   ├─> GOVERNED（已治理，人工编辑或 AI 清洗）
 *   └─> DISCARDED（已废弃）
 * GOVERNED ─> ARCHIVED（已归档）
 * </pre>
 */
@Getter
public enum AssetStatus {

    PENDING_CONVERSION(0, "待转换"),
    PENDING_GOVERNANCE(1, "待治理"),
    GOVERNED(2, "已治理"),
    ARCHIVED(3, "已归档"),
    DISCARDED(4, "已废弃"),
    CONVERT_FAILED(5, "转换失败");

    private final int value;
    private final String name;

    AssetStatus(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static AssetStatus fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (AssetStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        return null;
    }

}
