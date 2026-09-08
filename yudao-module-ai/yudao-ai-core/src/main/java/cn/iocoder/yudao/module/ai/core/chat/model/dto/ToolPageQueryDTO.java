package cn.iocoder.yudao.module.ai.core.chat.model.dto;

import cn.iocoder.yudao.module.ai.core.chat.enums.ToolType;
import lombok.Data;

import java.util.List;

@Data
public class ToolPageQueryDTO {

    private String rename;

    private ToolType type;

    private Boolean isOnline;

    private List<Long> filterIds;

    //页码
    private int page;

    //每页显示记录数
    private int pageSize;

}
