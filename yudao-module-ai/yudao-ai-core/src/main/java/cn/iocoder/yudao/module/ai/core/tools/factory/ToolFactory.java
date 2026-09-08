package cn.iocoder.yudao.module.ai.core.tools.factory;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class ToolFactory {

    private final List<AITool> availableTools;

    public ToolFactory(List<AITool> tools) {
        // 过滤掉已禁用的工具，后续所有查询方法均只包含启用状态的工具
        this.availableTools = tools.stream()
                .filter(AITool::isEnabled)
                .collect(Collectors.toList());
    }

    public List<Object> getEnabledToolInstances() {
        return availableTools.stream()
                .map(AITool::getToolInstance)
                .collect(Collectors.toList());
    }

    public List<Object> getToolInstancesByIds(List<String> enabledToolIds) {
        if (CollectionUtils.isEmpty(enabledToolIds)) {
            return Collections.emptyList();
        }

        return availableTools.stream()
                .filter(tool -> enabledToolIds.contains(tool.getName())) // 匹配ID
                .map(AITool::getToolInstance) // 获取工具实例
                .collect(Collectors.toList());
    }

    public List<AITool> getAllTools() {
        return availableTools;
    }

    public Integer getToolTotal() {
        return availableTools.size();
    }

    public List<String> getAvailableToolNames() {
        return availableTools.stream()
                .map(AITool::getName)
                .collect(Collectors.toList());
    }

}