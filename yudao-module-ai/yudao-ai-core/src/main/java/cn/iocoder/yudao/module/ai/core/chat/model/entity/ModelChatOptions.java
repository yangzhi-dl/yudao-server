package cn.iocoder.yudao.module.ai.core.chat.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: Aitenry
 * @Date: 2023/01/22 00:00
 * @Version: v1.0.0
 * @Description: TODO
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModelChatOptions {

    private Double frequencyPenalty;

    private Integer maxTokens;

    private Double presencePenalty;

    private List<String> stopSequences;

    private Double temperature;

    private Integer topK;

    private Double topP;
}
