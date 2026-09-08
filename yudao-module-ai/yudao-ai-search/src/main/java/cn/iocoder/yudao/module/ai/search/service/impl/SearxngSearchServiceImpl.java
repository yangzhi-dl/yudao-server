package cn.iocoder.yudao.module.ai.search.service.impl;

import cn.iocoder.yudao.module.ai.search.model.SearchResponse;
import cn.iocoder.yudao.module.ai.search.properties.SearchServicesProperties;
import cn.iocoder.yudao.module.ai.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearxngSearchServiceImpl implements SearchService {
    
    private final WebClient.Builder webClientBuilder;
    private final SearchServicesProperties properties;
    
    private WebClient webClient;
    private SearchServicesProperties.SearchServiceConfig config;
    
    @Override
    public SearchResponse search(String query) {
        loadConfig();
        
        if (!isAvailable()) {
            log.error("Searxng service is not available");
            return null;
        }
        
        String engines = config.getDefaultEngines();
        String format = config.getFormat();
        
        log.info("Searxng搜索: {}, 引擎: {}, 格式: {}", query, engines, format);
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", query)
                        .queryParam("language", "zh-CN")
                        .queryParam("format", format)
                        .queryParam("engines", engines)
                        .build())
                .retrieve()
                .bodyToMono(SearchResponse.class)
                .doOnSuccess(response -> {
                    assert response != null;
                    log.info("Searxng搜索成功，结果数: {}",
                            response.getResults().size());
                })
                .doOnError(error -> log.error("Searxng搜索失败: {}", error.getMessage())).block();
    }
    
    private void loadConfig() {
        if (config == null) {
            config = properties.getServiceByType("searxng");
            if (config != null && config.getBaseUrl() != null) {
                this.webClient = webClientBuilder.baseUrl(config.getBaseUrl()).build();
            }
        }
    }
    
    @Override
    public String getType() {
        return "searxng";
    }
    
    @Override
    public boolean isAvailable() {
        loadConfig();
        return config != null && config.getEnabled() && webClient != null;
    }
}