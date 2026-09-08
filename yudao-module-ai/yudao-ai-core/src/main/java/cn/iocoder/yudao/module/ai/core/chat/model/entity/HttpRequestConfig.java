package cn.iocoder.yudao.module.ai.core.chat.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * HTTP请求配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpRequestConfig {
    
    /**
     * 基础URL
     */
    private String baseUrl;
    
    /**
     * 请求路径
     */
    private String path;
    
    /**
     * 请求方法：GET, POST, PUT, DELETE, PATCH
     */
    private String method;
    
    /**
     * 请求头
     */
    private Map<String, String> headers;
    
    /**
     * 超时时间（毫秒）
     */
    @Builder.Default
    private Integer timeout = 30000;
    
    /**
     * 内容类型
     */
    @Builder.Default
    private String contentType = "application/json";
    
    /**
     * 字符编码
     */
    @Builder.Default
    private String charset = "UTF-8";
}