package cn.iocoder.yudao.module.ai.knowledge.common.factory.tool;

import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.ai.common.utils.ResultUtil;
import cn.iocoder.yudao.module.ai.core.rag.service.MilvusStoreService;
import cn.iocoder.yudao.module.ai.core.tools.factory.AITool;
import cn.iocoder.yudao.module.ai.knowledge.common.service.KnowledgeService;
import cn.iocoder.yudao.module.ai.knowledge.document.model.entity.DocumentSearch;
import cn.iocoder.yudao.module.ai.knowledge.document.model.vo.DocumentDetailTransformer;
import cn.iocoder.yudao.module.ai.knowledge.document.service.DocumentService;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.entity.WikiDetailCatalog;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.entity.WikiCatalogueTransformer;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.vo.FindBatchCatalogDocumentVO;
import cn.iocoder.yudao.module.ai.knowledge.wiki.service.WikiService;
import cn.iocoder.yudao.module.ai.search.enums.IndexNameEnum;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.alibaba.fastjson.JSONObject;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DocumentTool implements AITool {

    private final DocumentService documentService;
    private final MilvusStoreService milvusStoreService;
    private final WikiService wikiService;
    private final KnowledgeService knowledgeService;
    private final ElasticsearchClient elasticsearchClient;

    public DocumentTool(DocumentService documentService,
                        MilvusStoreService milvusStoreService,
                        WikiService wikiService, KnowledgeService knowledgeService,
                        ElasticsearchClient elasticsearchClient) {
        this.documentService = documentService;
        this.milvusStoreService = milvusStoreService;
        this.wikiService = wikiService;
        this.knowledgeService = knowledgeService;
        this.elasticsearchClient = elasticsearchClient;
    }

    @Override
    public Object getToolInstance() {
        return this;
    }

    @Override
    public String getName() {
        return "document-tools";
    }

    @Override
    public String getTitle() {
        return "系统文档工具箱";
    }

    @Override
    public String getDescription() {
        return "根据输入的文档ID和自然语言召回对应文档里面的文本块或者获取对应文档的相关详情信息";
    }

    @Tool(description = "根据文档ID列表批量查询文档详情信息")
    public String findDocumentDetailByIds(@ToolParam(description = "文档的ID列表（禁止凭空捏造）") List<String> documentIds) {
        try {
            List<Long> longList = documentIds.stream()
                    .map(Long::parseLong).toList();

            List<Long> permissionByDocumentIds = knowledgeService.filterWikiDocumentIdsByPermission(longList, Permission.READ);

            // 计算无权限的文档ID
            Set<Long> noPermissionIds = new HashSet<>(longList);
            permissionByDocumentIds.forEach(noPermissionIds::remove);

            List<DocumentDetailTransformer> documentDetailTransformers = documentService.findDocumentDetailByIds(permissionByDocumentIds);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("documents", documentDetailTransformers);
            if (!noPermissionIds.isEmpty()) {
                result.put("noPermissionDocumentIds", noPermissionIds.stream().sorted().toList());
                result.put("noPermissionMessage", "以下文档ID因无读取权限已被过滤: " + noPermissionIds.stream().sorted().map(String::valueOf).collect(Collectors.joining(", ")));
            }
            return JSONObject.toJSONString(result);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 递归收集目录树中所有的 catalogId，用于批量查询文档信息
     */
    private List<Long> collectAllCatalogIds(List<WikiDetailCatalog> catalogues) {
        List<Long> ids = new ArrayList<>();
        ids.add(0L); // root level
        collectCatalogIdsRecursive(catalogues, ids);
        return ids;
    }

    private void collectCatalogIdsRecursive(List<WikiDetailCatalog> catalogues, List<Long> ids) {
        if (catalogues == null) return;
        for (WikiDetailCatalog catalog : catalogues) {
            ids.add(catalog.getCatalogId());
            if (catalog.getChildren() != null && !catalog.getChildren().isEmpty()) {
                collectCatalogIdsRecursive(catalog.getChildren(), ids);
            }
        }
    }

    @Tool(description = "执行知识库文档向量检索，从指定知识库文档中召回与用户查询最相关的文档片段。系统会根据相似度分数自动过滤低质量结果，仅返回高匹配度的内容，并附带来源元数据（知识库名称和文档标题），以便后续展示引用来源或追溯完整文档。")
    public String loadDocumentByWikiDocumentReader(@ToolParam(description = "文档ID列表，指定要检索的文档范围（禁止凭空捏造）") List<String> documentIds,
                                               @ToolParam(description = "需查询内容（建议精简为关键词或短句）") String question,
                                               @ToolParam(description = "检索时返回的候选内容数量（topK）") Integer topK,
                                               @ToolParam(description = "相似度分数阈值，只返回分数大于该值的内容（0 ~ 1）") Double score) {
        try {
            List<Document> documentsFilter;
            List<Long> longList = documentIds.stream()
                    .map(Long::parseLong).toList();

            List<Long> permissionByDocumentIds = knowledgeService.filterWikiDocumentIdsByPermission(longList, Permission.EXECUTE);

            // 计算无权限的文档ID
            Set<Long> noPermissionIds = new HashSet<>(longList);
            permissionByDocumentIds.forEach(noPermissionIds::remove);

            List<Document> documents = milvusStoreService.loadDocumentByDocumentReader(permissionByDocumentIds, question, topK);
            if (Objects.nonNull(documents)) {
                // 过滤文档并将符合条件的文档添加到 documentsFilter
                documentsFilter = documents.stream()
                        .filter(doc -> Optional.ofNullable(doc.getScore()).orElse(0.0) > score)
                        .collect(Collectors.toList());

                // 如果过滤后没有符合条件的文档，则选择概率最大的文档
                if (documentsFilter.isEmpty()) {
                    Optional<Document> maxScoreDoc = documents.stream()
                            .max(Comparator.comparingDouble(doc -> Optional.ofNullable(doc.getScore()).orElse(0.0)));
                    maxScoreDoc.ifPresent(documentsFilter::add);
                }

                // 收集所有涉及的知识库ID
                Set<Long> wikiIdSet = documentsFilter.stream()
                        .map(doc -> {
                            Object wikiIdObj = doc.getMetadata().get("wiki_id");
                            return Long.parseLong(wikiIdObj.toString());
                        })
                        .collect(Collectors.toSet());

                // 批量查询知识库详情
                List<WikiCatalogueTransformer> wikiDetails = wikiService.findWikiDetailByIds(new ArrayList<>(wikiIdSet));
                Map<Long, WikiCatalogueTransformer> wikiDetailMap = wikiDetails.stream()
                        .collect(Collectors.toMap(WikiCatalogueTransformer::getWikiId, Function.identity()));

                // 批量查询文档信息，构建 documentId -> title 映射
                Map<Long, String> docTitleMap = new HashMap<>();
                for (Map.Entry<Long, WikiCatalogueTransformer> entry : wikiDetailMap.entrySet()) {
                    Long wikiId = entry.getKey();
                    List<Long> parentIds = collectAllCatalogIds(entry.getValue().getCatalogue());
                    Map<Long, List<FindBatchCatalogDocumentVO>> batchResult = wikiService.findBatchCatalogDocuments(wikiId, parentIds);
                    for (List<FindBatchCatalogDocumentVO> vos : batchResult.values()) {
                        for (FindBatchCatalogDocumentVO vo : vos) {
                            if (vo.getDocumentId() != null) {
                                docTitleMap.put(vo.getDocumentId(), vo.getTitle());
                            }
                        }
                    }
                }

                List<String> filteredContent = documentsFilter.stream().map(document -> {
                    Map<String, Object> metadata = document.getMetadata();
                    Long documentId = Long.parseLong(metadata.get("document_id").toString());
                    Long wikiId = Long.parseLong(metadata.get("wiki_id").toString());
                    String index = metadata.get("chunk_index").toString().replaceAll("\\.0+$", "");

                    // 获取知识库名称和文档标题
                    String wikiName = "";
                    WikiCatalogueTransformer wikiDetail = wikiDetailMap.get(wikiId);
                    if (wikiDetail != null) {
                        wikiName = wikiDetail.getTitle();
                    }
                    String documentTitle = docTitleMap.getOrDefault(documentId, "未知文档");

                    String url = String.format("/ai/data/wiki/preview?wikiId=%s&documentId=%s&index=%s", wikiId, documentId, index);
                    String context = document.getText();

                    // 返回带知识库名称和文档标题的文本块（使用 \n 换行，兼容性好）
                    return String.format("""
                            知识库：%s
                            文档：%s
                            文本块链接：%s
                            文本块内容：
                            %s
                            """, wikiName, documentTitle,
                            String.format("[^%s]: [%s](%s)",
                                    index, ResultUtil.getFirstLineIfH1(context), url), ResultUtil.removeFirstLineIfH1(context));
                }).collect(Collectors.toList());

                // 拼接文档内容（使用 \n\n 分隔不同文档块）
                if (!filteredContent.isEmpty()) {
                    String content = String.join("\n\n", filteredContent);
                    if (!noPermissionIds.isEmpty()) {
                        String permissionTip = "【权限提示】因当前账户对以下文档无执行权限，相关查询已被系统禁止，不参与本次检索："
                                + noPermissionIds.stream().sorted().map(String::valueOf).collect(Collectors.joining(", "))
                                + "\n\n";
                        return permissionTip + content;
                    }
                    return content;
                }
            }
            if (!noPermissionIds.isEmpty()) {
                return "【权限提示】因当前账户对以下文档无执行权限，相关查询已被系统禁止，不参与本次检索："
                        + noPermissionIds.stream().sorted().map(String::valueOf).collect(Collectors.joining(", "));
            }
            return "";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "调用本地搜索引擎服务，对指定索引中的文档执行关键词匹配查询，在文档标题、摘要、分类和标签中进行全文匹配，返回匹配到的文档信息。" +
            "支持两种匹配模式：EXACT（精确匹配，要求关键词所有词都命中，结果少但精度高，适合关键词少且明确时使用）" +
            "和 LOOSE（宽松匹配，关键词任一词命中即可，结果多但可能有噪声，适合关键词多或不太确定时使用）。" +
            "各字段 boost 可控制权重：标题默认3倍、摘要默认2倍、分类默认1倍、标签默认1倍，调高可增强该字段的匹配贡献度。")
    public String searchDocument(@ToolParam(description = "搜索关键词") String keyword,
                                  @ToolParam(description = "匹配模式：EXACT（精确，要求所有词命中）或 LOOSE（宽松，任一词命中即可）", required = false) String matchMode,
                                  @ToolParam(description = "页码，从1开始，默认1", required = false) Integer pageNum,
                                  @ToolParam(description = "每页数量，默认10", required = false) Integer pageSize,
                                  @ToolParam(description = "标题字段权重，默认3.0", required = false) Double titleBoost,
                                  @ToolParam(description = "摘要字段权重，默认2.0", required = false) Double summaryBoost,
                                  @ToolParam(description = "分类字段权重，默认1.0", required = false) Double categoryBoost,
                                  @ToolParam(description = "标签字段权重，默认1.0", required = false) Double tagsBoost) {
        try {
            int page = pageNum != null && pageNum > 0 ? pageNum : 1;
            int size = pageSize != null && pageSize > 0 ? pageSize : 10;
            double tBoost = titleBoost != null && titleBoost > 0 ? titleBoost : 3.0;
            double sBoost = summaryBoost != null && summaryBoost > 0 ? summaryBoost : 2.0;
            double cBoost = categoryBoost != null && categoryBoost > 0 ? categoryBoost : 1.0;
            double tgBoost = tagsBoost != null && tagsBoost > 0 ? tagsBoost : 1.0;
            Operator operator = "LOOSE".equalsIgnoreCase(matchMode) ? Operator.Or : Operator.And;

            List<String> fields = List.of(
                    String.format("title^%.1f", tBoost),
                    String.format("summary^%.1f", sBoost),
                    String.format("category^%.1f", cBoost),
                    String.format("tags^%.1f", tgBoost)
            );

            // 获取当前用户有 READ 权限的文档 ID，在查询前过滤
            List<Long> permittedIds = knowledgeService.selectWikiDocumentIdByPermission(Permission.READ);
            if (permittedIds == null || permittedIds.isEmpty()) {
                return "当前用户没有任何文档的读取权限";
            }

            SearchRequest request = SearchRequest.of(s -> s
                    .index(IndexNameEnum.DOCUMENT.getValue())
                    .query(q -> q
                            .bool(b -> b
                                    .must(m -> m.multiMatch(mm -> mm
                                            .query(keyword)
                                            .fields(fields)
                                            .operator(operator)
                                    ))
                                    .filter(f -> f.terms(t -> t
                                            .field("id")
                                            .terms(tq -> tq.value(permittedIds.stream()
                                                    .map(FieldValue::of)
                                                    .toList()))
                                    ))
                            )
                    )
                    .from((page - 1) * size)
                    .size(size)
                    .trackTotalHits(th -> th.enabled(true))
            );

            SearchResponse<DocumentSearch> response = elasticsearchClient.search(request, DocumentSearch.class);

            List<DocumentSearch> hits = response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .toList();

            if (hits.isEmpty()) {
                return "未找到与 \"" + keyword + "\" 相关的文档";
            }

            List<Map<String, Object>> results = new ArrayList<>();
            for (DocumentSearch doc : hits) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("document_id", doc.getId());
                item.put("title", doc.getTitle());
                item.put("summary", doc.getSummary());
                item.put("category", doc.getCategory());
                item.put("tags", doc.getTags());
                results.add(item);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("keyword", keyword);
            result.put("total", response.hits().total() != null ? response.hits().total().value() : hits.size());
            result.put("documents", results);

            return JSONObject.toJSONString(result);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}