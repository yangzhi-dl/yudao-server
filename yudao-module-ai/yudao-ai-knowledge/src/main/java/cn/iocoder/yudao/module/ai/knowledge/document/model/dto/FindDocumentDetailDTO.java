package cn.iocoder.yudao.module.ai.knowledge.document.model.dto;

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
public class FindDocumentDetailDTO {

    private Long wikiId;

    /**
     * 文档 ID
     */
    private Long documentId;
}