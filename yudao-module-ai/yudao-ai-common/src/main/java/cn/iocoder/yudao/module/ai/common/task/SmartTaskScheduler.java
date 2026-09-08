package cn.iocoder.yudao.module.ai.common.task;

import cn.iocoder.yudao.module.ai.common.enums.TaskPriority;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import com.alibaba.ttl.TtlCallable;
import com.alibaba.ttl.TtlRunnable;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * 通用智能任务调度器 —— 线程池 + 优先级队列
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li><b>优先级调度</b>：内部使用 {@link PriorityBlockingQueue}，任务按优先级+提交时间 FIFO 排序</li>
 *   <li><b>标签分级</b>：支持按 tag 分组统计排队/运行中任务数，便于监控与限流</li>
 *   <li><b>超时控制</b>：任务级超时，超时后自动中断执行</li>
 *   <li><b>简单 API</b>：{@code scheduler.submit(() -> doWork())} 一行替代原有用法</li>
 *   <li><b>优雅关闭</b>：实现 {@link AutoCloseable}，Spring 容器销毁时自动等待任务完成</li>
 * </ul>
 *
 * <h3>API 风格</h3>
 * <pre>{@code
 * smartTaskScheduler.submit(() -> doWork(), TaskPriority.HIGH, "chat");
 * }</pre>
 */
@Slf4j
public class SmartTaskScheduler implements AutoCloseable {

    private final ThreadPoolExecutor executor;
    private final String name;

    // 统计
    private final AtomicLong submittedCount = new AtomicLong(0);
    private final AtomicLong completedCount = new AtomicLong(0);
    private final AtomicLong rejectedCount = new AtomicLong(0);
    private final AtomicLong timeoutCount = new AtomicLong(0);
    private final AtomicLong failedCount = new AtomicLong(0);

