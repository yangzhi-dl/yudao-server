package cn.iocoder.yudao.module.ai.data.constans;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * 数据采集与治理错误码。
 * <p>
 * 复用 AI 系统 1-030-xxx 段，数据治理使用 1-030-007-xxx。
 */
public interface DataErrorCodeConstants {

    // ========== 数据源 1-030-007-000 ==========
    ErrorCode DATA_SOURCE_NOT_FOUND = new ErrorCode(1_030_007_000, "数据源不存在");
    ErrorCode DATA_SOURCE_TYPE_INVALID = new ErrorCode(1_030_007_001, "数据源类型无效");
    ErrorCode DATA_SOURCE_DISABLED = new ErrorCode(1_030_007_002, "数据源已禁用");

    // ========== 采集 1-030-007-010 ==========
    ErrorCode COLLECT_TASK_NOT_FOUND = new ErrorCode(1_030_007_010, "采集任务不存在");
    ErrorCode COLLECT_RESULT_NOT_FOUND = new ErrorCode(1_030_007_011, "采集结果不存在");
    ErrorCode COLLECT_CONVERT_FAILED = new ErrorCode(1_030_007_012, "数据转换失败");

    // ========== 资产治理 1-030-007-020 ==========
    ErrorCode ASSET_NOT_FOUND = new ErrorCode(1_030_007_020, "数据资产不存在");
    ErrorCode ASSET_STATUS_INVALID = new ErrorCode(1_030_007_021, "数据资产状态无效");
    ErrorCode ASSET_QUALITY_BELOW_THRESHOLD = new ErrorCode(1_030_007_022, "资产质量评分低于归档门槛，请先编辑或清洗");
    ErrorCode ASSET_ALREADY_ARCHIVED = new ErrorCode(1_030_007_023, "数据资产已归档");
    ErrorCode ASSET_ALREADY_DISCARDED = new ErrorCode(1_030_007_024, "数据资产已废弃");

}
