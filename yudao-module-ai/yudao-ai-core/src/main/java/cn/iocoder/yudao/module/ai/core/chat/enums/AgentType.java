package cn.iocoder.yudao.module.ai.core.chat.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum AgentType {

    REACT_AGENT(0),

    MULTI_AGENT(1);

    @EnumValue
    private final int value;

    AgentType(int value) {
        this.value = value;
    }
}
