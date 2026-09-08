package cn.iocoder.yudao.module.hub.core.market.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Hub 智能体市场体验会话关联数据对象。
 */
@TableName("hub_market_experience_topic")
@KeySequence("hub_market_experience_topic_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HubMarketExperienceTopicDO extends TenantBaseDO {

    /** 市场体验会话关联编号。 */
    @TableId
    private Long id;

    /** 市场记录编号。 */
    private Long marketAgentId;

    /** AI 会话主题编号。 */
    private Long topicId;

    /** 体验用户编号。 */
    private Long userId;

    /** 创建会话时的来源智能体编号快照。 */
    private Long sourceAgentId;

    /** 创建会话时的来源智能体租户编号快照。 */
    private Long sourceTenantId;

    /** 创建会话时的智能体类型快照。 */
    private Integer agentType;

    /** 最近使用时间。 */
    private LocalDateTime lastActiveTime;

}
