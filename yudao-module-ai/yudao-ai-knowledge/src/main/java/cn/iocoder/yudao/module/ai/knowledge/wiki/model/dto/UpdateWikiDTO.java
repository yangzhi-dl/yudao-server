package cn.iocoder.yudao.module.ai.knowledge.wiki.model.dto;

import cn.iocoder.yudao.module.ai.knowledge.wiki.enums.WikiTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateWikiDTO {

    private Long id;

    @NotBlank(message = "知识库标题不能为空")
    @Length(min = 1, max = 20, message = "知识库标题字数需大于 1 小于 20")
    private String title;

    @NotBlank(message = "知识库摘要不能为空")
    private String summary;

    @NotNull(message = "知识库封面不能为空")
    private Long cover;

    @NotNull(message = "知识库类型不能为空")
    private WikiTypeEnum type;

}