    // 异常回调（全局兜底）
    @Setter
    private volatile Consumer<Throwable> exceptionHandler;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * @param name          调度器名称（用于日志、线程名）
     * @param corePoolSize  核心线程数
     * @param maxPoolSize   最大线程数
     * @param queueCapacity 队列容量
     * @param keepAliveMs   非核心线程空闲存活时间（毫秒）
     */
    public SmartTaskScheduler(String name, int corePoolSize, int maxPoolSize,
                              int queueCapacity, long keepAliveMs) {
        this.name = name;

        PriorityBlockingQueue<Runnable> workQueue = new PriorityBlockingQueue<>(queueCapacity,
                (a, b) -> {
                    if (a instanceof ComparableTask ta && b instanceof ComparableTask tb) {
                        return ta.comparePriorityTo(tb);
                    }
                    return 0;
                });

        this.executor = new ThreadPoolExecutor(
                corePoolSize, maxPoolSize,
                keepAliveMs, TimeUnit.MILLISECONDS,
                workQueue,
                new NamedThreadFactory(name),
                new ThreadPoolExecutor.CallerRunsPolicy() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                        rejectedCount.incrementAndGet();
                        if (r instanceof ComparableTask ft) {
                            log.warn("[{}] 任务被拒绝: id={} name={} tag={}", name,
                                    ft.getTask().getId(), ft.getTask().getName(), ft.getTask().getTag());
                        }
                        super.rejectedExecution(r, e);
                    }
                });

        // 允许核心线程超时
        this.executor.allowCoreThreadTimeOut(corePoolSize == 0);

        log.info("[{}] SmartTaskScheduler 创建完成: core={} max={} queue={} keepAlive={}ms",
                name, corePoolSize, maxPoolSize, queueCapacity, keepAliveMs);
    }

    // ==================== 提交 API ====================

    /**
     * 提交无返回值的异步任务（普通优先级，默认标签）
     */
    public void submit(Runnable runnable) {
        submit(runnable, TaskPriority.NORMAL.getValue(), "default");
    }

    /**
     * 提交无返回值的异步任务，指定优先级
     */
    public void submit(Runnable runnable, TaskPriority priority) {
        submit(runnable, priority.getValue(), "default");
    }

    /**
     * 提交无返回值的异步任务，指定优先级和标签
     */
    public void submit(Runnable runnable, TaskPriority priority, String tag) {
        submit(runnable, priority.getValue(), tag);
    }

    /**
     * 提交无返回值的异步任务，指定优先级数值和标签
     */
    public void submit(Runnable runnable, int priority, String tag) {
        SmartTask<Void> task = SmartTask.ofRunnable(runnable)
                .priority(priority).tag(tag).build();
        submitTask(task);
    }

    /**
     * 提交带返回值的任务
     */
    public <R> Future<R> submit(SmartTask.Callable<R> callable) {
        return submit(SmartTask.builder(callable).build());
    }

    /**
     * 提交已构建好的 SmartTask
     */
    public <R> Future<R> submit(SmartTask<R> task) {
        return submitTask(task);
    }

    // ==================== 监控 API ====================

    /**
     * 当前排队任务数
     */
    public int getQueueSize() {
        return executor.getQueue().size();
    }

    /**
     * 当前活跃线程数
     */
    public int getActiveCount() {
        return executor.getActiveCount();
    }

    /**
     * 历史完成任务数
     */
    public long getCompletedCount() {
        return completedCount.get();
    }

    /**
     * 历史提交任务数
     */
    public long getSubmittedCount() {
        return submittedCount.get();
    }

    /**
     * 历史拒绝任务数
     */
    public long getRejectedCount() {
        return rejectedCount.get();
    }

    /**
     * 历史超时任务数
     */
    public long getTimeoutCount() {
        return timeoutCount.get();
    }

    /**
     * 历史失败任务数
     */
    public long getFailedCount() {
        return failedCount.get();
    }

    /**
     * 线程池快照
     */
    public String getStats() {
        return String.format("[%s] submitted=%d completed=%d running=%d queued=%d rejected=%d timeout=%d failed=%d",
                name, submittedCount.get(), completedCount.get(),
                getActiveCount(), getQueueSize(), rejectedCount.get(), timeoutCount.get(), failedCount.get());
    }

    /**
     * 按 tag 统计：排队中 + 运行中
     */
    public Map<String, Long> getTagCounts() {
        Map<String, Long> counts = new ConcurrentHashMap<>();
        for (Runnable r : executor.getQueue()) {
            if (r instanceof ComparableTask ft) {
                counts.merge(ft.getTask().getTag(), 1L, Long::sum);
            }
        }
        return counts;
    }

    // ==================== 生命周期 ====================

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) return;
        log.info("[{}] SmartTaskScheduler 开始关闭, 等待排队任务 {} 完成...", name, getQueueSize());
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                log.warn("[{}] 等待超时，强制关闭, 剩余排队 {} 个", name, getQueueSize());
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("[{}] SmartTaskScheduler 已关闭, {}", name, getStats());
    }

    // ==================== 内部实现 ====================

    /**
     * 无泛型任务标记接口，供 PriorityBlockingQueue 比较器使用，避免泛型捕获冲突
     */
    private interface ComparableTask {
        int comparePriorityTo(ComparableTask other);

        SmartTask<?> getTask();
    }

    private <R> Future<R> submitTask(SmartTask<R> task) {
        if (closed.get()) {
            rejectedCount.incrementAndGet();
            throw new RejectedExecutionException("SmartTaskScheduler[" + name + "] 已关闭");
        }

        SmartTaskFutureTask<R> futureTask = new SmartTaskFutureTask<>(task);
        submittedCount.incrementAndGet();

        try {
            executor.execute(TtlRunnable.get(futureTask));
        } catch (RejectedExecutionException e) {
            rejectedCount.incrementAndGet();
            throw e;
        }
        return futureTask;
    }

    /**
     * 适配 PriorityBlockingQueue + FutureTask，同时实现 ComparableTask 用于队列比较
     */
    private class SmartTaskFutureTask<R> extends FutureTask<R> implements ComparableTask {
        private final SmartTask<R> task;

        SmartTaskFutureTask(SmartTask<R> task) {
            super(new SmartCallableWrapper<>(SmartTaskScheduler.this, task));
            this.task = task;
        }

        @Override
        public SmartTask<?> getTask() {
            return task;
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public int comparePriorityTo(ComparableTask other) {
            return ((Comparable) this.task).compareTo(other.getTask());
        }
    }

    /**
     * 包装 callable，加入超时、统计、异常处理
     */
    private record SmartCallableWrapper<R>(SmartTaskScheduler scheduler, SmartTask<R> task) implements Callable<R> {

        @Override
        public R call() throws Exception {
            long start = System.currentTimeMillis();
            long waitMs = start - task.getSubmitTimeMs();
            log.debug("[{}] 开始执行: id={} name={} tag={} 排队耗时={}ms",
                    scheduler.name, task.getId(), task.getName(), task.getTag(), waitMs);

            try {
                R result;
                if (task.getTimeoutMs() <= 0) {
                    result = task.getCallable().call();
                } else {
                    result = executeWithTimeout(task.getCallable(), task.getTimeoutMs());
                }
                scheduler.completedCount.incrementAndGet();
                long elapsed = System.currentTimeMillis() - start;
                if (elapsed > 1000) {
                    log.info("[{}] 完成(慢): id={} name={} tag={} 耗时={}ms",
                            scheduler.name, task.getId(), task.getName(), task.getTag(), elapsed);
                }
                return result;
            } catch (TimeoutException e) {
                scheduler.timeoutCount.incrementAndGet();
                log.warn("[{}] 任务超时: id={} name={} tag={} timeout={}ms",
                        scheduler.name, task.getId(), task.getName(), task.getTag(), task.getTimeoutMs());
                throw e;
            } catch (InterruptedException e) {
                scheduler.failedCount.incrementAndGet();
                Thread.currentThread().interrupt();
                throw e;
            } catch (Exception e) {
                scheduler.failedCount.incrementAndGet();
                log.error("[{}] 任务异常: id={} name={} tag={}", scheduler.name, task.getId(), task.getName(), task.getTag(), e);
                Consumer<Throwable> handler = scheduler.exceptionHandler;
                if (handler != null) {
                    try {
                        handler.accept(e);
                    } catch (Exception ignored) {
                    }
                }
                throw e;
            }
        }
    }

    /**
     * 在独立线程中执行带超时的任务，不占用线程池
     */
    private static <R> R executeWithTimeout(SmartTask.Callable<R> callable, long timeoutMs) throws Exception {
        ExecutorService singleExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "smart-task-timeout");
            t.setDaemon(true);
            return t;
        });
        try {
            Future<R> future = singleExecutor.submit(TtlCallable.get(() -> {
                try {
                    return callable.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
            try {
                return future.get(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                throw e;
            }
        } finally {
            singleExecutor.shutdownNow();
        }
    }

    /**
     * 可命名的线程工厂
     */
    static class NamedThreadFactory implements ThreadFactory {
        private final AtomicLong counter = new AtomicLong(0);
        private final String prefix;

        NamedThreadFactory(String prefix) {
            this.prefix = prefix.endsWith("-") ? prefix : prefix + "-";
        }

        @Override
        public Thread newThread(@NonNull Runnable r) {
            Thread t = new Thread(r, prefix + counter.incrementAndGet());
            t.setDaemon(true);
            return t;
        }
    }
}
