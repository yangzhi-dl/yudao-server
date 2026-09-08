package cn.iocoder.yudao.module.ai.core.chat.enums;

/**
 * 切块方式枚举
 */
public enum ChunkMethod {
    /**
     * 固定大小切块
     */
    FIXED_SIZE,
    /**
     * 按句子切块
     */
    SENTENCE,
    /**
     * 按段落切块
     */
    PARAGRAPH,
    /**
     * 语义切块
     */
    SEMANTIC,
    /**
     * 按标题切块（按Markdown/HTML标题层级进行切块）
     */
    HEADING
}