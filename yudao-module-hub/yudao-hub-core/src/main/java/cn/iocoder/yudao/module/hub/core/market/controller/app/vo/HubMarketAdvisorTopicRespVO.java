package cn.iocoder.yudao.module.hub.core.market.controller.app.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户前台 - Hub 市场选型助手会话响应对象")
@Data
public class HubMarketAdvisorTopicRespVO {

    @Schema(description = "AI 会话主题编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long topicId;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "最近使用时间")
    private LocalDateTime lastActiveTime;

}
