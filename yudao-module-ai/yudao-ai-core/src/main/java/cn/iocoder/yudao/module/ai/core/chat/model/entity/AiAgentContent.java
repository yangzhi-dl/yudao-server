package cn.iocoder.yudao.module.ai.core.chat.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAgentContent {
    
    // 代理ID
    private String id;

    // 代理名称
    private String name;

    // 代理命令
    private String command;

    private Boolean isComplete;

    private List<AiContent> aiContent;

}
