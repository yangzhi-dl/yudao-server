package cn.iocoder.yudao.module.ai.search.repository;

import cn.iocoder.yudao.module.ai.search.builder.BoolQueryBuilder;
import cn.iocoder.yudao.module.ai.search.enums.IndexNameEnum;
import cn.iocoder.yudao.module.ai.search.model.FilterCondition;
import cn.iocoder.yudao.module.ai.search.model.QueryParam;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Conflicts;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用 Elasticsearch Repository（无逻辑删除）
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class GenericElasticsearchRepository<T> {
    
    private final ElasticsearchClient elasticsearchClient;

    private final BoolQueryBuilder boolQueryBuilder;

    /**
     * 单条写入
     */
    public String save(IndexNameEnum indexName, String id, T document) throws IOException {
        IndexRequest<T> request = IndexRequest.of(builder -> builder
            .index(indexName.getValue())
            .id(id)
            .document(document)
            .refresh(Refresh.True)
        );
        
        IndexResponse response = elasticsearchClient.index(request);
        log.info("写入文档: index={}, id={}, result={}", indexName, id, response.result());
        return response.id();
    }
    
    /**
     * 批量写入（自动分批，每批最多 2000 条，避免 OOM 和 ES 请求体过大）
     * @param idExtractor 从文档对象中提取 ES _id 的函数，返回 null 则由 ES 自动生成
     */
    public BulkResponse bulkSave(IndexNameEnum indexName, List<T> documents,
                                  Function<T, String> idExtractor) throws IOException {
        if (documents == null || documents.isEmpty()) {
            return BulkResponse.of(b -> b.items(List.of()));
        }

        int totalSize = documents.size();
        int batchSize = 2000;
        BulkResponse lastResponse = null;

        for (int start = 0; start < totalSize; start += batchSize) {
            int end = Math.min(start + batchSize, totalSize);
            List<T> batch = documents.subList(start, end);

            List<BulkOperation> operations = batch.stream()
                    .map(doc -> {
                        String docId = idExtractor.apply(doc);
                        return BulkOperation.of(op -> op
                                .index(idx -> {
                                    idx.index(indexName.getValue()).document(doc);
                                    if (docId != null) {
                                        idx.id(docId);
                                    }
                                    return idx;
                                })
                        );
                    })
                    .collect(Collectors.toList());

            BulkRequest request = BulkRequest.of(builder -> builder
                    .operations(operations)
                    .refresh(Refresh.True)
            );

            lastResponse = elasticsearchClient.bulk(request);
        }

        log.info("批量写入完成: index={}, 总数: {}", indexName, totalSize);
        return lastResponse;
    }
    
    /**
     * 根据 ID 查询
     */
    public Optional<T> findById(IndexNameEnum indexName, String id, Class<T> clazz) throws IOException {
        GetRequest request = GetRequest.of(builder -> builder
            .index(indexName.getValue())
            .id(id)
        );
        
        GetResponse<T> response = elasticsearchClient.get(request, clazz);
        if (response.found()) {
            assert response.source() != null;
            return Optional.of(response.source());
        } else {
            return Optional.empty();
        }
    }
    
    /**
     * 通用查询（支持动态过滤）
     */
    public SearchResponse<T> search(IndexNameEnum indexName, QueryParam param, Class<T> clazz) throws IOException {
        // 构建查询条件
        Query query = buildQuery(param);
        
        // 构建 SearchRequest
        SearchRequest.Builder requestBuilder = new SearchRequest.Builder()
            .index(indexName.getValue())
            .query(query)
            .from((param.getPageNum() - 1) * param.getPageSize())
            .size(param.getPageSize())
            .trackTotalHits(t -> t.enabled(true)); // 精确统计总数
        
        // 添加排序
        if (param.getSortField() != null) {
            requestBuilder.sort(s -> s
                .field(f -> f
                    .field(param.getSortField())
                    .order("asc".equalsIgnoreCase(param.getSortOrder()) ? SortOrder.Asc : SortOrder.Desc)
                )
            );
        }
        
        // 4. 执行查询
        SearchRequest request = requestBuilder.build();
        return elasticsearchClient.search(request, clazz);
    }
    
    /**
     * 构建 Query（核心方法）
     * 移除了逻辑删除过滤，只保留业务过滤条件
     */
    private Query buildQuery(QueryParam param) {
        if (param.getFilters() == null || param.getFilters().isEmpty()) {
            // 无条件时匹配所有
            return MatchAllQuery.of(m -> m)._toQuery();
        }
        
        // 直接使用 BoolQueryBuilder 构建业务过滤条件
        return boolQueryBuilder.build(param.getFilters());
    }
    
    /**
     * 物理删除（直接删除文档）
     */
    public void delete(IndexNameEnum indexName, String id) throws IOException {
        DeleteRequest request = DeleteRequest.of(builder -> builder
            .index(indexName.getValue())
            .id(id)
            .refresh(Refresh.True)
        );
        
        DeleteResponse response = elasticsearchClient.delete(request);
        log.info("删除文档: index={}, id={}, result={}", indexName, id, response.result());
    }
    
    /**
     * 批量删除（自动分批，每批最多 2000 条）
     */
    public BulkResponse bulkDelete(IndexNameEnum indexName, List<String> ids) throws IOException {
        if (ids == null || ids.isEmpty()) {
            return BulkResponse.of(b -> b.items(List.of()));
        }

        int totalSize = ids.size();
        int batchSize = 2000;
        BulkResponse lastResponse = null;

        for (int start = 0; start < totalSize; start += batchSize) {
            int end = Math.min(start + batchSize, totalSize);
            List<String> batch = ids.subList(start, end);

            List<BulkOperation> operations = batch.stream()
                    .map(id -> BulkOperation.of(op -> op
                            .delete(d -> d.index(indexName.getValue()).id(id))
                    ))
                    .collect(Collectors.toList());

            BulkRequest request = BulkRequest.of(builder -> builder
                    .operations(operations)
                    .refresh(Refresh.True)
            );

            lastResponse = elasticsearchClient.bulk(request);
        }

        log.info("批量删除完成: index={}, 总数: {}", indexName, totalSize);
        return lastResponse;
    }
    
    /**
     * 根据条件删除（慎用）
     */
    public DeleteByQueryResponse deleteByQuery(IndexNameEnum indexName, List<FilterCondition> filters) throws IOException {
        Query query = boolQueryBuilder.build(filters);
        
        DeleteByQueryRequest request = DeleteByQueryRequest.of(builder -> builder
            .index(indexName.getValue())
            .query(query)
            .refresh(true)
            .conflicts(Conflicts.Proceed) // 继续执行即使有版本冲突
        );
        
        DeleteByQueryResponse response = elasticsearchClient.deleteByQuery(request);
        log.info("按条件删除: index={}, 删除数量: {}", indexName, response.deleted());
        
        return response;
    }
    
    /**
     * 统计总数
     */
    public long count(IndexNameEnum indexName, List<FilterCondition> filters) throws IOException {
        Query query = boolQueryBuilder.build(filters);
        
        CountRequest request = CountRequest.of(builder -> builder
            .index(indexName.getValue())
            .query(query)
        );
        
        CountResponse response = elasticsearchClient.count(request);
        return response.count();
    }
    
    /**
     * 判断文档是否存在
     */
    public boolean exists(IndexNameEnum indexName, String id) throws IOException {
        ExistsRequest request = ExistsRequest.of(builder -> builder
            .index(indexName.getValue())
            .id(id)
        );

        BooleanResponse exists = elasticsearchClient.exists(request);
        return exists.value();
    }

    /**
     * 获取索引中所有文档的 ID 列表（通过 scroll API）
     */
    public List<String> fetchAllIds(IndexNameEnum indexName) throws IOException {
        // 索引不存在则直接返回空列表
        if (!indexExists(indexName)) {
            log.info("索引不存在，跳过查询: index={}", indexName);
            return List.of();
        }

        // 初始化搜索，获取第一页和 scroll_id
        SearchRequest initialRequest = SearchRequest.of(s -> s
                .index(indexName.getValue())
                .query(MatchAllQuery.of(m -> m)._toQuery())
                .size(1000)
                .scroll(ts -> ts.time("2m"))
        );

        SearchResponse<T> response = elasticsearchClient.search(initialRequest, getDocumentClass());
        String scrollId = response.scrollId();

        List<String> allIds = new java.util.ArrayList<>();
        // 提取第一页的 ID
        response.hits().hits().forEach(hit -> allIds.add(hit.id()));

        // 滚动获取剩余数据
        while (response.hits().hits().size() == 1000) {
            String currentScrollId = scrollId;
            ScrollResponse<T> scrollResponse = elasticsearchClient.scroll(s -> s
                    .scrollId(currentScrollId)
                    .scroll(ts -> ts.time("2m")), getDocumentClass());
            scrollId = scrollResponse.scrollId();
            List<Hit<T>> hits = scrollResponse.hits().hits();
            if (hits.isEmpty()) {
                break;
            }
            hits.forEach(hit -> allIds.add(hit.id()));
        }

        // 清理 scroll
        if (scrollId != null) {
            String finalScrollId = scrollId;
            elasticsearchClient.clearScroll(cs -> cs.scrollId(finalScrollId));
        }

        log.info("获取索引所有ID: index={}, count={}", indexName, allIds.size());
        return allIds;
    }

    /**
     * 判断索引是否存在
     */
    public boolean indexExists(IndexNameEnum indexName) throws IOException {
        return elasticsearchClient.indices().exists(e -> e.index(indexName.getValue())).value();
    }

    /**
     * 确保索引存在，不存在则创建
     */
    public void ensureIndexExists(IndexNameEnum indexName) throws IOException {
        if (!indexExists(indexName)) {
            elasticsearchClient.indices().create(c -> c.index(indexName.getValue()));
            log.info("索引已创建: index={}", indexName);
        }
    }

    /**
     * 用于 fetchAllIds 的泛型类型提取，子类可按需覆盖
     */
    @SuppressWarnings("unchecked")
    private Class<T> getDocumentClass() {
        return (Class<T>) Object.class;
    }

}