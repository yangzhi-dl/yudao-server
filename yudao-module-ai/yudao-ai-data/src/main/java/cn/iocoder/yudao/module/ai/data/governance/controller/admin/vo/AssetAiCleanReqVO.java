package cn.iocoder.yudao.module.ai.data.governance.controller.admin.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * AI 清洗资产请求 VO。
 */
@Data
public class AssetAiCleanReqVO {

    @NotNull(message = "资产 ID 不能为空")
    private Long assetId;

    /**
     * 自定义提示词，为空时使用默认提示词。
     */
    private String prompt;

}
