package cn.iocoder.yudao.module.ai.knowledge.document.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTaxonomy {

    private Long categoryId;

    private List<Long> tagIds;

}
