package cn.iocoder.yudao.module.ai.core.chat.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum ToolType {

    SYSTEM_TOOL(0),

    MCP_TOOL(1),

    HTTP_DYNAMIC_TOOL(2),

    DATABASE_TOOL(3);

    @EnumValue
    private final int value;

    ToolType(int value) {
        this.value = value;
    }
}
