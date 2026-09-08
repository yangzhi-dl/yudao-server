package cn.iocoder.yudao.module.ai.core.chat.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;

/**
 * AI 聊天对话 DO
 *
 * @author yudao
 */
@TableName(value = "ai_chat_dialogue", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatDialogueDO extends BaseDO {

    /**
     * 对话编号
     */
    @TableId
    private Long id;

    /**
     * 上一条对话编号
     */
    private Long lastId;

    /**
     * 主题编号
     */
    private Long topicId;

    /**
     * 发送者角色
     */
    private String sender;

    /**
     * 元数据
     */
    private String metadata;

    /**
     * 工具调用记录
     */
    private String tools;

    /**
     * 文件 ID 列表（JSON）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> fileIds;

    /**
     * 是否删除
     */
    private Boolean deleted;

    /**
     * 反馈状态
     */
    private Integer feedbackStatus;

    /**
     * 是否收藏
     */
    private Boolean isStar;

}
