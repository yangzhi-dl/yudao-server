package cn.iocoder.yudao.module.ai.knowledge.document.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindDocumentPageListDTO {

    /**
     * 文档标题
     */
    private String title;

    /**
     * 发布的起始日期
     */
    private LocalDateTime startDate;

    /**
     * 发布的结束日期
     */
    private LocalDateTime endDate;

    private Long categoryId;

    private List<Long> tagIds;

    /**
     * 文档类型
     */
    private Integer type;

    //页码
    private int page;

    //每页显示记录数
    private int pageSize;

}
