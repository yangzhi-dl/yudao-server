package cn.iocoder.yudao.module.ai.core.chat.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI 对话内容 DO
 *
 * @author yudao
 */
@TableName("ai_chat_dialogue_content")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatDialogueContentDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 对话ID（主键）
     */
    @TableId
    private Long dialogueId;

    /**
     * 对话内容
     */
    private String content;

}
