package cn.iocoder.yudao.module.ai.search.service;

import cn.iocoder.yudao.module.ai.search.properties.SearchServicesProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceManager {

    private final List<SearchService> searchServices;
    private final SearchServicesProperties properties;

    /**
     * 获取所有可用的搜索服务
     */
    public Flux<SearchService> getAllAvailableServices(String query) {
        Map<String, SearchServicesProperties.SearchServiceConfig> enabledServices =
                properties.getEnabledServices();

        return Flux.fromIterable(searchServices)
                .filter(SearchService::isAvailable)
                .filter(service -> enabledServices.containsKey(service.getType()));
    }

    /**
     * 获取指定类型的可用搜索服务
     */
    public SearchService getServiceByType(String type) {
        return searchServices.stream()
                .filter(service -> service.getType().equals(type) && service.isAvailable())
                .findFirst()
                .orElseThrow(() -> {
                    log.error("No available search service found for type: {}", type);
                    return new IllegalStateException("No available search service found for type: " + type);
                });
    }

    /**
     * 获取优先级最高的可用搜索服务
     */
    public SearchService getHighestPriorityService() {
        return properties.getEnabledServices().entrySet().stream()
                .min(Map.Entry.comparingByValue(
                        Comparator.comparing(SearchServicesProperties.SearchServiceConfig::getPriority)
                ))
                .map(entry -> getServiceByType(entry.getKey()))
                .orElseThrow(() -> new IllegalStateException("No available search service found"));
    }
}