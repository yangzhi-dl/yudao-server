package cn.iocoder.yudao.module.ai.core.chat.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * HTTP工具配置
 * @Author: Aitenry
 * @Date: 2026/01/22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpToolConfig {

    private String name;

    private String description;
    
    /**
     * 请求基础配置
     */
    private HttpRequestConfig requestConfig;
    
    /**
     * 参数定义列表
     */
    private List<HttpParameterDef> parameters;
    
    /**
     * 请求映射配置
     */
    private HttpRequestMapping requestMapping;
    
    /**
     * 认证配置
     */
    private HttpAuthConfig authConfig;
}