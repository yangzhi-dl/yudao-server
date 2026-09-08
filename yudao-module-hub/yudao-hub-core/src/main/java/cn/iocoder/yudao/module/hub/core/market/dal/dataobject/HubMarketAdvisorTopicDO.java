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
 * 市场选型助手会话关联。
 *
 * <p>选型助手不属于市场商品，因此不复用 {@code hub_market_agent}。该表只用于约束
 * 当前用户可访问的 AI 会话主题，并保留创建会话时的助手快照。</p>
 */
@TableName("hub_market_advisor_topic")
@KeySequence("hub_market_advisor_topic_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HubMarketAdvisorTopicDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** AI 会话主题编号。 */
    private Long topicId;

    /** 体验用户编号。 */
    private Long userId;

    /** 创建会话时的选型助手编号快照。 */
    private Long advisorAgentId;

    /** 创建会话时的选型助手租户编号快照。 */
    private Long advisorTenantId;

    /** 创建会话时的智能体类型快照。 */
    private Integer agentType;

    /** 最近使用时间。 */
    private LocalDateTime lastActiveTime;

}
