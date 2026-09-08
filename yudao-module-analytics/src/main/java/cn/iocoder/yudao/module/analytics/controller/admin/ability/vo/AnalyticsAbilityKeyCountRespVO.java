package cn.iocoder.yudao.module.analytics.controller.admin.ability.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 能力分布键值对 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsAbilityKeyCountRespVO {

    @Schema(description = "枚举值")
    private Integer key;

    @Schema(description = "数量")
    private Long count;

}