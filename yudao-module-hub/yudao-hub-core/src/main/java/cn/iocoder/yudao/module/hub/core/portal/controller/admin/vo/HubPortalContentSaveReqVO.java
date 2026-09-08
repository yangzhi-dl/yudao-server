package cn.iocoder.yudao.module.hub.core.portal.controller.admin.vo;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.hub.core.portal.enums.HubPortalContentTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - Hub 门户内容创建或修改请求对象")
@Data
public class HubPortalContentSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "页面编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "portal")
    @NotBlank(message = "页面编码不能为空")
    @Size(max = 32, message = "页面编码不能超过 32 个字符")
    private String pageCode;

    @Schema(description = "内容类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "solution")
    @NotBlank(message = "内容类型不能为空")
    @InEnum(HubPortalContentTypeEnum.class)
    private String contentType;

    @Schema(description = "业务编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "manufacturing")
    @NotBlank(message = "业务编码不能为空")
    @Size(max = 64, message = "业务编码不能超过 64 个字符")
    private String code;

    @Schema(description = "父级内容编码", example = "enterprise-knowledge")
    @Size(max = 64, message = "父级内容编码不能超过 64 个字符")
    private String parentCode;

    @Schema(description = "辅助标签", example = "智能制造")
    @Size(max = 128, message = "辅助标签不能超过 128 个字符")
    private String eyebrow;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不能超过 200 个字符")
    private String title;

    @Schema(description = "简短摘要")
    @Size(max = 500, message = "摘要不能超过 500 个字符")
    private String summary;

    @Schema(description = "详细描述")
    private String description;

    @Schema(description = "图片地址")
    @Size(max = 1024, message = "图片地址不能超过 1024 个字符")
    private String imageUrl;

    @Schema(description = "主要操作文字")
    @Size(max = 64, message = "主要操作文字不能超过 64 个字符")
    private String actionText;

    @Schema(description = "主要操作地址")
    @Size(max = 1024, message = "主要操作地址不能超过 1024 个字符")
    private String actionUrl;

    @Schema(description = "次要操作文字")
    @Size(max = 64, message = "次要操作文字不能超过 64 个字符")
    private String secondaryActionText;

    @Schema(description = "次要操作地址")
    @Size(max = 1024, message = "次要操作地址不能超过 1024 个字符")
    private String secondaryActionUrl;

    @Schema(description = "业务场景说明")
    private String scenario;

    @Schema(description = "结果说明")
    private String outcome;

    @Schema(description = "卖点、基础能力或标签列表")
    @Size(max = 12, message = "标签数量不能超过 12 个")
    private List<@Size(max = 100, message = "单个标签不能超过 100 个字符") String> highlights;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

}
