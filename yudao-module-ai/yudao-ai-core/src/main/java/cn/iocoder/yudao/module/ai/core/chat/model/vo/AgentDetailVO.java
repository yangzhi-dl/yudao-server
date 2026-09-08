package cn.iocoder.yudao.module.ai.core.chat.model.vo;

import cn.iocoder.yudao.module.ai.core.chat.enums.AgentType;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.MultiAgentSettings;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentDetailVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long modelId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long categoryId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private List<Long> tagIds;

    private String name;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long cover;

    private String imageUrl;

    private AgentType type;

    private String prompt;

    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private List<MultiAgentSettings> settings;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private List<Long> skillIds;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private List<Long> tools;

}
