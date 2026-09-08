package cn.iocoder.yudao.module.ai.core.chat.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 切换重新生成版本请求 DTO
 *
 * @author yudao
 */
@Data
public class RegenerateSwitchDTO implements Serializable {

    /**
     * assistant 对话 ID
     */
    private Long dialogueId;

    /**
     * 目标版本 ID（ai_chat_regenerate 行 id）
     */
    private Long versionId;

}
