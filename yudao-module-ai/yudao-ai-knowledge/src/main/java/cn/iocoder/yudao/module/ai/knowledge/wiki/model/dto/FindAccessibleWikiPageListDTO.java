package cn.iocoder.yudao.module.ai.knowledge.wiki.model.dto;

import cn.iocoder.yudao.module.ai.knowledge.wiki.enums.WikiTypeEnum;
import lombok.Data;

@Data
public class FindAccessibleWikiPageListDTO {

    /**
     * 知识库标题
     */
    private String title;

    private WikiTypeEnum type;

    //页码
    private int page;

    //每页显示记录数
    private int pageSize;

}
