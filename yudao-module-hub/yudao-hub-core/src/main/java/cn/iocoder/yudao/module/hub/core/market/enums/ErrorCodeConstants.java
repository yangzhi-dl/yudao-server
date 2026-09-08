package cn.iocoder.yudao.module.hub.core.market.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Hub 智能体市场错误码。
 *
 * Hub 智能体市场使用 1-070-001-000 段。
 */
public interface ErrorCodeConstants {

    ErrorCode MARKET_AGENT_NOT_EXISTS = new ErrorCode(1_070_001_000, "市场智能体不存在或已下架");
    ErrorCode MARKET_AGENT_TRIAL_DISABLED = new ErrorCode(1_070_001_001, "该市场智能体暂未开放试用");
    ErrorCode MARKET_AGENT_SOURCE_INVALID = new ErrorCode(1_070_001_002, "市场智能体来源已失效，请联系管理员");
    ErrorCode MARKET_EXPERIENCE_TOPIC_INVALID = new ErrorCode(1_070_001_003, "体验会话不存在或无权访问");
    ErrorCode MARKET_EXPERIENCE_MESSAGE_INVALID = new ErrorCode(1_070_001_004, "体验消息上下文无效");
    ErrorCode MARKET_ADVISOR_NOT_CONFIGURED = new ErrorCode(1_070_001_005, "市场选型助手尚未配置");
    ErrorCode MARKET_ADVISOR_SOURCE_INVALID = new ErrorCode(1_070_001_006, "市场选型助手配置无效，请联系管理员");
    ErrorCode MARKET_ADVISOR_TOPIC_INVALID = new ErrorCode(1_070_001_007, "选型助手会话不存在或无权访问");
    ErrorCode MARKET_ADVISOR_MESSAGE_INVALID = new ErrorCode(1_070_001_008, "选型助手消息上下文无效");

}
