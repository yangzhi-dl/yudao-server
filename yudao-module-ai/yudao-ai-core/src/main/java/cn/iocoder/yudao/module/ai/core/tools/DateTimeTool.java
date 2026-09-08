package cn.iocoder.yudao.module.ai.core.tools;

import cn.iocoder.yudao.module.ai.core.tools.factory.AITool;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class DateTimeTool implements AITool {

    @Override
    public Object getToolInstance() {
        return this;
    }

    @Override
    public String getName() {
        return "datetime-tools";
    }

    @Override
    public String getTitle() {
        return "系统时间工具箱";
    }

    @Override
    public String getDescription() {
        return "可以获取时间戳来得到不同格式的日期时间";
    }

    @Tool(description = "获取当前时间戳")
    public String getCurrentTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }

    @Tool(description = "获取当前时间")
    public String getCurrentTime() {
        return LocalDateTime.now().toString();
    }

    @Tool(description = "格式化指定时间")
    public String formatDateTime(@ToolParam(description = "时间戳") long timestamp,
                                @ToolParam(description = "格式化模式，如yyyy-MM-dd HH:mm:ss") String pattern) {
        try {
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
            return dateTime.format(DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            return "时间格式错误: " + e.getMessage();
        }
    }
}