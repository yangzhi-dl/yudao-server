package cn.iocoder.yudao.module.ai.core.chat.utils;

import cn.iocoder.yudao.module.ai.core.chat.model.entity.ToolFunction;
import cn.iocoder.yudao.module.ai.core.tools.factory.AITool;
import cn.iocoder.yudao.module.ai.core.tools.factory.ToolFactory;
import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ChatToolUtil {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ToolFactory toolFactory;

    public ChatToolUtil(ToolFactory toolFactory) {
        this.toolFactory = toolFactory;
    }

    public String generatePrompt(List<Object> toolInstances) {
        List<Map<String, Object>> toolsList = new ArrayList<>();
        StringBuilder prompt = new StringBuilder();
        for (Object toolInstance : toolInstances) {
            Class<?> clazz = toolInstance.getClass();
            String toolName = getToolNameFromInstance(toolInstance); // 使用工具名称

            // 收集该工具的所有方法信息
            List<Map<String, Object>> methods = Arrays.stream(clazz.getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(Tool.class))
                    .map(this::extractMethodInfo)
                    .toList();

            if (!methods.isEmpty()) {
                Map<String, Object> toolInfo = new HashMap<>();
                toolInfo.put("name", toolName);
                toolInfo.put("className", clazz.getSimpleName());
                toolInfo.put("methods", methods);
                toolsList.add(toolInfo);
            }
        }
        // 将工具信息转换为JSON格式并添加到提示词中
        try {
            String toolsJson = objectMapper.writeValueAsString(toolsList);
            prompt.append(toolsJson).append("\n\n");
        } catch (Exception e) {
            log.warn("工具信息JSON序列化失败，使用文本格式: {}", e.getMessage());
            // 如果JSON序列化失败，使用文本格式
            for (Map<String, Object> tool : toolsList) {
                prompt.append(String.format("工具名: %s (%s)\n",
                        tool.get("name"), tool.get("className")));
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> methods = (List<Map<String, Object>>) tool.get("methods");
                for (Map<String, Object> method : methods) {
                    prompt.append(String.format("  - %s: %s\n",
                            method.get("name"), method.get("description")));
                }
            }
        }

        return prompt.toString();
    }

    // 从实例获取工具名称
    private String getToolNameFromInstance(Object toolInstance) {
        // 查找对应的AITool实现
        for (AITool tool : toolFactory.getAllTools()) {
            if (tool.getToolInstance() == toolInstance) {
                return tool.getName();
            }
        }
        // 如果找不到对应的AITool，使用类名
        return toolInstance.getClass().getSimpleName();
    }

    // 提取方法信息
    public Map<String, Object> extractMethodInfo(Method method) {
        Tool toolAnnotation = method.getAnnotation(Tool.class);

        Map<String, Object> methodInfo = new HashMap<>();
        methodInfo.put("name", method.getName());
        methodInfo.put("description", toolAnnotation.description());

        // 参数信息
        List<Map<String, Object>> parameters = Arrays.stream(method.getParameters())
                .filter(param -> param.isAnnotationPresent(ToolParam.class))
                .map(param -> {
                    ToolParam toolParam = param.getAnnotation(ToolParam.class);
                    Map<String, Object> paramInfo = new HashMap<>();
                    paramInfo.put("name", param.getName());
                    paramInfo.put("description", toolParam.description());
                    paramInfo.put("type", getParameterType(param.getType()));
                    paramInfo.put("required", toolParam.required());
                    return paramInfo;
                })
                .collect(Collectors.toList());

        methodInfo.put("parameters", parameters);
        methodInfo.put("returnType", method.getReturnType().getSimpleName());

        return methodInfo;
    }

    public ToolFunction extractToolFunctionFromMethod(Method method) {
        Tool toolAnnotation = method.getAnnotation(Tool.class);
        return ToolFunction.builder()
                .name(method.getName())
                .description(toolAnnotation.description())
                .build();
    }

    // 获取参数类型
    private String getParameterType(Class<?> type) {
        if (type == int.class || type == Integer.class ||
                type == long.class || type == Long.class ||
                type == short.class || type == Short.class ||
                type == byte.class || type == Byte.class) {
            return "integer";
        } else if (type == float.class || type == Float.class ||
                type == double.class || type == Double.class) {
            return "number";
        } else if (type == boolean.class || type == Boolean.class) {
            return "boolean";
        } else {
            return "string";
        }
    }

}
