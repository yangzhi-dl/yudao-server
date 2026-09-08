package cn.iocoder.yudao.module.hub.core.portal.controller.admin.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - Hub 门户内容分页请求对象")
@Data
@EqualsAndHashCode(callSuper = true)
public class HubPortalContentPageReqVO extends PageParam {

    @Schema(description = "页面编码", example = "portal")
    private String pageCode;

    @Schema(description = "内容类型", example = "solution")
    private String contentType;

    @Schema(description = "业务编码", example = "manufacturing")
    private String code;

    @Schema(description = "标题，模糊匹配")
    private String title;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
