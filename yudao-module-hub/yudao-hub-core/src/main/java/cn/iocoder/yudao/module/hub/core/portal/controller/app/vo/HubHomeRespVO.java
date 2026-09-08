package cn.iocoder.yudao.module.hub.core.portal.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户前台 - Hub 门户首页响应对象")
@Data
public class HubHomeRespVO {

    private String pageCode;
    private HeroVO hero;
    private List<SectionVO> sections;
    private List<CapabilityVO> capabilities;
    private List<ProductVO> products;
    private List<SolutionVO> solutions;
    private List<CaseVO> cases;
    private PrivateDeploymentVO privateDeployment;
    private CallToActionVO callToAction;

    @Data
    public static class HeroVO {

        private String eyebrow;
        private String title;
        private String summary;
        private String imageUrl;
        private String actionText;
        private String actionUrl;
        private String secondaryActionText;
        private String secondaryActionUrl;
        private List<String> proofPoints;

    }

    @Data
    public static class SectionVO {

        private String code;
        private String eyebrow;
        private String title;
        private String summary;
        private String description;

    }

    @Data
    public static class CapabilityVO {

        private String code;
        private String eyebrow;
        private String title;
        private String description;
        private String imageUrl;
        private List<String> highlights;

    }

    @Data
    public static class ProductVO {

        private String code;
        private String title;
        private String summary;
        private String description;
        private List<String> highlights;
        private String imageUrl;
        private List<ProductMediaVO> media;
        private String actionText;
        private String actionUrl;

    }

    @Data
    public static class ProductMediaVO {

        private String code;
        private String title;
        private String summary;
        private String imageUrl;

    }

    @Data
    public static class SolutionVO {

        private String code;
        private String industry;
        private String title;
        private String description;
        private String scenario;
        private String outcome;
        private List<String> foundations;
        private String imageUrl;
        private List<SolutionMediaVO> media;
        private String actionText;
        private String actionUrl;

    }

    @Data
    public static class SolutionMediaVO {

        private String code;
        private String label;
        private String title;
        private String summary;
        private String imageUrl;

    }

    @Data
    public static class CaseVO {

        private String code;
        private String label;
        private String title;
        private String description;
        private String outcome;
        private String imageUrl;

    }

    @Data
    public static class PrivateDeploymentVO {

        private String title;
        private String description;
        private String imageUrl;
        private List<String> highlights;
        private String actionText;
        private String actionUrl;

    }

    @Data
    public static class CallToActionVO {

        private String eyebrow;
        private String title;
        private String summary;
        private String actionText;
        private String actionUrl;

    }

}
