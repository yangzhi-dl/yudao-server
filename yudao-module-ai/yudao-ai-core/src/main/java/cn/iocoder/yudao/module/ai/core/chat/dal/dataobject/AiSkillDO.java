package cn.iocoder.yudao.module.ai.core.chat.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;

/**
 * AI 技能 DO
 *
 * @author yudao
 */
@TableName(value = "ai_skill", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSkillDO extends BaseDO {

    /**
     * 技能编号
     */
    @TableId
    private Long id;

    /**
     * 技能名称（小写字母、数字、连字符，对应 SKILL.md 的 name 字段）
     */
    private String name;

    /**
     * 技能展示名称
     */
    private String displayName;

    /**
     * 技能描述（LLM 据此判断何时使用该技能）
     */
    private String description;

    /**
     * SKILL.md 完整内容（YAML frontmatter + Markdown 正文）
     */
    private String skillMdContent;

    /**
     * 技能图标文件 ID
     */
    private Long icon;

    /**
     * 技能分类 ID
     */
    private Long categoryId;

    /**
     * 技能标签 ID 列表（JSON）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> tagIds;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 状态：0=禁用、1=启用
     */
    private Integer status;

    /**
     * 租户 ID
     */
    private Long tenantId;

}