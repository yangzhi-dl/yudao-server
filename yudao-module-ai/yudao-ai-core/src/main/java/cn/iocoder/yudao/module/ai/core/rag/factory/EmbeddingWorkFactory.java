package cn.iocoder.yudao.module.ai.core.rag.factory;

import cn.iocoder.yudao.module.ai.core.rag.enums.EmbeddingWorkEnum;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EmbeddingWorkFactory {
    private final Map<EmbeddingWorkEnum, EmbeddingWorkService> services;

    public EmbeddingWorkFactory(List<EmbeddingWorkService> serviceList) {
        this.services = serviceList.stream()
                .collect(Collectors.toMap(
                        EmbeddingWorkService::getType,
                        Function.identity(),
                        (existing, _) -> existing
                ));
    }

    public EmbeddingWorkService getService(EmbeddingWorkEnum type) {
        return Optional.ofNullable(services.get(type))
                .orElseThrow(() -> new IllegalArgumentException("No service found for type: " + type));
    }
}