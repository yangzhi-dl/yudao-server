package cn.iocoder.yudao.module.ai.core.workflow.engine.node;

import cn.iocoder.yudao.module.ai.core.rag.service.MilvusStoreService;
import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowGraphNode;
import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowRunContext;
import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowVariableResolver;
import cn.iocoder.yudao.module.ai.core.workflow.engine.handler.WorkflowNodeHandlerAdapter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 知识库节点：从多选的知识库中向量召回与查询内容最相关的文本块。
 * <p>
 * 前置必须连接「大模型（结构化 JSON 模式）」节点，由大模型根据用户问题输出查询内容字段，
 * 该字段经 {@code queryRef} 引用后作为本次召回的问题；节点可配置召回数量 {@code topK}
 * 与置信度阈值 {@code score}，仅召回相似度大于等于该阈值的片段。
 *
 * @author yudao
 */
@Slf4j
@Component
public class KnowledgeNodeHandler implements WorkflowNodeHandler, WorkflowNodeHandlerAdapter {

    private static final String DEFAULT_OUTPUT = "content";

    private final MilvusStoreService milvusStoreService;

    public KnowledgeNodeHandler(MilvusStoreService milvusStoreService) {
        this.milvusStoreService = milvusStoreService;
    }

    @Override
    public String[] types() {
        return new String[]{"knowledgeNode", "knowledge"};
    }

    @Override
    public Map<String, Object> execute(WorkflowGraphNode node, WorkflowRunContext ctx, Map<String, Object> memory) {
        JSONObject data = node.getData();

        // 1. 解析查询内容引用：{{上游大模型节点.查询字段}}，渲染为实际查询文本
        String queryRef = data == null ? null : data.getString("queryRef");
        String query = queryRef == null ? "" : WorkflowVariableResolver.render(queryRef, memory);
        if (!StringUtils.hasText(query)) {
            throw new IllegalArgumentException("知识库节点 [%s] 未配置查询内容或查询内容为空，请确保前置大模型(JSON模式)已配置查询字段".formatted(
                    node.getName() == null ? node.getId() : node.getName()));
        }

        // 2. 解析知识库 ID 列表
        List<Long> wikiIds = parseWikiIds(data == null ? null : data.getJSONArray("knowledgeBaseIds"));
        if (wikiIds.isEmpty()) {
            throw new IllegalArgumentException("知识库节点 [%s] 未选择知识库".formatted(
                    node.getName() == null ? node.getId() : node.getName()));
        }

        // 3. 召回数量（topK）
        Integer topK = data == null ? null : data.getInteger("topK");
        final int recallCount;
        if (topK == null || topK < 1) {
            recallCount = 5;
        } else {
            recallCount = topK;
        }

        // 4. 置信度阈值（score）
        Double score = data == null ? null : data.getDouble("score");
        final double scoreThreshold;
        if (score == null || score < 0 || score > 1) {
            scoreThreshold = 0.0;
        } else {
            scoreThreshold = score;
        }

        List<Document> documents = milvusStoreService.loadDocumentByWikiReader(
                wikiIds, Collections.emptyList(), query, recallCount);

        List<Document> matched = documents == null ? List.of()
                : documents.stream()
                        .filter(doc -> Optional.ofNullable(doc.getScore()).orElse(0.0) >= scoreThreshold)
                        .collect(Collectors.toList());

        String content = matched.stream()
                .map(this::formatDocument)
                .collect(Collectors.joining("\n\n---\n\n"));

        return Map.of(outputNameOf(node), content);
    }

