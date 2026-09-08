package cn.iocoder.yudao.module.ai.knowledge.document.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversionUpdateDocument {

    private Long id;

    private String title;

    private String content;

    private Long cover;

    private String summary;

    private Long categoryId;

    private List<Long> tagIds;

    private Long fileId;

}
