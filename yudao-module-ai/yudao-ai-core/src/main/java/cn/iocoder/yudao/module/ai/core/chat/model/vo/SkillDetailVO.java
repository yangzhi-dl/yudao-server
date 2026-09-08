package cn.iocoder.yudao.module.ai.core.chat.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillDetailVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String name;

    private String displayName;

    private String description;

    private String skillMdContent;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long icon;

    private String imageUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long categoryId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private List<Long> tagIds;

    private Integer version;

    private Integer status;

}