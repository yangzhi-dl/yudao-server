package cn.iocoder.yudao.module.ai.data.governance.controller.admin.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 人工编辑资产请求 VO。
 */
@Data
public class AssetEditReqVO {

    @NotNull(message = "资产 ID 不能为空")
    private Long assetId;

    private String title;

    private String summary;

    @NotBlank(message = "正文内容不能为空")
    private String content;

}
