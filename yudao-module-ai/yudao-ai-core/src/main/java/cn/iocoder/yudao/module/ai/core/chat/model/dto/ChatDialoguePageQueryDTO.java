package cn.iocoder.yudao.module.ai.core.chat.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: Aitenry
 * @Date: 2023/01/22 00:00
 * @Version: v1.0.0
 * @Description: TODO
 **/
@Data
public class ChatDialoguePageQueryDTO implements Serializable {

    private Long topicId;

    private int page;

    private int pageSize;

}
