package cn.iocoder.yudao.module.ai.search.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Component
@ConfigurationProperties(prefix = "iims.search")
public class SearchServicesProperties {
    
    private Map<String, SearchServiceConfig> services;
    
    @Data
    public static class SearchServiceConfig {
        private String type;  // searxng, baidu, tavily
        private String baseUrl;
        private String format;
        private String defaultEngines;
        private String apiKey;
        private String apiSecret;
        private Boolean enabled = true;
        private Integer priority = 999;
        private Integer maxResults = 20;
        private Map<String, Object> extra;  // 额外配置
    }
    
    /**
     * 获取启用的服务列表（按优先级排序）
     */
    public Map<String, SearchServiceConfig> getEnabledServices() {
        return services.entrySet().stream()
                .filter(entry -> entry.getValue().getEnabled())
                .collect(Collectors.toMap(
                    Map.Entry::getKey, 
                    Map.Entry::getValue
                ));
    }
    
    /**
     * 根据类型获取服务配置
     */
    public SearchServiceConfig getServiceByType(String type) {
        return services.values().stream()
                .filter(config -> config.getType().equals(type))
                .findFirst()
                .orElse(null);
    }
}