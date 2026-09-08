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
public class ToolSettings {

    private ToolStyle style;

    private McpConfig mcpConfig;

    private List<HttpToolConfig> httpConfigs;

    private List<DatabaseToolConfig> databaseConfigs;

}
