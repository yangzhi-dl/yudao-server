package cn.iocoder.yudao.module.ai.core.chat.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum AiModelType {

    EMBEDDING(0),

    LANGUAGE(1),

    VISION(2),

    MULTIMODAL(3);

    @EnumValue
    private final int value;

    AiModelType(int value) {
        this.value = value;
    }
}
