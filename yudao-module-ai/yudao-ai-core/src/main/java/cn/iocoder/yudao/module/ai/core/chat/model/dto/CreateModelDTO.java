package cn.iocoder.yudao.module.ai.core.chat.model.dto;

import cn.iocoder.yudao.module.ai.core.chat.enums.AiApiType;
import cn.iocoder.yudao.module.ai.core.chat.enums.AiModelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateModelDTO {

    private String name;

    private String rename;

    private AiApiType type;

    private AiModelType modelType;

    private String url;

    private String key;

    private Long token;

    private String description;

}
