package cn.iocoder.yudao.module.ai.core.tools.callback;

import cn.iocoder.yudao.module.ai.core.chat.model.entity.HttpParameterDef;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.HttpRequestConfig;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.HttpToolConfig;
import com.alibaba.fastjson.JSON;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import io.netty.channel.ChannelOption;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.ai.tool.metadata.DefaultToolMetadata;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpToolCallback implements ToolCallback {

    private final HttpToolConfig config;
    private final ToolDefinition toolDefinition;
    private final ToolMetadata toolMetadata;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public HttpToolCallback(HttpToolConfig config, WebClient.Builder webClientBuilder) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        HttpRequestConfig requestConfig = config.getRequestConfig();
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .responseTimeout(Duration.ofMillis(requestConfig.getTimeout()));
        this.webClient = webClientBuilder.exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs()
                                .maxInMemorySize(1024 * 1024))
                        .build())
                .baseUrl(this.config.getRequestConfig().getBaseUrl())
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .clientConnector(new ReactorClientHttpConnector(httpClient)).build();

        this.toolDefinition = DefaultToolDefinition.builder()
                .name(config.getName()).description(config.getDescription())
                .inputSchema(buildInputSchema(this.config.getParameters()))
                .build();

        this.toolMetadata = DefaultToolMetadata.builder().build();
    }

    @NotNull
    @Override
    public String call(@NotNull String toolInput) {
        try {
            // 解析输入参数
            Map<String, Object> arguments = objectMapper.readValue(
                    toolInput,
                    new TypeReference<>() {}
            );

            String url = config.getRequestConfig().getPath();
            HttpMethod method = HttpMethod.valueOf(config.getRequestConfig().getMethod().toUpperCase());

            String response = executeHttpRequest(method, url, arguments);
            return formatResponse(response);

        } catch (Exception e) {
            return "调用HTTP工具失败: " + e.getMessage();
        }
    }

    private String buildInputSchema(List<HttpParameterDef> parameters) {
        String emptySchema = "{\"$schema\":\"https://json-schema.org/draft/2020-12/schema\",\"type\":\"object\",\"properties\":{},\"additionalProperties\":false}";

        if (parameters == null || parameters.isEmpty()) {
            return emptySchema;
        }

        try {
            Map<String, Object> schema = new HashMap<>();
            schema.put("$schema", "https://json-schema.org/draft/2020-12/schema");
            schema.put("type", "object");

            Map<String, Object> properties = new HashMap<>();
            List<String> required = new ArrayList<>();

            for (HttpParameterDef param : parameters) {
                Map<String, Object> paramSchema = new HashMap<>();
                paramSchema.put("type", getJsonType(param.getType()));
                paramSchema.put("description", param.getDescription() != null ? param.getDescription() : "");

                // 添加枚举值
                if (param.getEnumValues() != null && !param.getEnumValues().isEmpty()) {
                    paramSchema.put("enum", param.getEnumValues());
                }

                properties.put(param.getName(), paramSchema);

                if (Boolean.TRUE.equals(param.getRequired())) {
                    required.add(param.getName());
                }
            }

            schema.put("properties", properties);

            if (!required.isEmpty()) {
                schema.put("required", required);
            }

            schema.put("additionalProperties", false);

            return objectMapper.writeValueAsString(schema);
        } catch (Exception e) {
            return emptySchema;
        }
    }

    private String getJsonType(String type) {
        if (type == null) return "string";

        return switch (type.toLowerCase()) {
            case "integer", "long", "int", "short", "byte" -> "integer";
            case "number", "double", "float", "decimal" -> "number";
            case "boolean", "bool" -> "boolean";
            case "array", "list", "set" -> "array";
            case "object", "map" -> "object";
            default -> "string";
        };
    }

    private String executeHttpRequest(HttpMethod method, String url, Map<String, Object> args) {
        Map<String, Object> resolvedArgs = applyDefaultValues(args);

        Set<String> usedPathParams = new HashSet<>();

        WebClient.RequestBodySpec requestSpec = webClient.method(method)
                .uri(uriBuilder -> {
                    String resolvedUrl = resolvePathParams(url, resolvedArgs, usedPathParams);
                    Map<String, Object> remainingArgs = getRemainingArgs(resolvedArgs, usedPathParams);

                    // 处理 queryParams 映射（用于 GET 请求）
                    if (method == HttpMethod.GET) {
                        MultiValueMap<String, String> queryParams = buildQueryParamsWithMapping(remainingArgs);
                        return uriBuilder.path(resolvedUrl)
                                .queryParams(queryParams)
                                .build();
                    } else {
                        return uriBuilder.path(resolvedUrl).build();
                    }
                });

        // 添加配置中的 headers
        if (config.getRequestConfig().getHeaders() != null) {
            config.getRequestConfig().getHeaders().forEach(requestSpec::header);
        }

        // 添加标记为 header 的参数
        addHeaderParams(requestSpec, resolvedArgs, usedPathParams);

        // 处理 POST/PUT/PATCH 请求体
        if (method != HttpMethod.GET && method != HttpMethod.DELETE) {
            Map<String, Object> remainingForBody = getRemainingArgs(resolvedArgs, usedPathParams);

            // 根据 contentType 决定请求体格式
            String contentType = config.getRequestConfig().getContentType();

            if (contentType != null && contentType.contains("multipart/form-data")) {
                // 处理表单数据
                MultiValueMap<String, Object> formData = buildFormDataWithMapping(remainingForBody);
                if (!formData.isEmpty()) {
                    requestSpec.contentType(MediaType.MULTIPART_FORM_DATA);
                    requestSpec.bodyValue(formData);
                }
            } else {
                // 处理 JSON 请求体
                Object body = buildRequestBodyWithMapping(remainingForBody);
                if (body != null) {
                    requestSpec.contentType(MediaType.APPLICATION_JSON);
                    requestSpec.bodyValue(body);
                }
            }
        }

        return requestSpec
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("HTTP错误 " + response.statusCode() + ": " + errorBody))))
                .bodyToMono(String.class)
                .block();
    }

    /**
     * 应用参数的默认值
     */
    private Map<String, Object> applyDefaultValues(Map<String, Object> args) {
        Map<String, Object> inputArgs = args != null ? args : Collections.emptyMap();
        Map<String, Object> result = new HashMap<>(inputArgs);

        if (config.getParameters() != null) {
            for (HttpParameterDef param : config.getParameters()) {
                String paramName = param.getName();

                if (!result.containsKey(paramName) || result.get(paramName) == null) {
                    Object defaultValue = getDefaultValue(param);
                    if (defaultValue != null) {
                        result.put(paramName, defaultValue);
                    }
                }
            }
        }

        return result;
    }

    /**
     * 获取参数的默认值
     */
    private Object getDefaultValue(HttpParameterDef param) {
        if (param.getDefaultValue() != null) {
            return convertToType(param.getDefaultValue().toString(), param.getType());
        }
        return null;
    }

    /**
     * 将字符串值转换为指定类型
     */
    private Object convertToType(String value, String targetType) {
        if (value == null || targetType == null) {
            return value;
        }

        try {
            return switch (targetType.toLowerCase()) {
                case "integer", "long", "int", "short", "byte" -> Long.parseLong(value);
                case "number", "double", "float", "decimal" -> Double.parseDouble(value);
                case "boolean", "bool" -> Boolean.parseBoolean(value);
                default -> value;
            };
        } catch (NumberFormatException e) {
            return value;
        }
    }

    /**
     * 构建请求体（支持嵌套映射）
     */
    private Object buildRequestBodyWithMapping(Map<String, Object> args) {
        if (config.getRequestMapping() == null) {
            return args.isEmpty() ? null : args;
        }

        // 如果 useFullParamsAsBody 为 true，返回所有参数
        if (Boolean.TRUE.equals(config.getRequestMapping().getUseFullParamsAsBody())) {
            return args.isEmpty() ? null : args;
        }

        // 处理 body 映射
        Map<String, Object> bodyMapping = config.getRequestMapping().getBody();
        if (bodyMapping != null && !bodyMapping.isEmpty()) {
            return resolveNestedObject(bodyMapping, args);
        }

        return args.isEmpty() ? null : args;
    }

    /**
     * 构建表单数据（支持映射）
     */
    private MultiValueMap<String, Object> buildFormDataWithMapping(Map<String, Object> args) {
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();

        if (config.getRequestMapping() == null) {
            // 没有映射配置，直接使用所有参数
            for (Map.Entry<String, Object> entry : args.entrySet()) {
                if (entry.getValue() != null) {
                    formData.add(entry.getKey(), entry.getValue());
                }
            }
            return formData;
        }

        // 处理 formParams 映射
        Map<String, Object> formParamsMapping = config.getRequestMapping().getFormParams();
        if (formParamsMapping != null && !formParamsMapping.isEmpty()) {
            for (Map.Entry<String, Object> entry : formParamsMapping.entrySet()) {
                String targetKey = entry.getKey();
                String sourceExpr = JSON.toJSONString(entry.getValue());
                Object value = resolveExpression(sourceExpr, args);
                if (value != null) {
                    formData.add(targetKey, value);
                }
            }
        }

        // 如果 useFullParamsAsBody 为 true，添加未使用的参数
        if (Boolean.TRUE.equals(config.getRequestMapping().getUseFullParamsAsBody())) {
            for (Map.Entry<String, Object> entry : args.entrySet()) {
                if (entry.getValue() != null && !formData.containsKey(entry.getKey())) {
                    formData.add(entry.getKey(), entry.getValue());
                }
            }
        }

        return formData;
    }

    /**
     * 构建查询参数（支持映射）
     */
    private MultiValueMap<String, String> buildQueryParamsWithMapping(Map<String, Object> args) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();

        if (config.getRequestMapping() == null) {
            // 没有映射配置，使用 location 为 query 的参数
            List<HttpParameterDef> queryParameters = getParametersByLocation("query");
            if (queryParameters.isEmpty()) {
                for (Map.Entry<String, Object> entry : args.entrySet()) {
                    if (entry.getValue() != null) {
                        queryParams.add(entry.getKey(), String.valueOf(entry.getValue()));
                    }
                }
            } else {
                for (HttpParameterDef param : queryParameters) {
                    Object value = args.get(param.getName());
                    if (value != null) {
                        queryParams.add(param.getName(), String.valueOf(value));
                    }
                }
            }
            return queryParams;
        }

        // 处理 queryParams 映射
        Map<String, String> queryParamsMapping = config.getRequestMapping().getQueryParams();
        if (queryParamsMapping != null && !queryParamsMapping.isEmpty()) {
            for (Map.Entry<String, String> entry : queryParamsMapping.entrySet()) {
                String targetKey = entry.getKey();
                String sourceExpr = entry.getValue();
                Object value = resolveExpression(sourceExpr, args);
                if (value != null) {
                    queryParams.add(targetKey, String.valueOf(value));
                }
            }
        }

        return queryParams;
    }

    /**
     * 递归解析嵌套对象
     */
    @SuppressWarnings("unchecked")
    private Object resolveNestedObject(Object mapping, Map<String, Object> args) {
        if (mapping instanceof Map) {
            Map<String, Object> sourceMap = (Map<String, Object>) mapping;
            Map<String, Object> resultMap = new LinkedHashMap<>();

            for (Map.Entry<String, Object> entry : sourceMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof String) {
                    // 解析表达式
                    Object resolved = resolveExpression((String) value, args);
                    if (resolved != null) {
                        resultMap.put(key, resolved);
                    }
                } else {
                    // 递归处理嵌套对象
                    Object nested = resolveNestedObject(value, args);
                    if (nested != null) {
                        resultMap.put(key, nested);
                    }
                }
            }

            return resultMap.isEmpty() ? null : resultMap;
        } else if (mapping instanceof List) {
            List<Object> sourceList = (List<Object>) mapping;
            List<Object> resultList = new ArrayList<>();

            for (Object item : sourceList) {
                Object resolved = resolveNestedObject(item, args);
                if (resolved != null) {
                    resultList.add(resolved);
                }
            }

            return resultList.isEmpty() ? null : resultList;
        }

        return mapping;
    }

    /**
     * 解析表达式，支持 ${paramName} 格式
     */
    private Object resolveExpression(String expression, Map<String, Object> args) {
        if (expression == null) {
            return null;
        }

        // 先检查是否是单一的 ${paramName} 表达式（用于返回原始类型）
        Pattern singleExprPattern = Pattern.compile("^\\$\\{([^}]+)}$");
        Matcher singleExprMatcher = singleExprPattern.matcher(expression);
        if (singleExprMatcher.matches()) {
            String paramName = singleExprMatcher.group(1);
            return args.get(paramName);  // 返回原始对象类型，保持类型一致性
        }

        // 处理包含多个表达式或混合文本的情况
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)}");
        Matcher matcher = pattern.matcher(expression);
        StringBuilder result = new StringBuilder();
        boolean hasExpression = false;

        while (matcher.find()) {
            hasExpression = true;
            String paramName = matcher.group(1);
            Object value = args.get(paramName);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return hasExpression ? result.toString() : expression;
    }

    private String resolvePathParams(String url, Map<String, Object> args, Set<String> usedPathParams) {
        String resolvedUrl = url;

        List<String> pathParamNames = getParametersByLocation("path").stream()
                .map(HttpParameterDef::getName)
                .toList();

        for (String paramName : pathParamNames) {
            Object value = args.get(paramName);
            if (value != null) {
                String placeholder = "{" + paramName + "}";
                if (resolvedUrl.contains(placeholder)) {
                    resolvedUrl = resolvedUrl.replace(placeholder, String.valueOf(value));
                    usedPathParams.add(paramName);
                }
            }
        }

        Matcher matcher = Pattern.compile("\\{([^}]+)}").matcher(resolvedUrl);
        while (matcher.find()) {
            String paramName = matcher.group(1);
            Object value = args.get(paramName);
            if (value != null && !usedPathParams.contains(paramName)) {
                resolvedUrl = resolvedUrl.replace("{" + paramName + "}", String.valueOf(value));
                usedPathParams.add(paramName);
            }
        }

        return resolvedUrl;
    }

    private List<HttpParameterDef> getParametersByLocation(String location) {
        if (config.getParameters() == null) return List.of();
        return config.getParameters().stream()
                .filter(p -> p.getLocation() != null && location.equalsIgnoreCase(p.getLocation()))
                .toList();
    }

    private Map<String, Object> getRemainingArgs(Map<String, Object> args, Set<String> excludeKeys) {
        Map<String, Object> remaining = new HashMap<>(args);
        remaining.keySet().removeAll(excludeKeys);
        return remaining;
    }

    private void addHeaderParams(WebClient.RequestBodySpec requestSpec, Map<String, Object> args, Set<String> usedPathParams) {
        List<HttpParameterDef> headerParameters = getParametersByLocation("header");

        for (HttpParameterDef param : headerParameters) {
            Object value = args.get(param.getName());
            if (value != null) {
                requestSpec.header(param.getName(), String.valueOf(value));
                usedPathParams.add(param.getName());
            }
        }
    }

    private String formatResponse(String rawResponseBody) {
        if (rawResponseBody == null || rawResponseBody.trim().isEmpty()) {
            return "响应为空";
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(rawResponseBody);
            // 清洗HTML并移除超大字段
            JsonNode cleanedNode = removeLargeFields(jsonNode);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(cleanedNode);
        } catch (Exception e) {
            // 如果不是JSON，则当作HTML字符串清洗
            return extractMainContent(rawResponseBody);
        }
    }

    private JsonNode removeLargeFields(JsonNode node) {
        final int MAX_FIELD_SIZE = 10240; // 10KB，超过此大小的字段将被移除

        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;

            // 兼容 tools.jackson：通过 ObjectMapper 转换为 Map 遍历
            Map<String, JsonNode> fieldMap = objectMapper.convertValue(
                    objectNode, new TypeReference<>() {
                    });
            for (Map.Entry<String, JsonNode> entry : fieldMap.entrySet()) {
                String fieldName = entry.getKey();
                JsonNode childNode = entry.getValue();

                if (childNode.isString()) {
                    String text = childNode.asString();

                    // 检查字段大小，超大则移除
                    if (text.length() > MAX_FIELD_SIZE) {
                        objectNode.remove(fieldName);
                    } else {
                        // 清洗HTML标签
                        String cleanedText = extractMainContent(text);
                        objectNode.put(fieldName, cleanedText);
                    }
                } else if (childNode.isObject() || childNode.isArray()) {
                    // 递归处理嵌套对象和数组
                    JsonNode processedNode = removeLargeFields(childNode);
                    objectNode.set(fieldName, processedNode);
                }
            }
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode processedNode = removeLargeFields(arrayNode.get(i));
                arrayNode.set(i, processedNode);
            }
        }

        return node;
    }

    private String extractMainContent(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        Document doc = Jsoup.parse(html);

        // 移除噪声元素
        doc.select("script, style, head, title, meta, nav, footer, header, aside, iframe, noscript, svg, canvas, form, .ad, .advertisement, #sidebar, .nav, .footer, .header, .fenye")
                .remove();

        // 智能定位正文区域（优先级：article > main > content > body）
        Element contentElement = doc.selectFirst("article, main, .content, #content, .post, .article, [role=main]");

        // 提取文本并清理
        String text = (contentElement != null ? contentElement : doc.body()).text();

        // 清理多余空白
        return text.replaceAll("\\s+", " ").trim();
    }

    @NotNull
    @Override
    public ToolDefinition getToolDefinition() {
        return this.toolDefinition;
    }

    @NotNull
    @Override
    public ToolMetadata getToolMetadata() {
        return this.toolMetadata;
    }
}