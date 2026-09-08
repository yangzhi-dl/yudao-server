package cn.iocoder.yudao.module.ai.knowledge.wiki.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class FindWikiDocumentPreNextDTO {

    @NotNull(message = "知识库 ID 不能为空")
    private Long id;

    @NotNull(message = "文档 ID 不能为空")
    private Long documentId;

}