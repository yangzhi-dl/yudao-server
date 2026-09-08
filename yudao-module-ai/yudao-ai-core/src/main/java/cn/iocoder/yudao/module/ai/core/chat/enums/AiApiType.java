package cn.iocoder.yudao.module.ai.core.chat.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum AiApiType {
    AGENT(0),
    WORKFLOW(1),
    OPENAI(2),
    OLLAMA(3),
    DEEPSEEK(4),
    MULTI_AGENT(5);

    @EnumValue
    private final int value;

    AiApiType(int value) {
        this.value = value;
    }
}
