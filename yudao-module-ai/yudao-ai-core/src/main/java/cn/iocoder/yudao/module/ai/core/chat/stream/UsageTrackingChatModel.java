package cn.iocoder.yudao.module.ai.core.chat.stream;

import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 拦截底层 ChatModel 流式/非流式调用的真实 Token 用量。
 * <p>
 * Agent / Multi-Agent 通过 {@code agent.streamMessages()} 只拿到 {@code Flux<Message>}，
 * 而真实用量只存在于内部 {@link ChatResponse} 上（Agent 框架内部消费后丢弃）。因此包装一层
 * ChatModel，在 {@link #call} / {@link #stream} 内累计每次大模型调用返回的 usage。
 */
public class UsageTrackingChatModel implements ChatModel {

    private final ChatModel delegate;
    private final AtomicLong promptTokens = new AtomicLong();
    private final AtomicLong completionTokens = new AtomicLong();

    public UsageTrackingChatModel(ChatModel delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NonNull ChatResponse call(Prompt prompt) {
        ChatResponse response = delegate.call(prompt);
        accumulate(response);
        return response;
    }

    @Override
    public @NonNull Flux<ChatResponse> stream(Prompt prompt) {
        return delegate.stream(prompt).doOnNext(this::accumulate);
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return delegate.getDefaultOptions();
    }

    private void accumulate(ChatResponse response) {
        if (response == null) {
            return;
        }
        ChatResponseMetadata metadata = response.getMetadata();
        if (metadata == null) {
            return;
        }
        Usage usage = metadata.getUsage();
        // EmptyUsage 的 totalTokens 为 null，只有最后一个带真实用量的 chunk 才会计入
        if (usage != null && usage.getTotalTokens() != null) {
            promptTokens.addAndGet(usage.getPromptTokens() != null ? usage.getPromptTokens() : 0);
            completionTokens.addAndGet(usage.getCompletionTokens() != null ? usage.getCompletionTokens() : 0);
        }
    }

    public long getPromptTokens() {
        return promptTokens.get();
    }

    public long getCompletionTokens() {
        return completionTokens.get();
    }

    public long getTotalTokens() {
        return promptTokens.get() + completionTokens.get();
    }
}