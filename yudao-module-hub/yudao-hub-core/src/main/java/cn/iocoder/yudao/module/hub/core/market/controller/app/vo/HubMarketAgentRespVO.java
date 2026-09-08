package cn.iocoder.yudao.module.hub.core.market.controller.app.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "用户前台 - Hub 智能体市场列表响应对象")
@Data
public class HubMarketAgentRespVO {

    @Schema(description = "市场记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @Schema(description = "智能体类型：0 单智能体，1 多智能体", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer agentType;

    @Schema(description = "市场展示名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "市场列表摘要")
    private String summary;

    @Schema(description = "封面文件编号")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long coverId;

    @Schema(description = "封面访问地址")
    private String coverUrl;

    @Schema(description = "分类编号")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "标签编号列表")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private List<Long> tagIds;

    @Schema(description = "标签名称列表")
    private List<String> tagNames;

    @Schema(description = "服务提供方名称")
    private String providerName;

    @Schema(description = "是否推荐", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean recommended;

    @Schema(description = "是否允许进入试用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean trialEnabled;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

}
