package cn.iocoder.yudao.module.hub.core.market.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.hub.core.market.model.entity.HubMarketFaqItem;
import cn.iocoder.yudao.module.hub.core.market.model.entity.HubMarketGalleryItem;
import cn.iocoder.yudao.module.hub.core.market.model.entity.HubMarketProcessItem;
import cn.iocoder.yudao.module.hub.core.market.model.entity.HubMarketTextItem;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * Hub 智能体市场详情数据对象。
 */
@TableName(value = "hub_market_agent_detail", autoResultMap = true)
@KeySequence("hub_market_agent_detail_seq")
@TenantIgnore
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HubMarketAgentDetailDO extends BaseDO {

    /** 市场详情编号。 */
    @TableId
    private Long id;

    /** 市场记录编号。 */
    private Long marketAgentId;

    /** 详情介绍。 */
    private String introduction;

    /** 核心能力列表。 */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<HubMarketTextItem> features;

    /** 适用场景列表。 */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<HubMarketTextItem> scenarios;

    /** 产品优势列表。 */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<HubMarketTextItem> advantages;

    /** 使用或处理流程列表。 */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<HubMarketProcessItem> usageProcess;

    /** 案例说明。 */
    private String caseDescription;

    /** 详情图片列表。 */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<HubMarketGalleryItem> gallery;

    /** 试用说明。 */
    private String trialDescription;

    /** 私有化部署说明。 */
    private String deploymentDescription;

    /** 常见问题列表。 */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<HubMarketFaqItem> faq;

}
