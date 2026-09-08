package cn.iocoder.yudao.module.analytics.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 名称数量 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsNameCountRespVO {

    @Schema(description = "名称")
    private String name;

    @Schema(description = "数量")
    private Long count;

}