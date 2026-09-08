package cn.iocoder.yudao.module.ai.core.chat.model.dto;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateSkillDTO extends BaseDO {

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