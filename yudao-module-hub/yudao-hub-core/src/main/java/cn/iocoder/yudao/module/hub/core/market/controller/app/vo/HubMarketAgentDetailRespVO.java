package cn.iocoder.yudao.module.hub.core.market.controller.app.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Schema(description = "用户前台 - Hub 智能体市场详情响应对象")
@Data
@EqualsAndHashCode(callSuper = true)
public class HubMarketAgentDetailRespVO extends HubMarketAgentRespVO {

    @Schema(description = "详情介绍")
    private String introduction;

    @Schema(description = "核心能力列表")
    private List<TextItemVO> features;

    @Schema(description = "适用场景列表")
    private List<TextItemVO> scenarios;

    @Schema(description = "产品优势列表")
    private List<TextItemVO> advantages;

    @Schema(description = "使用或处理流程列表")
    private List<ProcessItemVO> usageProcess;

    @Schema(description = "案例说明")
    private String caseDescription;

    @Schema(description = "详情图片列表")
    private List<GalleryItemVO> gallery;

    @Schema(description = "试用说明")
    private String trialDescription;

    @Schema(description = "私有化部署说明")
    private String deploymentDescription;

    @Schema(description = "常见问题列表")
    private List<FaqItemVO> faq;

    @Schema(description = "文本内容项")
    @Data
    public static class TextItemVO {

        @Schema(description = "标题")
        private String title;

        @Schema(description = "说明")
        private String description;

        @Schema(description = "可选图标编码")
        private String icon;

    }

    @Schema(description = "流程内容项")
    @Data
    public static class ProcessItemVO {

        @Schema(description = "标题")
        private String title;

        @Schema(description = "说明")
        private String description;

        @Schema(description = "配图文件编号")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private Long imageId;

        @Schema(description = "配图访问地址")
        private String imageUrl;

    }

    @Schema(description = "详情图片项")
    @Data
    public static class GalleryItemVO {

        @Schema(description = "文件编号")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private Long fileId;

        @Schema(description = "图片访问地址")
        private String imageUrl;

        @Schema(description = "图片标题")
        private String title;

        @Schema(description = "图片说明")
        private String description;

    }

    @Schema(description = "常见问题项")
    @Data
    public static class FaqItemVO {

        @Schema(description = "问题")
        private String question;

        @Schema(description = "回答")
        private String answer;

    }

}