    /**
     * 提示词注入：当该知识库节点作为大模型节点的下游（承接点）时，
     * 向来源大模型提供「检索查询生成」提示词模板（含所选知识库信息），
     * 用户的系统提示词将置于 {@code %s} 处。
     */
    @Override
    public String buildPredecessorSystemPrompt(WorkflowGraphNode self, WorkflowGraphNode predecessor) {
        JSONObject data = self.getData();
        if (data == null) {
            return null;
        }
        JSONArray ids = data.getJSONArray("knowledgeBaseIds");
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        StringBuilder prompt = new StringBuilder();
        prompt.append("用户把该模型输出作为「知识库检索」的查询内容。请根据用户的实际问题，生成一段简洁、聚焦、适合向量语义检索的查询文本");
        List<String> kbInfo = knowledgeBaseInfo(data);
        if (!kbInfo.isEmpty()) {
            prompt.append("，需要检索的知识库为：").append(String.join("、", kbInfo));
        }
        prompt.append("。\n");
        // 模板：前缀 + %s（用户系统提示词）+ 后缀（字段说明取自前驱大模型的 outputDefs）
        String fields = LlmNodeHandler.buildOutputFieldsDescription(
                predecessor == null ? null : predecessor.getData());
        String fieldDesc = fields.isBlank() ? "字段与输出定义一致" : fields;
        return prompt + "%s\n只返回一个合法的 JSON 对象，不要 Markdown 代码块，字段定义为：" + fieldDesc + "\n";
    }

    private List<Long> parseWikiIds(JSONArray array) {
        if (array == null || array.isEmpty()) {
            return List.of();
        }
        List<Long> ids = new ArrayList<>(array.size());
        for (int i = 0; i < array.size(); i++) {
            Object o = array.get(i);
            if (o == null) {
                continue;
            }
            try {
                ids.add(Long.valueOf(String.valueOf(o)));
            } catch (NumberFormatException e) {
                log.warn("[knowledge][节点] 忽略非法知识库ID: {}", o);
            }
        }
        return ids;
    }

    /** 汇总所选知识库信息用于提示词注入：优先取名称，取不到时回退到知识库 ID */
    private List<String> knowledgeBaseInfo(JSONObject data) {
        JSONArray names = data.getJSONArray("knowledgeBaseNames");
        if (names != null && !names.isEmpty()) {
            List<String> titleList = new ArrayList<>();
            for (int i = 0; i < names.size(); i++) {
                Object o = names.get(i);
                if (o != null && !String.valueOf(o).isBlank()) {
                    titleList.add(String.valueOf(o));
                }
            }
            if (!titleList.isEmpty()) {
                return titleList;
            }
        }
        JSONArray ids = data.getJSONArray("knowledgeBaseIds");
        if (ids != null && !ids.isEmpty()) {
            List<String> idList = new ArrayList<>();
            for (int i = 0; i < ids.size(); i++) {
                Object o = ids.get(i);
                if (o != null) {
                    idList.add(String.valueOf(o));
                }
            }
            return idList;
        }
        return List.of();
    }

    private String formatDocument(Document document) {
        Map<String, Object> metadata = document.getMetadata();
        Object wikiIdObj = metadata.get("wiki_id");
        Object documentIdObj = metadata.get("document_id");
        Object indexObj = metadata.get("chunk_index");
        String wikiId = wikiIdObj == null ? "" : String.valueOf(wikiIdObj);
        String documentId = documentIdObj == null ? "" : String.valueOf(documentIdObj);
        String index = indexObj == null ? "" : String.valueOf(indexObj).replaceAll("\\.0+$", "");
        String url = String.format("/ai/data/wiki/preview?wikiId=%s&documentId=%s&index=%s",
                wikiId, documentId, index);
        String score = Optional.ofNullable(document.getScore()).map(String::valueOf).orElse("");

        return String.format("""
                相似度：%s
                来源链接：%s

                %s""", score, url, document.getText() == null ? "" : document.getText());
    }

    private String outputNameOf(WorkflowGraphNode node) {
        JSONArray outputDefs = node.getData() == null ? null : node.getData().getJSONArray("outputDefs");
        if (outputDefs != null && !outputDefs.isEmpty()) {
            String name = outputDefs.getJSONObject(0).getString("name");
            if (StringUtils.hasText(name)) {
                return name;
            }
        }
        return DEFAULT_OUTPUT;
    }
}