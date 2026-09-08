package cn.iocoder.yudao.module.ai.core.chat.utils;

import cn.iocoder.yudao.module.ai.core.chat.model.entity.*;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MarkdownUtil {

    /**
     * 将 ChatTool 列表转换为 Markdown 表格
     * @param tools 工具列表
     * @return Markdown 表格字符串
     */
    public static String toolsToMarkdownTable(List<ChatTool> tools) {
        if (tools == null || tools.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("| ID | 类型 | 名称 | 参数 | 响应数据 |\n");
        sb.append("|----|------|------|------|----------|\n");

        for (ChatTool tool : tools) {
            sb.append("| ")
              .append(escapeMarkdown(tool.getId()))
              .append(" | ")
              .append(escapeMarkdown(tool.getType()))
              .append(" | ")
              .append(escapeMarkdown(tool.getName()))
              .append(" | ")
              .append(escapeMarkdown(tool.getArguments()))
              .append(" | ")
              .append(escapeMarkdown(tool.getResponseData()))
              .append(" |\n");
        }
        return sb.toString();
    }

    /**
     * 简单转义 Markdown 特殊字符（可根据需要扩展）
     */
    private static String escapeMarkdown(String text) {
        if (text == null) return "";
        // 转义管道符、换行符等
        return text.replace("|", "\\|")
                   .replace("\n", "<br>")
                   .replace("\r", "");
    }



    /**
     * 为单个topic生成markdown内容
     */
    public static String generateMarkdownForTopic(TopicRange topicRange, List<ExportChatDialogue> chatDialogues) {
        StringBuilder markdown = new StringBuilder();

        // 添加标题和元数据
        markdown.append("# ").append(topicRange.getTitle()).append("\n\n")
                .append("## 会话信息\n\n")
                .append("- **会话ID**: ").append(topicRange.getId()).append("\n")
                .append("- **创建时间**: ").append(formatDateTime(topicRange.getCreateTime())).append("\n")
                .append("- **消息数量**: ").append(chatDialogues.size()).append("\n\n")
                .append("---\n\n")
                .append("## 对话内容\n\n");

        // 处理每条对话
        for (ExportChatDialogue dialogue : chatDialogues) {
            // 用户消息
            if (dialogue.getUserContent() != null && dialogue.getUserContent().getQuestion() != null) {
                markdown.append("### 用户\n\n").append(dialogue.getUserContent().getQuestion()).append("\n\n");
            }

            // AI消息
            if (dialogue.getAiAgentMessage() != null) {
                appendAiAgentMessage(markdown, dialogue.getAiAgentMessage());
            } else if (dialogue.getAiContent() != null && !dialogue.getAiContent().isEmpty()) {
                appendAiContent(markdown, dialogue.getAiContent());
            }

            markdown.append("---\n\n");
        }

        return markdown.toString();
    }



    /**
     * 格式化日期时间
     */
    private static String formatDateTime(LocalDateTime date) {
        if (date == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return date.format(formatter);
    }

    /**
     * 格式化JSON字符串
     */
    private static String formatJson(String jsonStr) {
        if (jsonStr == null || jsonStr.isEmpty()) return "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            Object json = mapper.readValue(jsonStr, Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e) {
            return jsonStr;
        }
    }

    /**
     * 添加AI代理消息内容（按 agentContentList 顺序渲染，保持时间线交错）
     */
    private static void appendAiAgentMessage(StringBuilder markdown, AiAgentMessage aiMessage) {
        markdown.append("### 智能体\n\n");

        List<AiAgentContent> agentContentList = aiMessage.getAgentContentList();
        if (agentContentList != null && !agentContentList.isEmpty()) {
            for (AiAgentContent segment : agentContentList) {
                if (segment.getId() != null && segment.getId().startsWith("main_")) {
                    // 主智能体分段
                    if (segment.getAiContent() != null && !segment.getAiContent().isEmpty()) {
                        for (AiContent aiContent : segment.getAiContent()) {
                            appendSingleAiContent(markdown, aiContent);
                        }
                    }
                } else {
                    // 子智能体分段
                    markdown.append("#### 智能体: ").append(segment.getName()).append("\n\n");
                    if (segment.getCommand() != null) {
                        markdown.append("**命令**: ").append(segment.getCommand()).append("\n\n");
                    }
                    if (segment.getAiContent() != null && !segment.getAiContent().isEmpty()) {
                        for (AiContent aiContent : segment.getAiContent()) {
                            appendSingleAiContent(markdown, aiContent);
                        }
                    }
                }
            }
        }
    }

    /**
     * 添加单个AI内容（思考过程、工具调用等）
     */
    private static void appendSingleAiContent(StringBuilder markdown, AiContent aiContent) {
        // 思考过程
        if (aiContent.getThinking() != null && !aiContent.getThinking().isEmpty()) {
            markdown.append("#### 思考过程\n\n```\n").append(aiContent.getThinking()).append("\n```\n\n");
        }

        // 普通内容
        if (aiContent.getContent() != null && !aiContent.getContent().isEmpty()) {
            markdown.append(aiContent.getContent()).append("\n\n");
        }

        // 工具调用
        if (aiContent.getTools() != null && !aiContent.getTools().isEmpty()) {
            markdown.append("#### 工具调用\n\n");
            for (ChatTool tool : aiContent.getTools()) {
                markdown.append("- **工具名称**: ").append(tool.getName()).append("\n")
                        .append("- **工具类型**: ").append(tool.getType()).append("\n");

                if (tool.getArguments() != null && !tool.getArguments().isEmpty()) {
                    markdown.append("- **参数**: \n```json\n").append(formatJson(tool.getArguments())).append("\n```\n");
                }
                if (tool.getResponseData() != null && !tool.getResponseData().isEmpty()) {
                    markdown.append("- **返回结果**: \n```json\n").append(formatJson(tool.getResponseData())).append("\n```\n");
                }
                markdown.append("\n");
            }
        }
    }

    /**
     * 添加AI内容列表
     */
    private static void appendAiContent(StringBuilder markdown, List<AiContent> aiContents) {
        markdown.append("### AI助手\n\n");
        for (AiContent aiContent : aiContents) {
            appendSingleAiContent(markdown, aiContent);
        }
    }

}