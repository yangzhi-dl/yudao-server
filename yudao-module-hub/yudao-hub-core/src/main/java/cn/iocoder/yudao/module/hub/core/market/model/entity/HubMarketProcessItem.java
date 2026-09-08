package cn.iocoder.yudao.module.hub.core.market.model.entity;

import lombok.Data;

/**
 * 市场详情流程项。
 */
@Data
public class HubMarketProcessItem {

    /** 标题。 */
    private String title;

    /** 说明。 */
    private String description;

    /** 配图文件编号。 */
    private Long imageId;

}
