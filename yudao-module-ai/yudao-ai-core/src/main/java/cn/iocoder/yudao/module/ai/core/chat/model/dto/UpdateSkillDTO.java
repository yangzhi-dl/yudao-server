package cn.iocoder.yudao.module.ai.core.chat.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateSkillDTO {

    private Long id;

    private String name;

    private String displayName;

    private String description;

    private String skillMdContent;

    private Long icon;

    private Long categoryId;

    private List<Long> tagIds;

    private Integer version;

    private Integer status;

}