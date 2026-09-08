package cn.iocoder.yudao.module.ai.core.chat.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.ai.core.chat.enums.AgentType;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.MultiAgentSettings;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;

/**
 * AI 智能体 DO
 *
 * @author yudao
 */
@TableName(value = "ai_chat_agents", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAgentDO extends TenantBaseDO {

    /**
     * 智能体编号
     */
    @TableId
    private Long id;

    /**
     * 关联模型编号
     */
    private Long modelId;

    /**
     * 分类编号
     */
    private Long categoryId;

    /**
     * 标签编号列表（JSON）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> tagIds;

    /**
     * 智能体名称
     */
    private String name;

    /**
     * 封面图片
     */
    private Long cover;

    /**
     * 智能体类型
     *
     * 枚举 {@link AgentType}
     */
    @TableField("`type`")
    private AgentType type;

    /**
     * 提示词
     */
    private String prompt;

    /**
     * 智能体描述
     */
    private String description;

    /**
     * 智能体设置列表（JSON）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<MultiAgentSettings> settings;

    /**
     * 关联技能编号列表（JSON）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> skillIds;

    /**
     * 关联工具编号列表（JSON）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> tools;

}
