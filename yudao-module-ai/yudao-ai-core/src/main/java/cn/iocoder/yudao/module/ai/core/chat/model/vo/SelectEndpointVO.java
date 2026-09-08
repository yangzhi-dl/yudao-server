package cn.iocoder.yudao.module.ai.core.chat.model.vo;


import cn.iocoder.yudao.module.ai.core.chat.enums.AiApiType;
import cn.iocoder.yudao.module.ai.core.chat.enums.AiModelType;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SelectEndpointVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long id;

    private String name;

    private String description;

    private AiApiType apiType;

    private AiModelType modelType;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private List<Long> toolIds;

}


