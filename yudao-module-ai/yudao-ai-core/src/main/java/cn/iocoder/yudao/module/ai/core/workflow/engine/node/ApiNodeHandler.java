package cn.iocoder.yudao.module.ai.core.workflow.engine.node;

import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowGraphNode;
import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowRunContext;
import cn.iocoder.yudao.module.ai.core.workflow.engine.WorkflowVariableResolver;
import cn.iocoder.yudao.module.ai.core.workflow.engine.handler.WorkflowNodeHandlerAdapter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * API 请求节点：按 Postman 风格配置（方法 / URL / Params / Headers / Body）发起外部 HTTP 请求。
 * <p>
 * 前置可连接「大模型」或「开始」节点：
 * <ul>
 *   <li>url / 查询参数值 / 请求头 / 请求体均支持 {@code {{变量}}} 引用，运行时渲染为实际值；</li>
 *   <li>参数行的 {@code valueSource=llm} 表示该值由前置大模型（JSON 模式）生成，配置时通过下拉
 *       选择前置节点输出字段，运行时经 {@code {{节点ID.输出名}}} 填入取值（参考知识库/判断节点）；</li>
 *   <li>接口响应文本作为节点输出（默认输出名 response），供下游 {@code {{节点ID.输出名}}} 引用。</li>
 * </ul>
 * 本类同时被 {@code AiWorkflowController#apiNodeTest} 复用：memory 为 null 时视为「Send 测试」场景，
 * 仅剥离变量占位符做连通性测试，不解析执行内存。
 *
 * @author yudao
 */
@Slf4j
@Component
public class ApiNodeHandler implements WorkflowNodeHandler, WorkflowNodeHandlerAdapter {

    private static final String DEFAULT_OUTPUT = "response";
    /** 连接超时（毫秒） */
    private static final int CONNECT_TIMEOUT_MS = 10_000;
    /** 响应超时（毫秒） */
    private static final long RESPONSE_TIMEOUT_MS = 30_000;
    /** 响应体最大缓冲（5MB，防止大响应撑爆内存） */
    private static final int MAX_RESPONSE_SIZE = 5 * 1024 * 1024;

    private final WebClient.Builder webClientBuilder;

    public ApiNodeHandler(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public String[] types() {
        return new String[]{"apiNode", "api"};
    }

    @Override
    public Map<String, Object> execute(WorkflowGraphNode node, WorkflowRunContext ctx, Map<String, Object> memory) {
        JSONObject data = node.getData();
        Map<String, Object> result = sendRequest(data, memory);
        return Map.of(outputNameOf(node), result.get("body"));
    }

    /**
     * 按节点配置发起 HTTP 请求，返回「状态码 + 响应体」（节点执行与前端 Send 测试共用）。
     *
     * @param data   节点配置（method/url/params/headers/bodyType/bodyFormat/rawBody/formData）
     * @param memory 执行内存（用于渲染 {@code {{变量}}}）；为 null 表示测试场景，仅剥离变量占位符
     */
    public Map<String, Object> sendRequest(JSONObject data, Map<String, Object> memory) {
        if (data == null) {
            throw new IllegalArgumentException("API 节点配置为空");
        }
        String url = data.getString("url");
        if (!StringUtils.hasText(url)) {
            throw new IllegalArgumentException("API 节点未配置请求 URL");
        }
        HttpMethod method = parseMethod(data.getString("method"));
        String resolvedUrl = render(url, memory);

        MultiValueMap<String, String> queryParams = buildKeyValues(data.getJSONArray("params"), memory);
        MultiValueMap<String, String> headers = buildKeyValues(data.getJSONArray("headers"), memory);

        String bodyType = data.getString("bodyType");
        String bodyFormat = data.getString("bodyFormat");
        String rawBody = render(data.getString("rawBody"), memory);
        MultiValueMap<String, Object> formData = buildFormData(data.getJSONArray("formData"), memory);

        try {
            WebClient webClient = webClientBuilder.clone()
                    .exchangeStrategies(ExchangeStrategies.builder()
                            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_RESPONSE_SIZE))
                            .build())
                    .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MS)
                            .responseTimeout(Duration.ofMillis(RESPONSE_TIMEOUT_MS))))
                    .build();

            WebClient.RequestBodySpec request = webClient.method(method)
                    .uri(uriBuilder -> {
                        URI uri = URI.create(resolvedUrl);
                        uriBuilder.scheme(uri.getScheme())
                                .host(uri.getHost())
                                .port(uri.getPort())
                                .path(uri.getPath());
                        if (uri.getQuery() != null) {
                            uriBuilder.query(uri.getQuery());
                        }
                        // 追加 Params 标签页配置的查询参数
                        uriBuilder.queryParams(queryParams);
                        return uriBuilder.build();
                    })
                    .headers(httpHeaders -> headers.forEach((key, values) ->
                            values.forEach(value -> httpHeaders.add(key, value))));

            if (isNoneBody(bodyType)) {
                return send(request, null, null, false);
            }
            if (isFormBody(bodyType)) {
                MediaType contentType = "form-data".equalsIgnoreCase(bodyType)
                        ? MediaType.MULTIPART_FORM_DATA
                        : MediaType.APPLICATION_FORM_URLENCODED;
                return send(request, formData, contentType, true);
            }
            // raw / binary：请求体为原始文本
            MediaType contentType = rawContentType(bodyFormat, bodyType);
            return send(request, rawBody, contentType, StringUtils.hasText(rawBody));
        } catch (Exception e) {
            throw new RuntimeException("[API节点] 请求失败: "
                    + (e.getMessage() == null ? e.toString() : e.getMessage()), e);
        }
    }

    /**
     * 提示词注入：当该 API 节点作为大模型节点的下游（承接点）时，
     * 向来源大模型提供「参数生成」提示词模板（列出由大模型填写的参数名），
     * 用户的系统提示词将置于 {@code %s} 处。
     */
    @Override
    public String buildPredecessorSystemPrompt(WorkflowGraphNode self, WorkflowGraphNode predecessor) {
        JSONObject data = self.getData();
        if (data == null) {
            return null;
        }
        List<String> llmKeys = new ArrayList<>();
        collectLlmFilledKeys(data.getJSONArray("params"), llmKeys);
        collectLlmFilledKeys(data.getJSONArray("headers"), llmKeys);
        collectLlmFilledKeys(data.getJSONArray("formData"), llmKeys);
        if (llmKeys.isEmpty()) {
            return null;
        }
        StringBuilder prompt = new StringBuilder();
        prompt.append("你的部分输出将作为后续「API 请求节点」的请求参数值发送给外部接口，请结合用户意图为以下参数生成合理、精准的值：\n");
        for (String key : llmKeys) {
            prompt.append("- ").append(key).append("\n");
        }
        // 模板：前缀 + %s（用户系统提示词）+ 后缀（字段说明取自前驱大模型的 outputDefs）
        String fields = LlmNodeHandler.buildOutputFieldsDescription(
                predecessor == null ? null : predecessor.getData());
        String fieldDesc = fields.isBlank() ? "字段与输出定义一致" : fields;
        return prompt + "%s\n只返回一个合法的 JSON 对象，不要 Markdown 代码块，字段定义为：" + fieldDesc + "\n";
    }

    /** 收集 valueSource=llm 的参数名（用于提示词注入） */
    private static void collectLlmFilledKeys(JSONArray array, List<String> keys) {
        if (array == null) {
            return;
        }
        for (int i = 0; i < array.size(); i++) {
            JSONObject item = array.getJSONObject(i);
            if (item == null || !"llm".equalsIgnoreCase(item.getString("valueSource"))) {
                continue;
            }
            String key = item.getString("key");
            if (StringUtils.hasText(key) && !keys.contains(key)) {
                keys.add(key);
            }
        }
    }

    /** 执行请求：有请求体则携带 content-type 与 body，否则仅带请求头；返回状态码 + 响应体 */
    private static Map<String, Object> send(WebClient.RequestBodySpec request, Object body,
                                            MediaType contentType, boolean hasBody) {
        WebClient.RequestHeadersSpec<?> spec = hasBody
                ? request.contentType(contentType).bodyValue(body)
                : request;
        return spec.exchangeToMono(response -> response.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .map(text -> {
                            Map<String, Object> result = new LinkedHashMap<>();
                            result.put("status", response.statusCode().value());
                            result.put("body", text);
                            return result;
                        }))
                .block();
    }

    /** 查询参数 / 请求头（字符串值） */
    private static MultiValueMap<String, String> buildKeyValues(JSONArray array, Map<String, Object> memory) {
        MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
        if (array == null) {
            return result;
        }
        for (int i = 0; i < array.size(); i++) {
            JSONObject item = array.getJSONObject(i);
            if (item == null || Boolean.FALSE.equals(item.getBoolean("enabled"))) {
                continue;
            }
            String key = item.getString("key");
            if (!StringUtils.hasText(key)) {
                continue;
            }
            result.add(key, render(item.getString("value"), memory));
        }
        return result;
    }

    /** 表单请求体（form-data / x-www-form-urlencoded） */
    private static MultiValueMap<String, Object> buildFormData(JSONArray array, Map<String, Object> memory) {
        MultiValueMap<String, Object> result = new LinkedMultiValueMap<>();
        if (array == null) {
            return result;
        }
        for (int i = 0; i < array.size(); i++) {
            JSONObject item = array.getJSONObject(i);
            if (item == null || Boolean.FALSE.equals(item.getBoolean("enabled"))) {
                continue;
            }
            String key = item.getString("key");
            if (!StringUtils.hasText(key)) {
                continue;
            }
            result.add(key, render(item.getString("value"), memory));
        }
        return result;
    }

    /** 渲染变量：执行场景用执行内存；测试场景（memory 为 null）仅剥离 {{占位符}} 便于连通性测试 */
    private static String render(String text, Map<String, Object> memory) {
        if (text == null) {
            return "";
        }
        if (memory == null) {
            return text.replaceAll("\\{\\{\\s*[^}]+?\\s*}}", "");
        }
        return WorkflowVariableResolver.render(text, memory);
    }

    private static boolean isNoneBody(String bodyType) {
        return bodyType == null || bodyType.isBlank()
                || "none".equalsIgnoreCase(bodyType) || "binary".equalsIgnoreCase(bodyType);
    }

    private static boolean isFormBody(String bodyType) {
        return "form-data".equalsIgnoreCase(bodyType)
                || "x-www-form-urlencoded".equalsIgnoreCase(bodyType);
    }

    private static MediaType rawContentType(String bodyFormat, String bodyType) {
        if ("XML".equalsIgnoreCase(bodyFormat)) {
            return MediaType.APPLICATION_XML;
        }
        if ("Text".equalsIgnoreCase(bodyFormat)) {
            return MediaType.TEXT_PLAIN;
        }
        if ("binary".equalsIgnoreCase(bodyType)) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        return MediaType.APPLICATION_JSON;
    }

    private static HttpMethod parseMethod(String method) {
        if (!StringUtils.hasText(method)) {
            return HttpMethod.GET;
        }
        try {
            return HttpMethod.valueOf(method.trim().toUpperCase());
        } catch (Exception e) {
            return HttpMethod.GET;
        }
    }

    private static String outputNameOf(WorkflowGraphNode node) {
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
