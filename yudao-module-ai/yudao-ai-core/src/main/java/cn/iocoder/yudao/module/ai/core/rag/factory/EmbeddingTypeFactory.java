package cn.iocoder.yudao.module.ai.core.rag.factory;

import cn.iocoder.yudao.module.ai.core.chat.enums.EmbeddingTypeEnum;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EmbeddingTypeFactory {

    private final Map<EmbeddingTypeEnum, EmbeddingTypeService> services;

    public EmbeddingTypeFactory(List<EmbeddingTypeService> serviceList) {
        this.services = serviceList.stream()
                .collect(Collectors.toMap(
                        EmbeddingTypeService::getType,
                        Function.identity(),
                        (existing, _) -> existing
                ));
    }

    public EmbeddingTypeService getService(EmbeddingTypeEnum type) {
        return Optional.ofNullable(services.get(type))
                .orElseThrow(() -> new IllegalArgumentException("No service found for type: " + type));
    }

}
