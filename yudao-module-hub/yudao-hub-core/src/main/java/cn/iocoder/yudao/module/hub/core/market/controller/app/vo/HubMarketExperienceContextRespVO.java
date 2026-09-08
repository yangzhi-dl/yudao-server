package cn.iocoder.yudao.module.hub.core.market.controller.app.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户前台 - Hub 智能体市场体验上下文响应对象")
@Data
public class HubMarketExperienceContextRespVO {

    @Schema(description = "市场记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long marketId;

    @Schema(description = "智能体类型：0 单智能体，1 多智能体", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer agentType;

    @Schema(description = "市场展示名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "市场展示摘要")
    private String summary;

    @Schema(description = "市场封面访问地址")
    private String coverUrl;

    @Schema(description = "服务提供方名称")
    private String providerName;

}
