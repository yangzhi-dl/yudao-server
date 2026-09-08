package cn.iocoder.yudao.module.hub.core.market.model.entity;

import lombok.Data;

/**
 * 市场详情图片项。
 */
@Data
public class HubMarketGalleryItem {

    /** 文件编号。 */
    private Long fileId;

    /** 图片标题。 */
    private String title;

    /** 图片说明。 */
    private String description;

}
