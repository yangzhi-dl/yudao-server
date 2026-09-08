package cn.iocoder.yudao.module.hub.core.market.model.entity;

import lombok.Data;

/**
 * 市场详情文本内容项。
 */
@Data
public class HubMarketTextItem {

    /** 标题。 */
    private String title;

    /** 说明。 */
    private String description;

    /** 可选图标编码。 */
    private String icon;

}
