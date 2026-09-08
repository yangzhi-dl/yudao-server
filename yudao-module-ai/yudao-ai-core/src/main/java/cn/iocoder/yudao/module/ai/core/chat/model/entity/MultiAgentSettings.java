package cn.iocoder.yudao.module.ai.core.chat.model.entity;

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
public class MultiAgentSettings {

    private String name;

    /**
     * 子智能体英文名（当前集群内唯一）
     */
    private String enName;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long modelId;

    private String description;

    private String prompt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private List<Long> skillIds;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private List<Long> tools;

}
