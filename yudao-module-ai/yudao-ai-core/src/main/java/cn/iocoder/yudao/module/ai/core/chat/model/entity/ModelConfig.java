package cn.iocoder.yudao.module.ai.core.chat.model.entity;

import cn.iocoder.yudao.module.ai.core.chat.enums.AiModelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelConfig {

    private Long id;

    private AiModelType type;

}
