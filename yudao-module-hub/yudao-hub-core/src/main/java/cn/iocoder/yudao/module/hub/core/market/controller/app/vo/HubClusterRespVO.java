package cn.iocoder.yudao.module.hub.core.market.controller.app.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户前台 - Hub 智能体集群列表响应对象")
@Data
public class HubClusterRespVO {

    @Schema(description = "市场记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long marketId;

    @Schema(description = "集群展示名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "集群摘要")
    private String summary;

    @Schema(description = "封面访问地址")
    private String coverUrl;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "标签名称列表")
    private List<String> tagNames;

    @Schema(description = "是否允许进入试用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean trialEnabled;

    @Schema(description = "可展示子成员数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer memberCount;

    @Schema(description = "集群子成员列表")
    private List<MemberVO> members;

    /**
     * 仅公开多智能体 settings 中可面向用户展示的名称和职责。
     */
    @Schema(description = "集群子成员")
    @Data
    public static class MemberVO {

        @Schema(description = "成员名称", requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        @Schema(description = "成员职责说明", requiredMode = Schema.RequiredMode.REQUIRED)
        private String description;

    }

}
