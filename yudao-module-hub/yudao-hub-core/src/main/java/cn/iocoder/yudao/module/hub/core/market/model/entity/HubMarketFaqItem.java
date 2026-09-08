package cn.iocoder.yudao.module.hub.core.market.model.entity;

import lombok.Data;

/**
 * 市场详情常见问题项。
 */
@Data
public class HubMarketFaqItem {

    /** 问题。 */
    private String question;

    /** 回答。 */
    private String answer;

}
