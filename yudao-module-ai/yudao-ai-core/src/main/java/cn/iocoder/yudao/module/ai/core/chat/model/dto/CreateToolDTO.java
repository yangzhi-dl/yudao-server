package cn.iocoder.yudao.module.ai.core.chat.model.dto;

import cn.iocoder.yudao.module.ai.core.chat.enums.ToolType;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ToolFunction;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ToolSettings;
import lombok.Data;

import java.util.List;

@Data
public class CreateToolDTO {

    private String name;

    private String rename;

    private String description;

    private ToolType type;

    private ToolSettings settings;

    private List<ToolFunction> functions;

}
