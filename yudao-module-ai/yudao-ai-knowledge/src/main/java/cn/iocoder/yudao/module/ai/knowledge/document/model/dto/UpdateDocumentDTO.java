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

/**
 * @Author: Aitenry
 * @Date: 2023/01/22 00:00
 * @Version: v1.0.0
 * @Description: TODO
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateDocumentDTO {

    @NotNull(message = "文档 ID 不能为空")
    private Long id;

    @NotBlank(message = "文档标题不能为空")
    @Length(min = 1, max = 60, message = "文档标题字数需大于 1 小于 40")
    private String title;

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

    private Long fileId;

}
