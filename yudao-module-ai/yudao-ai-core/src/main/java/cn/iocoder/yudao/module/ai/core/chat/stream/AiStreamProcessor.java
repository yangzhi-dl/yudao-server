package cn.iocoder.yudao.module.ai.core.chat.stream;

import cn.iocoder.yudao.module.ai.core.chat.enums.AiApiType;

/**
 * AI 流式处理策略接口 —— 策略模式
 * <p>
 * 根据不同的 {@link AiApiType} 选择对应的流式处理策略，消除 if-else 分支。
 *
 */
public interface AiStreamProcessor {

    /**
     * 是否支持该 API 类型
     */
    boolean supports(AiApiType apiType);

    /**
     * 处理流式对话
     */
    void processStream(StreamContext context);
}
