package cn.iocoder.yudao.module.ai.knowledge.wiki.model.entity;

import cn.iocoder.yudao.module.ai.core.chat.enums.ChunkMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WikiSettings {

    /**
     * 是否自动向量化
     */
    private Boolean autoVectorize;

    /**
     * 切块方式
     */
    private ChunkMethod chunkMethod;

    /**
     * 切块大小（字符数）
     */
    private Integer chunkSize;

    /**
     * 重叠大小（字符数）
     */
    private Integer overlapSize;

}