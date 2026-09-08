package cn.iocoder.yudao.module.ai.core.chat.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * AI 聊天主题 DO
 *
 * @author yudao
 */
@TableName("ai_chat_topic")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatTopicDO extends BaseDO {

    /**
     * 主题编号
     */
    @TableId
    private Long id;

    /**
     * 置顶位
     */
    @TableField("`top`")
    private Integer top;

    /**
     * 标题
     */
    private String title;

    /**
     * 是否删除
     */
    private Boolean deleted;

    /**
     * 是否归档
     */
    private Boolean isArchived;

}
