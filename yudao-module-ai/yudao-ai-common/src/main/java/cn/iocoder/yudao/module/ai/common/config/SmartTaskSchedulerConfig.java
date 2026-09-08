package cn.iocoder.yudao.module.ai.common.config;

import cn.iocoder.yudao.module.ai.common.task.SmartTaskScheduler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 通用智能任务调度器配置
 */
@Configuration
public class SmartTaskSchedulerConfig {

    @Value("${iims.smart-task.core-pool-size:6}")
    private int corePoolSize;

    @Value("${iims.smart-task.max-pool-size:16}")
    private int maxPoolSize;

    @Value("${iims.smart-task.queue-capacity:1000}")
    private int queueCapacity;

    @Value("${iims.smart-task.keep-alive-ms:60000}")
    private long keepAliveMs;

    /**
     * 通用智能任务调度器。
     *
     * <h3>用法示例</h3>
     * <pre>{@code
     * @Qualifier("smartTaskScheduler")
     * private SmartTaskScheduler scheduler;
     * scheduler.submit(() -> doWork());
     *
     * // 带优先级和标签
     * scheduler.submit(() -> doWork(), TaskPriority.HIGH, "chat");
     * }</pre>
     */
    @Bean(name = "smartTaskScheduler")
    public SmartTaskScheduler smartTaskScheduler() {
        return new SmartTaskScheduler(
                "smart-task",
                corePoolSize,
                maxPoolSize,
                queueCapacity,
                keepAliveMs
        );
    }
}
