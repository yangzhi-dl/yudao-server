package cn.iocoder.yudao.module.ai.core.chat.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;

/**
 * AI 回答重新生成版本 DO
 * <p>
 * 当前生效的回答始终存于 {@link AiChatDialogueDO} 主行（原地更新，id 不变）；
 * 被重新生成替换掉的旧回答快照到本表，用于版本列表展示与版本切换。
 *
 * @author yudao
 */
@TableName(value = "ai_chat_regenerate", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRegenerateDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 归属的 assistant 对话 ID（锚点，重生成时不变）
     */
    private Long dialogueId;

    /**
     * 话题 ID
     */
    private Long topicId;

    /**
     * 用户消息 ID（重生成的问题锚点）
     */
    private Long lastId;

    /**
     * 版本序号（第几次回答，1 起）
     */
    private Integer version;

    /**
     * 是否当前生效版本：true 是、false 否
     */
    private Boolean isCurrent;

    /**
     * 回答内容（aiContent 数组 或 aiAgentMessage 对象，JSON）
     */
    private String content;

    /**
     * 知识库引用元数据
     */
    private String metadata;

    /**
     * 工具调用记录
     */
    private String tools;

    /**
     * 生成的文件 ID 列表（JSON）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> fileIds;

    /**
     * 是否删除
     */
    private Boolean deleted;

}
