package cn.iocoder.yudao.module.ai.common.config;

import cn.iocoder.yudao.module.ai.common.model.entity.ConversionResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;

@Configuration
public class QueueConfig {
    
    @Bean
    public ConcurrentMap<String, BlockingQueue<ConversionResult>> documentResultQueues() {
        return new ConcurrentHashMap<>();
    }
    
    @Bean
    public Semaphore documentConversionSemaphore() {
        return new Semaphore(5); // 限制同时转换的文档数，降低内存压力
    }
    
    @Bean
    public Semaphore markdownConversionSemaphore() {
        return new Semaphore(5); // 限制同时调用markdown API的数量，降低内存压力
    }

    /**
     * 通用并发限制信号量，可用于限制任意类型任务的并发数，
     * 配合 {@link cn.iocoder.yudao.module.ai.common.task.SmartTaskScheduler} 使用。
     */
    @Value("${iims.smart-task.max-concurrency:10}")
    private int maxConcurrency;

    @Bean
    public Semaphore generalTaskSemaphore() {
        return new Semaphore(maxConcurrency);
    }
}