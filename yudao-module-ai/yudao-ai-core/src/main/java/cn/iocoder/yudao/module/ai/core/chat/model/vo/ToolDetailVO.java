package cn.iocoder.yudao.module.ai.core.chat.model.vo;

import cn.iocoder.yudao.module.ai.core.chat.enums.ToolType;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ToolFunction;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ToolSettings;
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
public class ToolDetailVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JSONField(serializeUsing = ToStringSerializer.class)
    private Long id;

    private String name;

    private String rename;

    private String description;

    private ToolType type;

    private ToolSettings settings;

    private List<ToolFunction> functions;

}
