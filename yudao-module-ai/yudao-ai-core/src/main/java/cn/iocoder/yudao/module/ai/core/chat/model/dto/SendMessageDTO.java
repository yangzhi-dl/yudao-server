package cn.iocoder.yudao.module.ai.core.chat.model.dto;

import cn.iocoder.yudao.module.ai.core.chat.enums.AiApiType;
import cn.iocoder.yudao.module.ai.core.chat.enums.AiModelType;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: Aitenry
 * @Date: 2023/01/22 00:00
 * @Version: v1.0.0
 * @Description: TODO
 **/
@Data
public class SendMessageDTO implements Serializable {

    private Long topicId;

    private Long lastId;

    private Long endpointId;

    private AiApiType apiType;

    private AiModelType modelType;

    private List<Long> wikiIds;

    private List<Long> fileIds;

    private String question;

    private List<Long> selectedTools;

}
