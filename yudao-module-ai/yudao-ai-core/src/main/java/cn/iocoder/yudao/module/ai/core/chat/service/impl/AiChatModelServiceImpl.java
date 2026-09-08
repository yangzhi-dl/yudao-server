package cn.iocoder.yudao.module.ai.core.chat.service.impl;

import cn.iocoder.yudao.module.ai.core.chat.model.entity.ChatApi;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ModelChatOptions;
import cn.iocoder.yudao.module.ai.core.chat.service.AiChatModelService;
import cn.iocoder.yudao.module.ai.core.chat.service.AiModelService;
import cn.iocoder.yudao.module.ai.core.chat.utils.AESEncryptionUtil;
import cn.iocoder.yudao.module.ai.core.chat.overwrite.LocalOpenAiApi;
import io.netty.channel.ChannelOption;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Objects;

@Service
public class AiChatModelServiceImpl implements AiChatModelService {

    public final AiModelService aiModelService;
    private final RestClient.Builder restClientBuilder;
    private final WebClient.Builder webClientBuilder;

    public AiChatModelServiceImpl(
            AiModelService aiModelService,
            @Value("${iims.model.rest-client.connect-timeout:3600}") Integer connectTimeout,
            @Value("${iims.model.rest-client.read-timeout:3600}") Integer readTimeout,
            @Value("${iims.model.web-client.response-timeout:1800}") Integer webClientResponseTimeout) {
        this.aiModelService = aiModelService;
        this.restClientBuilder = RestClient.builder()
                .requestFactory(new SimpleClientHttpRequestFactory() {{
                    setConnectTimeout(Duration.ofSeconds(connectTimeout));
                    setReadTimeout(Duration.ofSeconds(readTimeout));
                }});
        // 流式 SSE 请求（AgentNode/LLMNode 均走此路径）使用独立的 WebClient（Reactor Netty）。
        // 配置响应超时，防止长时间思考/长文本生成时连接被提前关闭（PrematureCloseException）。
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout * 1000)
                .responseTimeout(Duration.ofSeconds(webClientResponseTimeout));
        this.webClientBuilder = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    @Override
    public OllamaChatModel getOllamaChatModel(OllamaApi ollamaApi, OllamaChatOptions ollamaOptions) {
        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(ollamaOptions).build();
    }

    @Override
    public OpenAiChatModel getOpenAIChatModel(OpenAiApi openAiApi, OpenAiChatOptions openAiOptions) {
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(openAiOptions).build();
    }

    @Override
    public ChatModel getDeepSeekChatModel(DeepSeekApi deepSeekApi, DeepSeekChatOptions deepSeekChatOptions) {
        return DeepSeekChatModel.builder()
                .deepSeekApi(deepSeekApi)
                .defaultOptions(deepSeekChatOptions).build();
    }

    @Override
    public ChatModel getChatModel(Long modelId, ModelChatOptions options) {
        ChatApi chatApi = aiModelService.selectChatModelApiById(modelId);
        Integer maxTokens = options.getMaxTokens();
        if (Objects.isNull(maxTokens)) {
            maxTokens = chatApi.getToken();
        }
        return switch (chatApi.getType()) {
            case AGENT, MULTI_AGENT, WORKFLOW -> null;
            case OLLAMA -> getOllamaChatModel(OllamaApi.builder().restClientBuilder(restClientBuilder).baseUrl(chatApi.getUrl()).build(),
                    OllamaChatOptions.builder().model(chatApi.getName()).topP(options.getTopP())
                            .temperature(options.getTemperature()).frequencyPenalty(options.getFrequencyPenalty())
                            .numCtx(maxTokens).presencePenalty(options.getPresencePenalty()).build());
            case OPENAI -> getOpenAIChatModel(LocalOpenAiApi.myBuilder().restClientBuilder(restClientBuilder)
                    .webClientBuilder(webClientBuilder)
                    .apiKey(Objects.requireNonNullElse(AESEncryptionUtil.decrypt(chatApi.getKey()), ""))
                    .baseUrl(chatApi.getUrl()).build(), OpenAiChatOptions.builder().model(chatApi.getName()).topP(options.getTopP())
                    .temperature(options.getTemperature()).frequencyPenalty(options.getFrequencyPenalty())
                    .maxTokens(maxTokens).presencePenalty(options.getPresencePenalty()).build());
            case DEEPSEEK -> getDeepSeekChatModel(DeepSeekApi.builder().restClientBuilder(restClientBuilder)
                    .webClientBuilder(webClientBuilder)
                    .apiKey(Objects.requireNonNullElse(AESEncryptionUtil.decrypt(chatApi.getKey()), ""))
                    .baseUrl(chatApi.getUrl()).build(), DeepSeekChatOptions.builder().model(chatApi.getName()).topP(options.getTopP())
                    .temperature(options.getTemperature()).frequencyPenalty(options.getFrequencyPenalty())
                    .maxTokens(maxTokens).presencePenalty(options.getPresencePenalty()).build());
        };
    }

    @Override
    public EmbeddingModel getOllamaEmbeddingModel(OllamaApi ollamaApi, OllamaEmbeddingOptions ollamaOptions) {
        return OllamaEmbeddingModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(ollamaOptions).build();
    }

    @Override
    public EmbeddingModel getOpenAiEmbeddingModel(OpenAiApi openAiApi, MetadataMode metadataMode,
                                                  OpenAiEmbeddingOptions openAiOptions) {
        return new OpenAiEmbeddingModel(openAiApi, metadataMode, openAiOptions);
    }

    @Override
    public EmbeddingModel getEmbeddingModel(Long modelId, MetadataMode metadataMode) {
        ChatApi chatApi = aiModelService.selectChatModelApiById(modelId);
        return switch (chatApi.getType()) {
            case AGENT, MULTI_AGENT, DEEPSEEK, WORKFLOW -> null;
            case OLLAMA -> getOllamaEmbeddingModel(OllamaApi.builder()
                    .baseUrl(chatApi.getUrl()).restClientBuilder(restClientBuilder).build(), OllamaEmbeddingOptions.builder()
                    .model(chatApi.getName()).build());
            case OPENAI -> getOpenAiEmbeddingModel(OpenAiApi.builder().restClientBuilder(restClientBuilder)
                    .apiKey(Objects.requireNonNull(AESEncryptionUtil.decrypt(chatApi.getKey())))
                    .baseUrl(chatApi.getUrl()).build(), metadataMode, OpenAiEmbeddingOptions.builder()
                    .model(chatApi.getName()).dimensions(2048).build());
        };
    }

}
