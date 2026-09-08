package cn.iocoder.yudao.module.ai.core.chat.overwrite;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.model.ApiKey;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.common.OpenAiApiConstants;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * 扩展 {@link OpenAiApi}，通过 Jackson Mixin 在不修改源码的情况下为
 * {@link OpenAiApi.ChatCompletionMessage} 的 {@code reasoningContent} 字段
 * 增加 {@code @JsonAlias("reasoning")} 支持。
 *
 * <p>DeepSeek-R1 等模型在流式 SSE 中以 {@code "reasoning_content"} 返回思考过程，
 * 部分兼容模型使用 {@code "reasoning"}；Mixin 让两种字段名都能映射到同一个字段，
 * 最终出现在 {@code AssistantMessage.getMetadata().get("reasoningContent")} 中。
 *
 * <h3>核心原理</h3>
 * <ul>
 *   <li>流式响应：{@link OpenAiApi#chatCompletionStream} 内部通过
 *       {@link ModelOptionsUtils#jsonToObject} 反序列化，所使用的
 *       {@link ModelOptionsUtils#OBJECT_MAPPER} 是 {@code public static final}
 *       可变对象，在静态块中注册 Mixin 即可生效。</li>
 *   <li>非流式响应：{@link OpenAiApi#chatCompletionEntity} 通过 {@link RestClient}
 *       反序列化，使用的是框架自己的 ObjectMapper，不会带上本 Mixin；如需覆盖须在
 *       {@code RestClient.Builder} 上注入携带 Mixin 的转换器。</li>
 * </ul>
 *
 * <p>仅覆盖流式路径（本项目工作流/对话均走流式），不影响原生 {@link OpenAiApi}。
 *
 * @see OpenAiApi#chatCompletionStream
 */
public class LocalOpenAiApi extends OpenAiApi {

    // -----------------------------------------------------------------------
    // Jackson Mixin
    // -----------------------------------------------------------------------

    /**
     * 为 {@link OpenAiApi.ChatCompletionMessage} 追加注解，无需修改源码。
     * 使用带 {@code @JsonCreator} 的抽象构造函数覆盖记录（record）canonical
     * constructor 上的注解；参数列表与原始 record 完全一致，仅在
     * {@code reasoningContent} 上额外添加 {@code @JsonAlias("reasoning")}。
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    abstract static class ChatCompletionMessageMixin {
        @JsonCreator
        ChatCompletionMessageMixin(
                @JsonProperty("content") Object rawContent,
                @JsonProperty("role") OpenAiApi.ChatCompletionMessage.Role role,
                @JsonProperty("name") String name,
                @JsonProperty("tool_call_id") String toolCallId,
                @JsonProperty("tool_calls")
                @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                List<OpenAiApi.ChatCompletionMessage.ToolCall> toolCalls,
                @JsonProperty("refusal") String refusal,
                @JsonProperty("audio") OpenAiApi.ChatCompletionMessage.AudioOutput audioOutput,
                @JsonProperty("annotations") List<OpenAiApi.ChatCompletionMessage.Annotation> annotations,
                // 新增 @JsonAlias("reasoning")，同时支持 DeepSeek 等模型的 "reasoning" 字段名
                @JsonProperty("reasoning_content") @JsonAlias("reasoning") String reasoningContent
        ) {
        }
    }

    // -----------------------------------------------------------------------
    // 类加载时注册 Mixin（覆盖流式路径）
    // -----------------------------------------------------------------------

    static {
        // chatCompletionStream() 使用 ModelOptionsUtils.OBJECT_MAPPER 做 SSE 反序列化
        // 该字段是 public static final，但 ObjectMapper 实例本身可变，可直接注册 Mixin
        ModelOptionsUtils.OBJECT_MAPPER.addMixIn(
                OpenAiApi.ChatCompletionMessage.class,
                ChatCompletionMessageMixin.class
        );
    }

    // -----------------------------------------------------------------------
    // 构造函数（私有，通过 myBuilder() 创建）
    // -----------------------------------------------------------------------

    private LocalOpenAiApi(String baseUrl, ApiKey apiKey, HttpHeaders headers,
                           String completionsPath, String embeddingsPath,
                           RestClient.Builder restClientBuilder, WebClient.Builder webClientBuilder,
                           ResponseErrorHandler responseErrorHandler) {
        super(baseUrl, apiKey, headers, completionsPath, embeddingsPath,
                restClientBuilder, webClientBuilder, responseErrorHandler);
    }

    // -----------------------------------------------------------------------
    // Builder（命名为 myBuilder() 避免与父类 builder() 冲突）
    // -----------------------------------------------------------------------

    /**
     * 创建 {@link LocalOpenAiApi} 的 Builder。
     * 命名为 {@code myBuilder()} 以避开父类 {@link OpenAiApi#builder()} 的返回类型。
     */
    public static Builder myBuilder() {
        return new Builder();
    }

    public static final class Builder {

        private String baseUrl = OpenAiApiConstants.DEFAULT_BASE_URL;
        private ApiKey apiKey;
        private final HttpHeaders headers = new HttpHeaders();
        private String completionsPath = "/v1/chat/completions";
        private String embeddingsPath = "/v1/embeddings";
        private RestClient.Builder restClientBuilder = RestClient.builder();
        private WebClient.Builder webClientBuilder = WebClient.builder();
        private ResponseErrorHandler responseErrorHandler = RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER;

        public Builder baseUrl(String baseUrl) {
            Assert.hasText(baseUrl, "baseUrl cannot be null or empty");
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = new SimpleApiKey(apiKey);
            return this;
        }

        public Builder apiKey(ApiKey apiKey) {
            Assert.notNull(apiKey, "apiKey cannot be null");
            this.apiKey = apiKey;
            return this;
        }

        public Builder completionsPath(String completionsPath) {
            Assert.hasText(completionsPath, "completionsPath cannot be null or empty");
            this.completionsPath = completionsPath;
            return this;
        }

        public Builder embeddingsPath(String embeddingsPath) {
            Assert.hasText(embeddingsPath, "embeddingsPath cannot be null or empty");
            this.embeddingsPath = embeddingsPath;
            return this;
        }

        public Builder restClientBuilder(RestClient.Builder restClientBuilder) {
            Assert.notNull(restClientBuilder, "restClientBuilder cannot be null");
            this.restClientBuilder = restClientBuilder;
            return this;
        }

        public Builder webClientBuilder(WebClient.Builder webClientBuilder) {
            Assert.notNull(webClientBuilder, "webClientBuilder cannot be null");
            this.webClientBuilder = webClientBuilder;
            return this;
        }

        public Builder responseErrorHandler(ResponseErrorHandler responseErrorHandler) {
            Assert.notNull(responseErrorHandler, "responseErrorHandler cannot be null");
            this.responseErrorHandler = responseErrorHandler;
            return this;
        }

        public LocalOpenAiApi build() {
            Assert.notNull(this.apiKey, "apiKey must be set");
            return new LocalOpenAiApi(baseUrl, apiKey, headers, completionsPath, embeddingsPath,
                    restClientBuilder, webClientBuilder, responseErrorHandler);
        }
    }
}