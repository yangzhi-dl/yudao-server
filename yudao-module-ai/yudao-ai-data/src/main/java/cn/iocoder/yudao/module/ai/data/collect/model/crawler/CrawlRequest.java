package cn.iocoder.yudao.module.ai.data.collect.model.crawler;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 爬虫服务请求体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlRequest {

    private List<String> urls;

    /**
     * 浏览器配置，直接透传为扁平 JSON（无 type/params 包装）。
     */
    @JsonProperty("browser_config")
    private Map<String, Object> browserConfig;

    @JsonProperty("crawler_config")
    private Map<String, Object> crawlerConfig;

}
