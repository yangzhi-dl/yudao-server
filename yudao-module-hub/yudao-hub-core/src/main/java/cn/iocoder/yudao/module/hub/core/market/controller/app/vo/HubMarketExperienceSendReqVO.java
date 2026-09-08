package cn.iocoder.yudao.module.hub.core.market.controller.app.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "用户前台 - Hub 智能体市场体验消息发送请求对象")
@Data
public class HubMarketExperienceSendReqVO {

    @Schema(description = "市场记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @NotNull(message = "市场记录编号不能为空")
    private Long marketId;

    @Schema(description = "AI 会话主题编号；首次发送时不传", example = "100")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long topicId;

    @Schema(description = "上一条对话编号；首次发送时不传", example = "200")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long lastId;

    @Schema(description = "本次消息上传的文件编号列表", example = "[\"1\", \"2\"]")
    @Size(max = 10, message = "上传文件数量不能超过 10 个")
    private List<Long> fileIds;

    @Schema(description = "用户问题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户问题不能为空")
    @Size(max = 10000, message = "用户问题不能超过 10000 个字符")
    private String question;

}
