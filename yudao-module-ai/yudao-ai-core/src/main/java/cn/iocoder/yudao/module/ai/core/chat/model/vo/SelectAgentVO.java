package cn.iocoder.yudao.module.ai.core.chat.model.vo;

import cn.iocoder.yudao.module.ai.core.chat.enums.AgentType;
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
public class SelectAgentVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long modelId;

    private String name;

    private AgentType type;

    private String description;

    private AiModelType modelType;

    private List<Long> tools;

}
