package cn.iocoder.yudao.module.ai.common.model.dto;

import lombok.Data;

@Data
public class BasePageListDTO {

    //页码
    private int page;

    //每页显示记录数
    private int pageSize;

}
