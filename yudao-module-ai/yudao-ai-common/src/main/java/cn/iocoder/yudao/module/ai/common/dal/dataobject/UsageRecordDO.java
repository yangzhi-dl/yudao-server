package cn.iocoder.yudao.module.ai.common.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.common.enums.UsageRecordType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * AI 使用记录 DO
 *
 * @author yudao
 */
@TableName("ai_usage_record")
@KeySequence("ai_usage_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageRecordDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 使用类型
     *
     * 枚举 {@link UsageRecordType}
     */
    @TableField("`type`")
    private UsageRecordType type;

    /**
     * 关联对象编号
     */
    private Long objectId;

}
