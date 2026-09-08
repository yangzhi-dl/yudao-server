package cn.iocoder.yudao.module.ai.core.chat.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * HTTP认证配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpAuthConfig {
    
    /**
     * 认证类型：none, basic, bearer, apiKey, oauth2
     */
    @Builder.Default
    private String type = "none";
    
    /**
     * Basic认证用户名
     */
    private String username;
    
    /**
     * Basic认证密码
     */
    private String password;
    
    /**
     * Bearer Token
     */
    private String bearerToken;
    
    /**
     * API Key位置：header, query
     */
    private String apiKeyLocation;
    
    /**
     * API Key名称
     */
    private String apiKeyName;
    
    /**
     * API Key值
     */
    private String apiKeyValue;
    
    /**
     * OAuth2配置
     */
    private OAuth2Config oauth2Config;
    
    /**
     * OAuth2配置内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OAuth2Config {
        private String tokenUrl;
        private String clientId;
        private String clientSecret;
        private String scope;
    }
}