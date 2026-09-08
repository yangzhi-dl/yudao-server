package cn.iocoder.yudao.module.analytics.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 趋势 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsTrendRespVO {

    @Schema(description = "日期（yyyy-MM-dd）")
    private String date;

    @Schema(description = "数量")
    private Long count;

}