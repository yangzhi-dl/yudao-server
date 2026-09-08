package cn.iocoder.yudao.module.ai.knowledge.wiki.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RemoveWikiDocumentDTO {

    @NotNull(message = "知识库 ID 不能为空")
    private Long wikiId;

    @NotNull(message = "文档 ID 不能为空")
    private Long documentId;

}
