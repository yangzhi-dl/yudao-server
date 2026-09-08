package cn.iocoder.yudao.module.ai.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Dzint认证配置属性
 */
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "dzint.auth")
public class DzintAuthProperties {
    
    /**
     * 认证URI
     */
    private String uri;
    
    /**
     * 客户端配置
     */
    private Client client = new Client();

    @Setter
    @Getter
    public static class Client {
        /**
         * 客户端ID
         */
        private String id;
        
        /**
         * 密钥
         */
        private String secret;
        
        /**
         * 分隔符
         */
        private String separator = "#";

    }
}