package cn.iocoder.yudao.module.ai.core.chat.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChatDialogueStarPageQueryDTO implements Serializable {

    private int page;

    private int pageSize;

}
