package cn.iocoder.yudao.module.hub.core.market.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Schema(description = "用户前台 - Hub 智能体集群详情响应对象")
@Data
@EqualsAndHashCode(callSuper = true)
public class HubClusterDetailRespVO extends HubClusterRespVO {

    @Schema(description = "公开详情介绍")
    private String introduction;

    @Schema(description = "适用任务列表")
    private List<ContentItemVO> taskItems;

    @Schema(description = "公开能力或可获得内容列表")
    private List<ContentItemVO> capabilityItems;

    @Schema(description = "试用说明")
    private String trialDescription;

    @Schema(description = "私有化部署说明")
    private String deploymentDescription;

    @Schema(description = "公开内容项")
    @Data
    public static class ContentItemVO {

        @Schema(description = "标题")
        private String title;

        @Schema(description = "说明")
        private String description;

    }

}
