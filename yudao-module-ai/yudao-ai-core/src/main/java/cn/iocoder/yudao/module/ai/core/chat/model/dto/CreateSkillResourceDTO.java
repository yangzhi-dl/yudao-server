package cn.iocoder.yudao.module.ai.core.chat.model.dto;

import lombok.Data;

@Data
public class CreateSkillResourceDTO {

    private Long skillId;

    private Integer resourceType;

    private Long fileId;

    private Integer sortOrder;

}