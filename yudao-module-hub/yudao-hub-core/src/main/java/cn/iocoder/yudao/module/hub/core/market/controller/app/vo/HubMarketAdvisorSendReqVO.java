package cn.iocoder.yudao.module.hub.core.market.controller.app.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "用户前台 - Hub 市场选型助手消息发送请求对象")
@Data
public class HubMarketAdvisorSendReqVO {

    @Schema(description = "AI 会话主题编号；首次发送时不传", example = "100")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long topicId;

    @Schema(description = "上一条对话编号；首次发送时不传", example = "200")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long lastId;

    @Schema(description = "用户需求", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户需求不能为空")
    @Size(max = 10_000, message = "用户需求不能超过 10000 个字符")
    private String question;

}
