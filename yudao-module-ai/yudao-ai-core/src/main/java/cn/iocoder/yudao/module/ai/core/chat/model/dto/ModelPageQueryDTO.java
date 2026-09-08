package cn.iocoder.yudao.module.ai.core.chat.model.dto;

import cn.iocoder.yudao.module.ai.core.chat.enums.AiApiType;
import cn.iocoder.yudao.module.ai.core.chat.enums.AiModelType;
import lombok.Data;

import java.util.List;

/**
 * @Author: Aitenry
 * @Date: 2025/11/23 14:25
 * @Version: v1.0.0
 * @Description: TODO
 **/
@Data
public class ModelPageQueryDTO {

    private String name;

    private AiApiType type;

    private AiModelType modelType;

    private Boolean isOnline;

    private List<Long> filterIds;

    //页码
    private int page;

    //每页显示记录数
    private int pageSize;

}
