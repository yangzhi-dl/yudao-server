package cn.iocoder.yudao.module.ai.knowledge.document.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class FindUserDocumentPageListDTO {

    /**
     * 文档标题
     */
    private String title;

    private Long categoryId;

    private List<Long> tagIds;

    //页码
    private int page;

    //每页显示记录数
    private int pageSize;

}
