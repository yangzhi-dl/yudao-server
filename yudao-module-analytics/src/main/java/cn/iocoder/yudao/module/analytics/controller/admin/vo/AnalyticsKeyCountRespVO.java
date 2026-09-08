package cn.iocoder.yudao.module.analytics.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 数值键分布 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsKeyCountRespVO {

    @Schema(description = "枚举值")
    private Integer key;

    @Schema(description = "数量")
    private Long count;

}