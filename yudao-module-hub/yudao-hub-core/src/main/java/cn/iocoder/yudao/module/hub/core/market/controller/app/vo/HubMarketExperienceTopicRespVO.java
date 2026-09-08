package cn.iocoder.yudao.module.hub.core.market.controller.app.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户前台 - Hub 智能体体验会话响应对象")
@Data
public class HubMarketExperienceTopicRespVO {

    @Schema(description = "AI 会话主题编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long topicId;

    @Schema(description = "会话标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "生成季度经营报告")
    private String title;

    @Schema(description = "会话创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "最近使用时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime lastActiveTime;

}
