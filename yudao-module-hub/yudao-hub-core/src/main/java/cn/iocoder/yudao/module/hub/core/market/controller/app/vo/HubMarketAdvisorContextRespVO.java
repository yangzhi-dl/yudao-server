package cn.iocoder.yudao.module.hub.core.market.controller.app.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户前台 - Hub 市场选型助手上下文响应对象")
@Data
public class HubMarketAdvisorContextRespVO {

    @Schema(description = "选型助手智能体编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long agentId;

    @Schema(description = "智能体类型：0 标准智能体，1 多智能体", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer agentType;

    @Schema(description = "助手名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "助手描述")
    private String summary;

    @Schema(description = "助手封面访问地址")
    private String coverUrl;

}
