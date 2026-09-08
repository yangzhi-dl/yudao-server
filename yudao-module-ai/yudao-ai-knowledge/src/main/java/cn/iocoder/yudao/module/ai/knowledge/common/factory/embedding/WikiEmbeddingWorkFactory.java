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
public class WikiEmbeddingWorkFactory implements EmbeddingWorkService {

    /**
     * 每次入库的批次大小，分批入库避免所有 Document 同时驻留内存
     */
    private static final int STORE_BATCH_SIZE = 10;

    private final DocumentService documentService;
    private final WikiService wikiService;
    private final CustomizeVectorStoreService storeService;
    private final EmbeddingProcessingRegistry processingRegistry;

    private WikiEmbeddingWorkFactory(@Lazy DocumentService documentService, @Lazy WikiService wikiService,
                                     CustomizeVectorStoreService storeService, EmbeddingProcessingRegistry processingRegistry) {
        this.documentService = documentService;
        this.wikiService = wikiService;
        this.storeService = storeService;
        this.processingRegistry = processingRegistry;
    }

    @Override
    public EmbeddingWorkEnum getType() {
        return EmbeddingWorkEnum.WIKI;
    }

    @Override
    public Boolean process(Long wikiId) {
        return doProcess(wikiId);
    }

    /**
     * 实际处理逻辑
     */
    private Boolean doProcess(Long wikiId) {
        WikiSettings wikiSettings = wikiService.getWikiSettings(wikiId);
        List<WikiCatalog> wikiCatalogs = wikiService.findWikiDocumentById(wikiId);
        // 整个知识库共用一个 VectorStore，避免每批都创建新实例
        VectorStore vectorStore = storeService.loadMilvusVectorStore();
        boolean isSuccess = true;
        for (WikiCatalog wikiCatalog : wikiCatalogs) {
            Long documentId = wikiCatalog.getDocumentId();
            if (wikiCatalog.getIsEmbedding()) {
                continue;
            }
            if (!processingRegistry.tryStart(documentId)) {
                log.info("文档正在向量化，跳过重复任务，documentId={}", documentId);
                continue;
            }
            try {
                String wikiIdStr = String.valueOf(wikiId);
                String documentIdStr = String.valueOf(documentId);

                FindDocumentDetailVO articleDetail = documentService.findDocumentDetail(documentId);
                String content = articleDetail.getContent();

                List<Document> documents;
                if (StringUtils.isBlank(content)) {
                    documents = new ArrayList<>();
                } else {
                    documents = MarkdownSplitter.splitByHeadersWithHierarchy(
                            UmoDocConverter.convertToMarkdown(content), Map.of("wiki_id", wikiIdStr, "document_id", documentIdStr),
                            wikiSettings.getChunkSize(), wikiSettings.getOverlapSize());
                }

                List<String> newChunkKeys = documents.stream().map(Document::getMetadata)
                        .map(item -> item.get("chunk_key").toString()).toList();
                List<String> oldChunkKeys = articleDetail.getChunkKeys();
                if (oldChunkKeys == null) {
                    oldChunkKeys = new ArrayList<>();
                }

                // 如果内容为空，需要删除所有旧的向量数据
                if (documents.isEmpty()) {
                    try {
                        // 删除该文档下所有旧的chunk
                        if (!oldChunkKeys.isEmpty()) {
                            FilterExpressionBuilder filter = new FilterExpressionBuilder();
                            for (String oldKey : oldChunkKeys) {
                                MilvusFilterExpressionConverter converter = new MilvusFilterExpressionConverter();
                                Filter.Expression build = filter.and(filter.eq("document_id", documentIdStr),
                                        filter.eq("chunk_key", oldKey)).build();
                                storeService.deleteMilvus(converter.convertExpression(build));
                            }
                        }
                        // 更新chunkKeys为空列表
                        documentService.updateChunkKeys(documentId, new ArrayList<>());
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        isSuccess = false;
                    }
                    continue; // 处理下一个文档
                }

                Set<String> newChunkKeySet = new LinkedHashSet<>(newChunkKeys);
                Set<String> oldChunkKeySet = new LinkedHashSet<>(oldChunkKeys);

                List<String> addChunk = new ArrayList<>();
                List<String> delChunk = new ArrayList<>();

                for (String newKey : newChunkKeySet) {
                    if (!oldChunkKeySet.contains(newKey)) addChunk.add(newKey);
                }
                for (String oldKey : oldChunkKeySet) {
                    if (!newChunkKeySet.contains(oldKey)) delChunk.add(oldKey);
                }

                // 分批入库：每次只取 STORE_BATCH_SIZE 个 Document，入库后即可被 GC
                Set<String> addChunkSet = new LinkedHashSet<>(addChunk);
                List<Document> batch = new ArrayList<>(STORE_BATCH_SIZE);
                for (Document doc : documents) {
                    Object chunkKey = doc.getMetadata().get("chunk_key");
                    if (chunkKey == null || !addChunkSet.contains(chunkKey.toString())) {
                        continue;
                    }
                    batch.add(doc);
                    if (batch.size() >= STORE_BATCH_SIZE) {
                        if (flushBatchFailed(vectorStore, batch, documentIdStr, delChunk)) {
                            isSuccess = false;
                        }
                        batch.clear(); // 复用同一个 batch 列表，避免重复分配
                    }
                }
                // 处理最后不足一批的
                if (!batch.isEmpty()) {
                    if (flushBatchFailed(vectorStore, batch, documentIdStr, delChunk)) {
                        isSuccess = false;
                    }
                }
                // 清理引用，帮助 GC
                batch.clear();
                documents.clear();

                // 更新 chunkKeys
                if (isSuccess) {
                    documentService.updateChunkKeys(documentId, newChunkKeys);
                }
                wikiService.enableEmbedding(List.of(wikiCatalog.getId()));
            } finally {
                processingRegistry.finish(documentId);
            }
        }
        return isSuccess;
    }

    /**
     * 将一批 Document 入库并删除旧 chunk
     *
     * @return false 表示失败
     */
    private boolean flushBatchFailed(VectorStore vectorStore, List<Document> batch, String documentIdStr, List<String> delChunk) {
        try {
            vectorStore.add(batch);
            if (!delChunk.isEmpty()) {
                FilterExpressionBuilder filter = new FilterExpressionBuilder();
                for (String delKey : delChunk) {
                    MilvusFilterExpressionConverter converter = new MilvusFilterExpressionConverter();
                    Filter.Expression build = filter.and(filter.eq("document_id", documentIdStr),
                            filter.eq("chunk_key", delKey)).build();
                    storeService.deleteMilvus(converter.convertExpression(build));
                }
                delChunk.clear(); // 删除完成后清空，避免重复删除
            }
            return false;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return true;
        }
    }
}
