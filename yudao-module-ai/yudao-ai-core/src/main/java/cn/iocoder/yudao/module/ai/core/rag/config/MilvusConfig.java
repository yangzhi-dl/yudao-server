package cn.iocoder.yudao.module.ai.core.rag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "iims.vector")
public class MilvusConfig {
    private String host = "localhost";
    private int port = 19530;
    private long connectTimeout = 60;  // 秒
    private long keepAliveTime = 45;   // 秒，保活探测间隔
    private long keepAliveTimeout = 15; // 秒
    private long idleTimeout = 30;     // 天，空闲超时
    private String database = "default";
    private String collection = "wiki";
    private boolean secure = false;
    private String token;
}