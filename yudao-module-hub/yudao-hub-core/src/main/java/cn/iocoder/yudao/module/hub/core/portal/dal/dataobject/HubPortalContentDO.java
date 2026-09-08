package cn.iocoder.yudao.module.hub.core.portal.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.hub.core.portal.enums.HubPortalContentTypeEnum;
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
 * Hub 门户内容数据对象。
 */
@TableName(value = "hub_portal_content", autoResultMap = true)
@KeySequence("hub_portal_content_seq")
@TenantIgnore
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HubPortalContentDO extends BaseDO {

    @TableId
    private Long id;

    /** 页面编码，例如 portal。 */
    private String pageCode;

    /** 内容类型，参见 {@link HubPortalContentTypeEnum}。 */
    private String contentType;

    /** 页面内稳定业务编码。 */
    private String code;

    /**
     * 父级内容编码。
     *
     * 用于把解决方案素材等子内容归属到对应的父级内容，顶级内容不填写。
     */
    private String parentCode;

    /** 辅助标签，例如行业或栏目标签。 */
    private String eyebrow;

    private String title;
    private String summary;
    private String description;
    private String imageUrl;
    private String actionText;
    private String actionUrl;
    private String secondaryActionText;
    private String secondaryActionUrl;
    private String scenario;
    private String outcome;

    /** 卖点、基础能力或标签列表。 */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> highlights;

    private Integer sort;
    private Integer status;

}
