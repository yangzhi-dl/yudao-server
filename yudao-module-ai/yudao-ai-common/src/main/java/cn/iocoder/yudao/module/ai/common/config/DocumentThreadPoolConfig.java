package cn.iocoder.yudao.module.ai.common.config;

import cn.iocoder.yudao.module.ai.common.model.entity.ConversionResult;
import cn.iocoder.yudao.module.ai.common.model.entity.DocumentTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Comparator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 文档转换线程池配置类
 * 用于配置文档转换和Markdown转换的线程池及任务队列
 *
 * @author system
 */
@Configuration
public class DocumentThreadPoolConfig {

    // ==================== 文档转换线程池配置 ====================

    /**
     * 文档转换线程池核心线程数
     */
    @Value("${iims.document.parser.office-conversion.core-pool-size:3}")
    private int docCorePoolSize;

    /**
     * 文档转换线程池最大线程数
     */
    @Value("${iims.document.parser.office-conversion.max-pool-size:6}")
    private int docMaxPoolSize;

    /**
     * 文档转换线程池队列容量
     */
    @Value("${iims.document.parser.office-conversion.queue-capacity:200}")
    private int docQueueCapacity;

    /**
     * 文档转换线程池空闲线程存活时间（秒）
     */
    @Value("${iims.document.parser.office-conversion.keep-alive-seconds:60}")
    private int docKeepAliveSeconds;

    /**
     * 文档转换线程池线程名称前缀
     */
    @Value("${iims.document.parser.office-conversion.thread-name-prefix:office-conversion-}")
    private String docThreadNamePrefix;

    // ==================== Markdown转换线程池配置 ====================

    /**
     * Markdown转换线程池核心线程数
     */
    @Value("${iims.document.parser.md-conversion.core-pool-size:3}")
    private int mdCorePoolSize;

    /**
     * Markdown转换线程池最大线程数
     */
    @Value("${iims.document.parser.md-conversion.max-pool-size:5}")
    private int mdMaxPoolSize;

    /**
     * Markdown转换线程池队列容量
     */
    @Value("${iims.document.parser.md-conversion.queue-capacity:100}")
    private int mdQueueCapacity;

    /**
     * Markdown转换线程池空闲线程存活时间（秒）
     */
    @Value("${iims.document.parser.md-conversion.keep-alive-seconds:60}")
    private int mdKeepAliveSeconds;

    /**
     * Markdown转换线程池线程名称前缀
     */
    @Value("${iims.document.parser.md-conversion.thread-name-prefix:md-conversion-}")
    private String mdThreadNamePrefix;

    // ==================== 队列配置 ====================

    /**
     * 文档任务优先队列容量
     */
    @Value("${iims.document.parser.document-queue.capacity:500}")
    private int documentQueueCapacity;

    /**
     * Markdown任务结果队列容量
     */
    @Value("${iims.document.parser.markdown-queue.capacity:50}")
    private int markdownQueueCapacity;

    // ==================== 拒绝策略配置 ====================

    /**
     * 文档转换线程池拒绝策略
     * 可选值：CallerRunsPolicy, AbortPolicy, DiscardPolicy, DiscardOldestPolicy
     */
    @Value("${iims.document.parser.office-conversion.rejected-policy:CallerRunsPolicy}")
    private String docRejectedPolicy;

    /**
     * Markdown转换线程池拒绝策略
     * 可选值：CallerRunsPolicy, AbortPolicy, DiscardPolicy, DiscardOldestPolicy
     */
    @Value("${iims.document.parser.md-conversion.rejected-policy:CallerRunsPolicy}")
    private String mdRejectedPolicy;

    /**
     * 文档转换线程池
     * 用于处理通用文档格式转换任务（如Word、Excel、PPT等转PDF或图片）
     * <p>
     * 线程池参数说明：
     * - corePoolSize: 核心线程数，即使空闲也会保持的线程数量
     * - maxPoolSize: 最大线程数，当队列满时允许创建的最大线程数
     * - queueCapacity: 队列容量，用于存放等待执行的任务
     * - keepAliveSeconds: 线程空闲存活时间，超过核心线程数的空闲线程在此时间后会被销毁
     * - threadNamePrefix: 线程名称前缀，便于日志追踪和问题排查
     * - rejectedExecutionHandler: 拒绝策略，当线程池和队列都满时的处理方式
     *
     * @return 配置好的文档转换线程池
     */
    @Bean(name = "documentConversionExecutor")
    public ThreadPoolTaskExecutor documentConversionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数：从配置文件读取，默认10个常驻线程
        executor.setCorePoolSize(docCorePoolSize);

