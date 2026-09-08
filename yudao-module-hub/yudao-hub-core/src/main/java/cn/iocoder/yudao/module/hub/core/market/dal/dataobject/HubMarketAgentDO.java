package cn.iocoder.yudao.module.hub.core.market.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.hub.core.market.enums.HubMarketAgentTypeEnum;
import cn.iocoder.yudao.module.hub.core.market.enums.HubMarketPublishStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Hub 智能体市场发布记录数据对象。
 */
@TableName(value = "hub_market_agent", autoResultMap = true)
@KeySequence("hub_market_agent_seq")
@TenantIgnore
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HubMarketAgentDO extends BaseDO {

    /** 市场记录编号。 */
    @TableId
    private Long id;

    /** 来源智能体编号。 */
    private Long agentId;

    /** 来源智能体所属租户编号。 */
    private Long sourceTenantId;

    /** 智能体类型，参见 {@link HubMarketAgentTypeEnum}。 */
    private Integer agentType;

    /** 市场展示名称。 */
    private String name;

    /** 市场列表摘要。 */
    private String summary;

    /** 市场封面文件编号。 */
    private Long coverId;

    /** 市场分类编号。 */
    private Long categoryId;

    /** 市场标签编号列表。 */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<Long> tagIds;

    /** 服务提供方名称。 */
    private String providerName;

    /** 是否推荐。 */
    private Boolean recommended;

    /** 是否允许进入试用。 */
    private Boolean trialEnabled;

    /** 显示顺序。 */
    private Integer sort;

    /** 发布状态，参见 {@link HubMarketPublishStatusEnum}。 */
    private Integer publishStatus;

    /** 发布时间。 */
    private LocalDateTime publishTime;

}
