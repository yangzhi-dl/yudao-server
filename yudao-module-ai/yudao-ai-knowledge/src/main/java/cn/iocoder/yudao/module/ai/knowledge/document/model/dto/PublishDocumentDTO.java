package cn.iocoder.yudao.module.ai.knowledge.document.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PublishDocumentDTO {

    @NotBlank(message = "文档标题不能为空")
    @Length(min = 1, max = 40, message = "文档标题字数需大于 1 小于 40")
    private String title;

    @NotBlank(message = "文档内容不能为空")
    private String content;

    @NotNull(message = "文档封面不能为空")
    private Long cover;

    private String summary;

    @NotNull(message = "文档分类不能为空")
    private Long categoryId;

    @NotEmpty(message = "文档标签不能为空")
    private List<Long> tagIds;

    @NotNull(message = "是否置顶不为空")
    private Boolean isTop;
}
