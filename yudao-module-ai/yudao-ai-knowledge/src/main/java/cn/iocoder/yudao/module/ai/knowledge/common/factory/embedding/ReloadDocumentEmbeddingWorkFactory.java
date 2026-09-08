package cn.iocoder.yudao.module.ai.knowledge.common.factory.embedding;

import cn.iocoder.yudao.module.ai.core.rag.enums.EmbeddingWorkEnum;
import cn.iocoder.yudao.module.ai.core.rag.factory.EmbeddingWorkService;
import cn.iocoder.yudao.module.ai.core.rag.service.CustomizeVectorStoreService;
import cn.iocoder.yudao.module.ai.core.rag.utils.MarkdownSplitter;
import cn.iocoder.yudao.module.ai.knowledge.common.utils.UmoDocConverter;
import cn.iocoder.yudao.module.ai.knowledge.document.model.vo.FindDocumentDetailVO;
import cn.iocoder.yudao.module.ai.knowledge.document.service.DocumentService;
import cn.iocoder.yudao.module.ai.knowledge.wiki.dal.dataobject.WikiCatalog;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.entity.WikiSettings;
import cn.iocoder.yudao.module.ai.knowledge.wiki.service.WikiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.milvus.MilvusFilterExpressionConverter;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class ReloadDocumentEmbeddingWorkFactory implements EmbeddingWorkService {

    /**
     * 每次入库的批次大小
     */
    private static final int STORE_BATCH_SIZE = 10;

    private final WikiService wikiService;
    private final DocumentService documentService;
    private final CustomizeVectorStoreService storeService;
    private final EmbeddingProcessingRegistry processingRegistry;

    private ReloadDocumentEmbeddingWorkFactory(@Lazy WikiService wikiService,
                                               @Lazy DocumentService documentService,
                                               CustomizeVectorStoreService storeService,
                                               EmbeddingProcessingRegistry processingRegistry) {
        this.wikiService = wikiService;
        this.documentService = documentService;
        this.storeService = storeService;
        this.processingRegistry = processingRegistry;
    }

    @Override
    public EmbeddingWorkEnum getType() {
        return EmbeddingWorkEnum.RELOAD_DOCUMENT;
    }

    @Override
    public Boolean process(Long documentId) {
        WikiCatalog wikiCatalog = wikiService.findWikiCatalogByDocumentId(documentId);
        if (Objects.isNull(wikiCatalog) || Objects.isNull(wikiCatalog.getDocumentId())) {
            return true;
        }
        return doProcess(wikiCatalog);
    }

    private Boolean doProcess(WikiCatalog wikiCatalog) {
        Long catalogId = wikiCatalog.getId();
        Long documentId = wikiCatalog.getDocumentId();
        Long wikiId = wikiCatalog.getWikiId();

        if (!processingRegistry.tryStart(documentId)) {
            log.info("文档正在向量化，跳过重复任务，documentId={}", documentId);
            return true;
        }

        boolean isSuccess = true;
        FindDocumentDetailVO articleDetail = documentService.findDocumentDetail(documentId);

        try {
            WikiSettings wikiSettings = wikiService.getWikiSettings(wikiId);
            String documentIdStr = String.valueOf(documentId);
            String wikiIdStr = String.valueOf(wikiId);

            wikiService.enableEmbedding(Collections.singletonList(catalogId));

            String content = articleDetail.getContent();

            List<Document> documents;
            if (StringUtils.isBlank(content)) {
                documents = new ArrayList<>();
            } else {
                documents = MarkdownSplitter.splitByHeadersWithHierarchy(
                        UmoDocConverter.convertToMarkdown(content), Map.of("wiki_id", wikiIdStr, "document_id", documentIdStr),
                        wikiSettings.getChunkSize(), wikiSettings.getOverlapSize());
            }

            VectorStore vectorStore = storeService.loadMilvusVectorStore();
            // 清空文档 chunk_keys
            documentService.updateChunkKeys(documentId, new ArrayList<>());
            deleteDocument(documentIdStr);

            // 如果内容为空，直接结束
            if (documents.isEmpty()) {
                wikiService.enableEmbedding(Collections.singletonList(catalogId));
                return true;
            }

            // 重新构建向量数据
            List<String> newChunkKeys = documents.stream()
                    .map(doc -> doc.getMetadata().get("chunk_key").toString())
                    .toList();

            List<Document> batch = new ArrayList<>(STORE_BATCH_SIZE);
            for (Document doc : documents) {
                batch.add(doc);
                if (batch.size() >= STORE_BATCH_SIZE) {
                    if (flushBatchFailed(vectorStore, batch)) {
                        isSuccess = false;
                        break;
                    }
                    batch.clear();
                }
            }
            if (isSuccess && !batch.isEmpty()) {
                if (flushBatchFailed(vectorStore, batch)) {
                    isSuccess = false;
                }
            }
            batch.clear();
            documents.clear();

            // 5. 更新 chunk_keys 与向量化状态
            if (isSuccess) {
                documentService.updateChunkKeys(documentId, newChunkKeys);
            }
            wikiService.enableEmbedding(Collections.singletonList(catalogId));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            isSuccess = false;
            wikiService.enableEmbedding(Collections.singletonList(catalogId));
        } finally {
            processingRegistry.finish(documentId);
        }

        return isSuccess;
    }

    private void deleteDocument(String documentIdStr) {
        FilterExpressionBuilder filter = new FilterExpressionBuilder();
        MilvusFilterExpressionConverter converter = new MilvusFilterExpressionConverter();
        Filter.Expression build = filter.eq("document_id", documentIdStr).build();
        storeService.deleteMilvus(converter.convertExpression(build));
    }

    private boolean flushBatchFailed(VectorStore vectorStore, List<Document> batch) {
        try {
            vectorStore.add(batch);
            return false;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return true;
        }
    }

}
