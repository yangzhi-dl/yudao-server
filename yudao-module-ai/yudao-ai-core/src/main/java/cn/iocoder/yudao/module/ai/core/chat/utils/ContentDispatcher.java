package cn.iocoder.yudao.module.ai.core.chat.utils;

import cn.iocoder.yudao.module.ai.core.chat.model.entity.ContextType;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import java.util.function.Consumer;

public class ContentDispatcher {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public void dispatchContent(String content, ContextType context) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }
        
        // 按优先级尝试不同的类型
        if (tryParseAndSet(content, new TypeReference<>() {}, context::setAiContent)) {
            return;
        }
        
        if (tryParseAndSet(content, new TypeReference<>() {}, context::setAiAgentMessage)) {
            return;
        }
    }
    
    private <T> boolean tryParseAndSet(String content, TypeReference<T> typeRef, 
                                        Consumer<T> setter) {
        try {
            T parsed = objectMapper.readValue(content, typeRef);
            if (parsed != null) {
                setter.accept(parsed);
                return true;
            }
        } catch (Exception e) {
            // 解析失败，继续尝试下一个
        }
        return false;
    }
}