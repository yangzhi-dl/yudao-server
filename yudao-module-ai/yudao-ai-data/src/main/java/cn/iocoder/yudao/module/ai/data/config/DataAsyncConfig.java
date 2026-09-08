package cn.iocoder.yudao.module.ai.data.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 数据采集异步执行器配置。
 * <p>
 * 用于爬虫等耗时采集源的异步执行，避免阻塞请求线程。
 */
@Configuration
public class DataAsyncConfig {

    @Bean("dataCollectExecutor")
    public Executor dataCollectExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("ai-data-collect-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

}
