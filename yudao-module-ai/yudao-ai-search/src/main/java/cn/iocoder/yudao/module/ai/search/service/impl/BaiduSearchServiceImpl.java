package cn.iocoder.yudao.module.ai.search.service.impl;

import cn.iocoder.yudao.module.ai.search.model.BaiduSearchResponse;
import cn.iocoder.yudao.module.ai.search.model.SearchResponse;
import cn.iocoder.yudao.module.ai.search.properties.SearchServicesProperties;
import cn.iocoder.yudao.module.ai.search.service.SearchService;
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
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BaiduSearchServiceImpl implements SearchService {

    private final WebClient.Builder webClientBuilder;
    private final SearchServicesProperties properties;

    private WebClient webClient;
    private SearchServicesProperties.SearchServiceConfig config;

    @Override
    public SearchResponse search(String query) {
        loadConfig();

        if (!isAvailable()) {
            log.error("Baidu search service is not available");
            return null;
        }

        log.info("百度搜索: {}", query);

        Map<String, Object> request = Map.of("messages", List.of(
                Map.of("role", "user", "content", query)
        ));

        // 根据百度API的实际接口调整
        BaiduSearchResponse searchResponse = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/ai_search/web_search")
                        .build())
                .header("Authorization", String.format("Bearer %s", config.getApiKey()))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(BaiduSearchResponse.class)
                .doOnSuccess(response -> {
                    assert response != null;
                    log.info("百度搜索成功，结果数: {}",
                            response.getReferences().size());
                })
                .doOnError(error -> log.error("百度搜索失败: {}", error.getMessage())).block();

        assert searchResponse != null;
        return SearchResponse.builder().results(searchResponse.getReferences()).query(query).build();
    }

    private void loadConfig() {
        if (config == null) {
            config = properties.getServiceByType("baidu");
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
        return "baidu";
    }

    @Override
    public boolean isAvailable() {
        loadConfig();
        return config != null && config.getEnabled() && webClient != null;
    }

}
