package cn.iocoder.yudao.module.hub.core.market.controller.app.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "用户前台 - Hub 市场选型助手消息分页请求对象")
@Data
@EqualsAndHashCode(callSuper = true)
public class HubMarketAdvisorMessagePageReqVO extends PageParam {

    @Schema(description = "AI 会话主题编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @NotNull(message = "AI 会话主题编号不能为空")
    private Long topicId;

}
