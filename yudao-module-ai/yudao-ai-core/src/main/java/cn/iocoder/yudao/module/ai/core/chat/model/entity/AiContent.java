package cn.iocoder.yudao.module.ai.core.chat.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiContent {

    private StringBuffer thinking;

    private StringBuffer content;

    private List<ChatTool> tools;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer index;

    /**
     * 思考是否已完成。
     * 在 ReAct Agent 多轮循环中，若该轮只产生 tool call 没有文本输出，
     * 工具全部返回后将此标记置为 true，前端据此展示"思考过程"而非"思考中"。
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean thinkingComplete;

    /**
     * 最终产物（如 PPT/PDF/图片等工作区生成文件）。
     * 由 {@code presentFiles} 工具返回后收集，格式为文件信息对象列表
     * {@code [{id, filename, fileSize, fileType, url}, ...]}，随增量下发给前端，
     * 用于在运行面板"最终产物"区域以文件卡片形式展示。
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Object> fileInfos;

}
