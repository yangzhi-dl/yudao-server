package cn.iocoder.yudao.module.ai.core.chat.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserProfessionPageQueryDTO {

    private String name;

    private Long categoryId;

    private List<Long> tagIds;

    //页码
    private int page;

    //每页显示记录数
    private int pageSize;

}
