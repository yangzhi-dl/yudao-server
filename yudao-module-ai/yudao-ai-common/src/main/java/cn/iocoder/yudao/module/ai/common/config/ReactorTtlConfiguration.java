package cn.iocoder.yudao.module.ai.common.config;

import com.alibaba.ttl.TtlRunnable;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;

/**
 * Reactor 调度器 TTL 装饰器配置。
 * <p>
 * 确保 Reactor 流回调线程（如 openai-stream-handler-thread-*）能自动继承
 * {@link com.alibaba.ttl.TransmittableThreadLocal} 上下文（如租户 ID），
 * 避免跨线程丢失上下文的 NPE。
 */
@Configuration
public class ReactorTtlConfiguration {

    static {
        Schedulers.onScheduleHook("ttl", task -> TtlRunnable.get(Objects.requireNonNull(task)));
    }
}
