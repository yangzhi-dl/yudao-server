package cn.iocoder.yudao.module.ai.core.chat.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillResourceVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long skillId;

    private Integer resourceType;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long fileId;

    private String fileName;

    private String fileUrl;

    private Integer sortOrder;

}