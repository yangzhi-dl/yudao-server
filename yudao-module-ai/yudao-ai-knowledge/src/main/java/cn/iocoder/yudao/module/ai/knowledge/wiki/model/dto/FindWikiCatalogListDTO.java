package cn.iocoder.yudao.module.ai.knowledge.wiki.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindWikiCatalogListDTO {

    @NotNull(message = "知识库 ID 不能为空")
    private Long wikiId;

    private Long parentId;

    private String title;

    private Long categoryId;

    private List<Long> tagIds;

    private Integer page;

    private Integer pageSize;

}
