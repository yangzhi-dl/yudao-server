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
public class BatchFindWikiCatalogDocumentsDTO {

    @NotNull(message = "知识库 ID 不能为空")
    private Long wikiId;

    @NotNull(message = "父目录 ID 列表不能为空")
    private List<Long> parentIds;

}
