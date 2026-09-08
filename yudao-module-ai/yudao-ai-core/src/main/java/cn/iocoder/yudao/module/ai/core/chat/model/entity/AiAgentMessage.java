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
public class AiAgentMessage {

    /**
     * 有序的智能体内容片段列表，按时间顺序排列。
     * 每个 AiAgentContent 代表主智能体或子智能体的一个输出段落：
     * - 主智能体分段：id 格式为 "main_0", "main_1"...，aiContent 包含思考、文本、工具调用
     * - 子智能体分段：id 为子智能体标识，含 command 指令和 aiContent 输出
     * 列表顺序即前端渲染顺序，确保主智能体与子智能体的输出按时间线交错显示。
     */
    private List<AiAgentContent> agentContentList;

}