        // 最大线程数：从配置文件读取，默认最多50个线程
        executor.setMaxPoolSize(docMaxPoolSize);

        // 队列容量：从配置文件读取，默认可容纳1000个待处理任务
        executor.setQueueCapacity(docQueueCapacity);

        // 空闲线程存活时间：从配置文件读取，默认60秒
        executor.setKeepAliveSeconds(docKeepAliveSeconds);

        // 线程名称前缀：从配置文件读取，默认doc-conversion-
        executor.setThreadNamePrefix(docThreadNamePrefix);

        // 拒绝策略：根据配置动态设置
        executor.setRejectedExecutionHandler(getRejectedPolicy(docRejectedPolicy));

        // 初始化线程池
        executor.initialize();
        return executor;
    }

    /**
     * Markdown转换线程池
     * 专门用于处理Markdown格式的转换任务（如MD转HTML、PDF等）
     * 由于Markdown转换相对简单，配置了较小的线程池资源
     *
     * @return 配置好的Markdown转换线程池
     */
    @Bean(name = "markdownConversionExecutor")
    public ThreadPoolTaskExecutor markdownConversionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数：从配置文件读取，默认5个常驻线程
        executor.setCorePoolSize(mdCorePoolSize);

        // 最大线程数：从配置文件读取，默认最多20个线程
        executor.setMaxPoolSize(mdMaxPoolSize);

        // 队列容量：从配置文件读取，默认可容纳500个待处理任务
        executor.setQueueCapacity(mdQueueCapacity);

        // 空闲线程存活时间：从配置文件读取，默认60秒
        executor.setKeepAliveSeconds(mdKeepAliveSeconds);

        // 线程名称前缀：从配置文件读取，默认md-conversion-
        executor.setThreadNamePrefix(mdThreadNamePrefix);

        // 拒绝策略：根据配置动态设置
        executor.setRejectedExecutionHandler(getRejectedPolicy(mdRejectedPolicy));

        // 初始化线程池
        executor.initialize();
        return executor;
    }

    /**
     * 文档任务优先级队列
     * 使用优先阻塞队列存储文档转换任务，支持任务优先级排序
     * <p>
     * 排序规则：
     * 1. 首先按任务优先级排序（priority值越小，优先级越高）
     * 2. 优先级相同时，按页码排序（pageNumber值越小越靠前）
     * <p>
     * 使用场景：高优先级的文档转换任务可以优先执行，提高用户体验
     *
     * @return 支持优先级排序的阻塞队列
     */
    @Bean
    public PriorityBlockingQueue<DocumentTask> documentTaskQueue() {
        // 创建容量可配置的优先阻塞队列
        // 比较器：先比较优先级，再比较页码
        return new PriorityBlockingQueue<>(documentQueueCapacity,
                Comparator.comparingInt(DocumentTask::getPriority)      // 按优先级升序
                        .thenComparingInt(DocumentTask::getPageNumber)); // 优先级相同时按页码升序
    }

    /**
     * Markdown任务结果队列
     * 使用链式阻塞队列存储Markdown转换的结果
     * <p>
     * 特点：
     * - 单向链表结构，FIFO（先进先出）顺序
     * - 阻塞特性：队列为空时获取操作阻塞，队列满时插入操作阻塞
     * - 适用于生产者-消费者模式
     * <p>
     * 使用场景：Markdown转换任务的结果暂存，供下游消费者处理
     *
     * @return 配置好的链式阻塞队列
     */
    @Bean
    public LinkedBlockingQueue<ConversionResult> markdownTaskQueue() {
        // 创建容量可配置的链式阻塞队列
        return new LinkedBlockingQueue<>(markdownQueueCapacity);
    }

    /**
     * 根据策略名称获取对应的拒绝策略处理器
     *
     * @param policyName 策略名称
     * @return 拒绝策略处理器
     */
    private RejectedExecutionHandler getRejectedPolicy(String policyName) {
        return switch (policyName) {
            case "AbortPolicy" -> new ThreadPoolExecutor.AbortPolicy();
            case "DiscardPolicy" -> new ThreadPoolExecutor.DiscardPolicy();
            case "DiscardOldestPolicy" -> new ThreadPoolExecutor.DiscardOldestPolicy();
            default -> new ThreadPoolExecutor.CallerRunsPolicy();
        };
    }
}