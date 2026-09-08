package cn.iocoder.yudao.module.hub.core.market.controller.app.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "用户前台 - Hub 智能体体验会话重命名请求对象")
@Data
public class HubMarketExperienceTopicRenameReqVO {

    @Schema(description = "市场记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @NotNull(message = "市场记录编号不能为空")
    private Long marketId;

    @Schema(description = "AI 会话主题编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @NotNull(message = "AI 会话主题编号不能为空")
    private Long topicId;

    @Schema(description = "新会话标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "季度经营报告")
    @NotBlank(message = "新会话标题不能为空")
    @Size(max = 100, message = "新会话标题不能超过 100 个字符")
    private String title;

}
