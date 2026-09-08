package cn.iocoder.yudao.module.ai.common.constans;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * AI 错误码枚举类
 * <p>
 * ai 系统，使用 1-030-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 任务历史 1-030-001-000 ==========
    ErrorCode TASK_HISTORY_NOT_EXISTS = new ErrorCode(1_030_001_000, "任务历史不存在");
    ErrorCode TASK_HISTORY_STATUS_INVALID = new ErrorCode(1_030_001_001, "任务状态无效");

    // ========== 使用记录 1-030-002-000 ==========
    ErrorCode USAGE_RECORD_NOT_EXISTS = new ErrorCode(1_030_002_000, "使用记录不存在");
    ErrorCode USAGE_RECORD_TYPE_INVALID = new ErrorCode(1_030_002_001, "使用记录类型无效");

    // ========== 文档转换 1-030-003-000 ==========
    ErrorCode DOCUMENT_CONVERSION_FAIL = new ErrorCode(1_030_003_000, "文档转换失败");
    ErrorCode MARKDOWN_CONVERSION_FAIL = new ErrorCode(1_030_003_001, "Markdown 转换失败");
    ErrorCode PDF_TO_IMAGE_FAIL = new ErrorCode(1_030_003_002, "PDF 转图片失败");

    // ========== 权限 1-030-004-000 ==========
    ErrorCode NO_READ_PERMISSION = new ErrorCode(1_030_004_000, "当前无访问权限，如需开通，请联系系统管理员协助处理！");
    ErrorCode NO_DELETE_PERMISSION = new ErrorCode(1_030_004_001, "当前无删除权限，如需开通，请联系系统管理员协助处理！");
    ErrorCode NO_WRITE_PERMISSION = new ErrorCode(1_030_004_002, "当前无写入权限，如需开通，请联系系统管理员协助处理！");
    ErrorCode NO_MANAGE_PERMISSION = new ErrorCode(1_030_004_003, "当前无管理权限，如需开通，请联系系统管理员协助处理！");
    ErrorCode NO_EXECUTE_PERMISSION = new ErrorCode(1_030_004_004, "当前无执行权限，如需开通，请联系系统管理员协助处理！");

    // ========== 知识库 1-030-005-000 ==========
    ErrorCode DOCUMENT_NOT_FOUND = new ErrorCode(1_030_005_000, "该文档不存在！");
    ErrorCode WIKI_NOT_FOUND = new ErrorCode(1_030_005_001, "该知识库不存在！");
    ErrorCode WIKI_GRAPH_NOT_FOUND = new ErrorCode(1_030_005_002, "删除知识图谱失败，请检查服务！");

    // ========== Chat 1-030-006-000 ==========
    ErrorCode UNSUPPORTED_API_TYPE = new ErrorCode(1_030_006_000, "不支持的 API 类型！");

    // ========== 重新生成 1-030-008-000 ==========
    ErrorCode REGENERATE_VERSION_NOT_EXISTS = new ErrorCode(1_030_008_000, "回答版本不存在或无权操作");
    ErrorCode REGENERATE_DIALOGUE_NOT_EXISTS = new ErrorCode(1_030_008_001, "要重新生成的消息不存在或无权操作");

    // ========== 用户设置 1-030-007-000 ==========
    ErrorCode USER_SETTING_USER_ID_NOT_NULL = new ErrorCode(1_030_007_000, "用户编号不能为空");
    ErrorCode MODEL_NOT_EXISTS = new ErrorCode(1_030_007_001, "模型不存在");

    // ========== 工作流 1-030-009-000 ==========
    ErrorCode WORKFLOW_NOT_EXISTS = new ErrorCode(1_030_009_000, "工作流不存在");
    ErrorCode WORKFLOW_NAME_EXISTS = new ErrorCode(1_030_009_001, "工作流名称已存在");
    ErrorCode WORKFLOW_GRAPH_INVALID = new ErrorCode(1_030_009_002, "工作流画布无效：{0}");
    ErrorCode WORKFLOW_RUN_NOT_EXISTS = new ErrorCode(1_030_009_003, "工作流运行记录不存在");
    ErrorCode WORKFLOW_NODE_TYPE_UNSUPPORTED = new ErrorCode(1_030_009_004, "不支持的节点类型：{0}");

}
