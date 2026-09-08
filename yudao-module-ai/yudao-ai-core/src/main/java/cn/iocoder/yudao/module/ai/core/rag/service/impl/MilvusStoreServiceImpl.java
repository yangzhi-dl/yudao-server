package cn.iocoder.yudao.module.ai.core.rag.service.impl;

import cn.iocoder.yudao.module.ai.core.rag.enums.EmbeddingWorkEnum;
import cn.iocoder.yudao.module.ai.core.rag.factory.EmbeddingWorkFactory;
import cn.iocoder.yudao.module.ai.core.rag.service.CustomizeVectorStoreService;
import cn.iocoder.yudao.module.ai.core.rag.service.MilvusStoreService;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.milvus.MilvusFilterExpressionConverter;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MilvusStoreServiceImpl implements MilvusStoreService {
    private final CustomizeVectorStoreService storeService;
    private final EmbeddingWorkFactory embeddingWorkFactory;


    public MilvusStoreServiceImpl(CustomizeVectorStoreService storeService, EmbeddingWorkFactory embeddingWorkFactory) {
        this.storeService = storeService;
        this.embeddingWorkFactory = embeddingWorkFactory;
    }

    @Override
    public List<Document> loadDocumentByWikiReader(List<Long> wikiIds, List<Long> restrictedDocumentIds, String question, Integer topK) {
        if (Objects.isNull(wikiIds) || wikiIds.isEmpty()) {
            return Collections.emptyList();
        }

        Object[] wikiIdArray = wikiIds.stream()
                .map(String::valueOf)
                .toArray();

        FilterExpressionBuilder filter = new FilterExpressionBuilder();
        FilterExpressionBuilder.Op filterOp;

        FilterExpressionBuilder.Op wikiFilter = filter.in("wiki_id", wikiIdArray);

        if (!Objects.isNull(restrictedDocumentIds) && !restrictedDocumentIds.isEmpty()) {
            Object[] restrictedDocIdArray = restrictedDocumentIds.stream()
                    .map(String::valueOf)
                    .toArray();
            FilterExpressionBuilder.Op restrictedFilter = filter.nin("document_id", restrictedDocIdArray);
            filterOp = filter.and(wikiFilter, restrictedFilter);
        } else {
            filterOp = wikiFilter;
        }

        Filter.Expression filterExpression = filterOp.build();

        SearchRequest build = SearchRequest.builder()
                .query(question)
                .topK(topK)
                .filterExpression(filterExpression)
                .build();

        return storeService.loadMilvusVectorStore()
                .similaritySearch(build);
    }

    @Override
    public List<Document> loadDocumentByDocumentReader(List<Long> documentIds, String question, Integer topK) {
        if (Objects.isNull(documentIds) || documentIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 将 Long 类型的 wikiIds 转换为 String 类型的列表
        List<String> documentIdStrings = documentIds.stream()
                .map(String::valueOf)
                .toList();

        FilterExpressionBuilder filter = new FilterExpressionBuilder();
        SearchRequest build = SearchRequest.builder()
                .query(question)
                .topK(topK)
                .filterExpression(filter.in("document_id", documentIdStrings.toArray()).build()) // 使用 in 操作符
                .build();

        return storeService.loadMilvusVectorStore()
                .similaritySearch(build);
    }

    @Override
    public Boolean addDocumentByWiki(Long wikiId) {
        return embeddingWorkFactory.getService(EmbeddingWorkEnum.WIKI)
                .process(wikiId);
    }

    @Override
    public Boolean reloadDocumentByWiki(Long documentId) {
        return embeddingWorkFactory.getService(EmbeddingWorkEnum.RELOAD_DOCUMENT)
                .process(documentId);
    }

    @Override
    public void delDocumentByWiki(Long wikiId) {
        if (wikiId == null) {
            return;
        }
        FilterExpressionBuilder filter = new FilterExpressionBuilder();
        MilvusFilterExpressionConverter converter = new MilvusFilterExpressionConverter();
        Filter.Expression build = filter.eq("wiki_id", String.valueOf(wikiId)).build();
        storeService.deleteMilvus(converter.convertExpression(build));
    }

    @Override
    public void delDocumentByDocIds(List<Long> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            return;
        }

        FilterExpressionBuilder filter = new FilterExpressionBuilder();
        MilvusFilterExpressionConverter converter = new MilvusFilterExpressionConverter();

        Filter.Expression build = filter.in("document_id", documentIds.stream()
                .map(String::valueOf)
                .collect(Collectors.toList())).build();
        storeService.deleteMilvus(converter.convertExpression(build));
    }


}
