package cn.iocoder.yudao.module.ai.knowledge.common.factory.embedding;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EmbeddingProcessingRegistry {

    private final Set<Long> processingDocumentIds = ConcurrentHashMap.newKeySet();

    public boolean tryStart(Long documentId) {
        return processingDocumentIds.add(documentId);
    }

    public void finish(Long documentId) {
        processingDocumentIds.remove(documentId);
    }
}