package cn.iocoder.yudao.module.ai.core.chat.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.core.chat.enums.AiApiType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI 对话 Token DO
 *
 * @author yudao
 */
@TableName("ai_chat_token")
@KeySequence("ai_chat_token_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatTokenDO extends BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private Long totalTokens;

    private Long promptTokens;

    private Long completionTokens;

    private String model;

    @TableField("`type`")
    private AiApiType type;

}
