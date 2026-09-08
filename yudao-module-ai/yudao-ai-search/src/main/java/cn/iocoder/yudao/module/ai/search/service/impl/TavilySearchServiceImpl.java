package cn.iocoder.yudao.module.ai.search.service.impl;

import cn.iocoder.yudao.module.ai.search.model.SearchResponse;
import cn.iocoder.yudao.module.ai.search.properties.SearchServicesProperties;
import cn.iocoder.yudao.module.ai.search.service.SearchService;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class TavilySearchServiceImpl implements SearchService {
    
    private final WebClient.Builder webClientBuilder;
    private final SearchServicesProperties properties;
    
    private WebClient webClient;
    private SearchServicesProperties.SearchServiceConfig config;
    
    @Override
    public SearchResponse search(String query) {
        loadConfig();
        
        if (!isAvailable()) {
            log.error("Tavily search service is not available");
            return null;
        }
        
        log.info("Tavily搜索: {}", query);
        
        // Tavily API请求体
        TavilyRequest request = TavilyRequest.builder()
                .query(query).searchDepth("advanced")
                .build();
        
        return webClient.post()
                .uri("/search")
                .header("Authorization", String.format("Bearer %s", config.getApiKey()))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(SearchResponse.class)
                .doOnSuccess(response -> {
                    assert response != null;
                    log.info("Tavily搜索成功，结果数: {}",
                            response.getResults().size());
                })
                .doOnError(error -> log.error("Tavily搜索失败: {}", error.getMessage())).block();
    }

    private void loadConfig() {
        if (config == null) {
            config = properties.getServiceByType("tavily");
            if (config != null && config.getBaseUrl() != null) {
                HttpClient httpClient = HttpClient.create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)  // 连接超时10秒
                        .responseTimeout(Duration.ofSeconds(30))  // 响应超时30秒
                        .doOnConnected(conn ->
                                conn.addHandlerLast(new ReadTimeoutHandler(30))
                                        .addHandlerLast(new WriteTimeoutHandler(10))
                        );

                this.webClient = webClientBuilder
                        .clientConnector(new ReactorClientHttpConnector(httpClient))
                        .baseUrl(config.getBaseUrl())
                        .build();
            }
        }
    }
    
    @Override
    public String getType() {
        return "tavily";
    }
    
    @Override
    public boolean isAvailable() {
        loadConfig();
        return config != null && config.getEnabled() && webClient != null;
    }
    
    @lombok.Data
    @lombok.Builder
    private static class TavilyRequest {

        private String query;

        @JsonProperty("search_depth")
        private String searchDepth;

    }
}