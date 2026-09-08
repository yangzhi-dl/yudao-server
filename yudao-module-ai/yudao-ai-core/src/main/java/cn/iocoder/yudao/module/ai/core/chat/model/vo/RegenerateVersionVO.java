package cn.iocoder.yudao.module.ai.core.chat.model.vo;

import cn.iocoder.yudao.module.ai.core.chat.model.entity.AiAgentMessage;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.AiContent;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ChatTool;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 回答重新生成版本 VO
 * <p>
 * 列表第 1 个元素为当前生效版本（isCurrent = true，内容来自 ai_chat_dialogue 主行），
 * 其余为历史版本（内容来自 ai_chat_regenerate 快照表）。
 *
 * @author yudao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegenerateVersionVO implements Serializable {

    /**
     * 版本 ID：当前版 = 主行 dialogue id；历史版 = ai_chat_regenerate 行 id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long id;

    /**
     * 归属的 assistant 对话 ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long dialogueId;

    /**
     * 是否当前生效版本
     */
    private Boolean isCurrent;

    /**
     * 回答内容（普通聊天：aiContent；智能体：aiAgentMessage）
     */
    private List<AiContent> aiContent;

    /**
     * 智能体回答内容
     */
    private AiAgentMessage aiAgentMessage;

    /**
     * 知识库引用元数据
     */
    private List<DocMetadataVO> docMetadata;

    /**
     * 工具调用记录
     */
    private List<ChatTool> tools;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
