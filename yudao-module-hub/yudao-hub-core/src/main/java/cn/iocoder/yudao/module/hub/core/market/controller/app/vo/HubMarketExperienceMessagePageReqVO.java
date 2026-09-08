package cn.iocoder.yudao.module.hub.core.market.controller.app.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "用户前台 - Hub 智能体体验消息分页请求对象")
@Data
@EqualsAndHashCode(callSuper = true)
public class HubMarketExperienceMessagePageReqVO extends PageParam {

    @Schema(description = "市场记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @NotNull(message = "市场记录编号不能为空")
    private Long marketId;

    @Schema(description = "AI 会话主题编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @NotNull(message = "AI 会话主题编号不能为空")
    private Long topicId;

}
