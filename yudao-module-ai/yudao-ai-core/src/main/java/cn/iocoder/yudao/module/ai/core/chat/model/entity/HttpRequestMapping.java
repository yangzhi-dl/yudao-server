package cn.iocoder.yudao.module.ai.core.chat.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * HTTP请求映射配置
 * 定义如何将工具参数映射到HTTP请求的各个部分
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpRequestMapping {
    
    /**
     * 路径参数映射
     * 例如: {"creator": "${creator}"}
     */
    private Map<String, String> pathParams;
    
    /**
     * 查询参数映射
     * 例如: {"q": "${city}", "page": "${pageNo}"}
     */
    private Map<String, String> queryParams;
    
    /**
     * 请求体映射
     * 支持JSON格式的请求体映射
     */
    private Map<String, Object> body;
    
    /**
     * 表单参数映射（application/x-www-form-urlencoded）
     */
    private Map<String, Object> formParams;
    
    /**
     * 是否将参数直接作为请求体（适用于整个对象作为请求体）
     */
    private Boolean useFullParamsAsBody;
    
    /**
     * 参数前置处理（支持SpEL表达式）
     */
    private Map<String, String> preProcessors;
}