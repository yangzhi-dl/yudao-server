package cn.iocoder.yudao.module.ai.core.chat.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * AI 技能资源 DO
 *
 * @author yudao
 */
@TableName(value = "ai_skill_resource")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSkillResourceDO extends BaseDO {

    /**
     * 资源编号
     */
    @TableId
    private Long id;

    /**
     * 关联技能 ID
     */
    private Long skillId;

    /**
     * 资源类型：0=脚本(scripts)、1=参考资料(references)、2=示例(examples)、3=其他
     */
    private Integer resourceType;

    /**
     * 文件 ID（关联文件上传系统）
     */
    private Long fileId;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 租户 ID
     */
    private Long tenantId;

}