package cn.iocoder.yudao.module.ai.knowledge.common.factory.tool;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.ai.common.utils.ResultUtil;
import cn.iocoder.yudao.module.ai.core.rag.service.MilvusStoreService;
import cn.iocoder.yudao.module.ai.core.tools.factory.AITool;
import cn.iocoder.yudao.module.ai.knowledge.common.service.KnowledgeService;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.entity.WikiDetailCatalog;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.entity.WikiCatalogueTransformer;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.entity.WikiDetailTransformer;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.vo.FindBatchCatalogDocumentVO;
import cn.iocoder.yudao.module.ai.knowledge.wiki.service.WikiService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class WikiTool implements AITool {

    private final WikiService wikiService;

    private final KnowledgeService knowledgeService;

    private final MilvusStoreService milvusStoreService;

    public WikiTool(WikiService wikiService, KnowledgeService knowledgeService, MilvusStoreService milvusStoreService) {
        this.wikiService = wikiService;
        this.knowledgeService = knowledgeService;
        this.milvusStoreService = milvusStoreService;
    }

    @Override
    public Object getToolInstance() {
        return this;
    }

    @Override
    public String getName() {
        return "wiki-tools";
    }

    @Override
    public String getTitle() {
        return "系统知识库工具箱";
    }

    @Override
    public String getDescription() {
        return "根据输入的知识库ID和自然语言召回对应知识库里面的文本块或者获取对应知识库的相关详情信息";
    }

    @Tool(description = "获取所有知识库详情信息（不包含目录结构）")
    public String findWikisDetail() {
        try {
            List<WikiDetailTransformer> wikiDetail = wikiService.findWikiDetailByPermission();
            return JSONObject.toJSONString(wikiDetail);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "根据知识库ID列表批量查询知识库详情信息（包含目录结构）")
    public String findWikiDetailByIds(@ToolParam(description = "知识库的ID列表（禁止凭空捏造）") List<String> wikiIds) {
        try {
            List<Long> longList = wikiIds.stream()
                    .map(Long::parseLong).toList();

            Set<Long> hasWikisAccess = wikiService.hasWikisAccess(longList, Permission.READ);
            Set<Long> noPermissionIds = new HashSet<>(longList);
            noPermissionIds.removeAll(hasWikisAccess);

            List<WikiCatalogueTransformer> wikiDetail = TenantUtils.executeIgnore(() ->
                    wikiService.findWikiDetailByIds(hasWikisAccess.stream().toList()));

            // 批量查询每个目录下的文档数量
            for (WikiCatalogueTransformer transformer : wikiDetail) {
                List<Long> parentIds = collectAllCatalogIds(transformer.getCatalogue());
                Map<Long, List<FindBatchCatalogDocumentVO>> batchResult = wikiService.findBatchCatalogDocuments(transformer.getWikiId(), parentIds);
                Map<Long, Integer> countMap = new HashMap<>();
                for (Map.Entry<Long, List<FindBatchCatalogDocumentVO>> entry : batchResult.entrySet()) {
                    countMap.put(entry.getKey(), entry.getValue().size());
                }
                assignDocumentCounts(transformer.getCatalogue(), countMap);
            }

            JSONObject result = new JSONObject();
            result.put("data", wikiDetail);
            if (!noPermissionIds.isEmpty()) {
                result.put("permissionTip", "因当前账户对该知识库无读取权限，相关详情已被系统禁止，不参与本次查询："
                        + noPermissionIds.stream().sorted().map(String::valueOf).collect(Collectors.joining(", ")));
            }
            return result.toJSONString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "根据知识库ID和目录ID获取该目录下的所有文档列表（包含文档ID和标题）")
    public String findCatalogDocuments(@ToolParam(description = "知识库ID") String wikiId,
                                       @ToolParam(description = "目录ID（禁止凭空捏造）") String catalogId) {
        try {
            Long wikiIdLong = Long.parseLong(wikiId);
            Long catalogIdLong = Long.parseLong(catalogId);

            Map<Long, List<FindBatchCatalogDocumentVO>> batchResult = wikiService.findBatchCatalogDocuments(wikiIdLong, List.of(catalogIdLong));
            List<FindBatchCatalogDocumentVO> documents = batchResult.getOrDefault(catalogIdLong, Collections.emptyList());

            // 移除 id 字段，只保留 documentId 和 title
            List<Map<String, Object>> docList = documents.stream()
                    .map(doc -> {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("documentId", doc.getDocumentId());
                        item.put("title", doc.getTitle());
                        return item;
                    })
                    .collect(Collectors.toList());

            JSONObject result = new JSONObject();
            result.put("wikiId", wikiIdLong);
            result.put("catalogId", catalogIdLong);
            result.put("documents", docList);
            result.put("total", docList.size());
            return result.toJSONString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "执行知识库向量检索，从指定知识库中召回与用户查询最相关的文档片段。系统会根据相似度分数自动过滤低质量结果，仅返回高匹配度的内容，并附带来源元数据（知识库名称和文档标题），以便后续展示引用来源或追溯完整文档。")
    public String loadDocumentByWikiReader(@ToolParam(description = "知识库ID列表，指定要检索的知识库范围（禁止凭空捏造）") List<String> wikiIds,
                                           @ToolParam(description = "需查询内容（建议精简为关键词或短句）") String question,
                                           @ToolParam(description = "检索时返回的候选内容数量（topK）") Integer topK,
                                           @ToolParam(description = "相似度分数阈值，只返回分数大于该值的内容（0 ~ 1）") Double score) {
        try {
            List<Document> documentsFilter;
            List<Long> longList = wikiIds.stream()
                    .map(Long::parseLong).toList();

            Set<Long> hasWikisAccess = wikiService.hasWikisAccess(longList, Permission.EXECUTE);

            // 计算无权限的知识库ID
            Set<Long> noPermissionIds = new HashSet<>(longList);
            noPermissionIds.removeAll(hasWikisAccess);

            Set<Long> restrictedDocumentIds = knowledgeService.getRestrictedDocumentIdsWithWikiFallback(Permission.EXECUTE);
            List<Document> documents = milvusStoreService.loadDocumentByWikiReader(
                    hasWikisAccess.stream().toList(), restrictedDocumentIds.stream().toList(), question, topK);
            if (Objects.nonNull(documents)) {
                // 过滤文档并将符合条件的文档添加到 documentsFilter
                documentsFilter = documents.stream()
                        .filter(doc -> Optional.ofNullable(doc.getScore()).orElse(0.0) > score)
                        .collect(Collectors.toList());

                // 如果过滤后没有符合条件的文档，则选择概率最大的文档
                if (documentsFilter.isEmpty()) {
                    Optional<Document> maxScoreDoc = documents.stream()
                            .max(Comparator.comparingDouble(doc -> Optional.ofNullable(doc.getScore()).orElse(0.0)));
                    maxScoreDoc.ifPresent(documentsFilter::add); // 将概率最大的文档加入 documentsFilter
                }

                List<WikiCatalogueTransformer> wikiDetails = TenantUtils.executeIgnore(() -> wikiService.findWikiDetailByIds(longList));
                // 将 wikiId -> WikiDetailVO 映射起来，方便快速查找
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
                    Object documentIdObj = metadata.get("document_id");
                    Object wikiIdObj = metadata.get("wiki_id");
                    Object index = metadata.get("chunk_index").toString().replaceAll("\\.0+$", "");

                    Long documentId = Long.parseLong(documentIdObj.toString());
                    Long wikiId = Long.parseLong(wikiIdObj.toString());

                    // 获取知识库名称和文档标题
                    String wikiName = "";
                    WikiCatalogueTransformer wikiDetail = wikiDetailMap.get(wikiId);
                    if (wikiDetail != null) {
                        wikiName = wikiDetail.getTitle();
                    }
                    String documentTitle = docTitleMap.getOrDefault(documentId, "未知文档");

                    String url = String.format("/ai/data/wiki/preview?wikiId=%s&documentId=%s&index=%s", wikiId, documentId, index);
                    String context = document.getText();

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
                // 拼接文档内容
                if (!filteredContent.isEmpty()) {
                    String content = String.join("\n", filteredContent);
                    if (!noPermissionIds.isEmpty()) {
                        String permissionTip = "【权限提示】因当前账户对该知识库无执行权限，相关查询已被系统禁止，不参与本次检索："
                                + noPermissionIds.stream().sorted().map(String::valueOf).collect(Collectors.joining(", "))
                                + "\n\n";
                        return permissionTip + content;
                    }
                    return content;
                }
            }
            if (!noPermissionIds.isEmpty()) {
                return "【权限提示】因当前账户对该知识库无执行权限，相关查询已被系统禁止，不参与本次检索："
                        + noPermissionIds.stream().sorted().map(String::valueOf).collect(Collectors.joining(", "));
            }
            return "";
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

    /**
     * 递归将文档数量赋值到目录树的每个节点上
     */
    private void assignDocumentCounts(List<WikiDetailCatalog> catalogues, Map<Long, Integer> countMap) {
        if (catalogues == null) return;
        for (WikiDetailCatalog catalog : catalogues) {
            catalog.setDocumentCount(countMap.getOrDefault(catalog.getCatalogId(), 0));
            if (catalog.getChildren() != null && !catalog.getChildren().isEmpty()) {
                assignDocumentCounts(catalog.getChildren(), countMap);
            }
        }
    }

}
