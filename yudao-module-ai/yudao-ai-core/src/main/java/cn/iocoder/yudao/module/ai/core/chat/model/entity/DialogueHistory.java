package cn.iocoder.yudao.module.ai.core.chat.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialogueHistory {

    private List<Message> messages;

    private List<Long> fileIds;

}
