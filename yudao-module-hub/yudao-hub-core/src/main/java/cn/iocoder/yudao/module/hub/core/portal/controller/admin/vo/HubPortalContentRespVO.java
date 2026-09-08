package cn.iocoder.yudao.module.hub.core.portal.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - Hub 门户内容响应对象")
@Data
public class HubPortalContentRespVO {

    private Long id;
    private String pageCode;
    private String contentType;
    private String code;
    private String parentCode;
    private String eyebrow;
    private String title;
    private String summary;
    private String description;
    private String imageUrl;
    private String actionText;
    private String actionUrl;
    private String secondaryActionText;
    private String secondaryActionUrl;
    private String scenario;
    private String outcome;
    private List<String> highlights;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
