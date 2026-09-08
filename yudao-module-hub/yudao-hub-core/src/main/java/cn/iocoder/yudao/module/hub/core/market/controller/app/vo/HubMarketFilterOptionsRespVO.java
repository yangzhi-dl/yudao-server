package cn.iocoder.yudao.module.hub.core.market.controller.app.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户前台 - Hub 智能体市场筛选项响应对象")
@Data
public class HubMarketFilterOptionsRespVO {

    @Schema(description = "智能体分类列表")
    private List<OptionVO> categories;

    @Schema(description = "智能体标签列表")
    private List<OptionVO> tags;

    @Schema(description = "市场筛选项")
    @Data
    public static class OptionVO {

        @Schema(description = "字典数据编号", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private Long id;

        @Schema(description = "显示名称", requiredMode = Schema.RequiredMode.REQUIRED)
        private String label;

    }

}
