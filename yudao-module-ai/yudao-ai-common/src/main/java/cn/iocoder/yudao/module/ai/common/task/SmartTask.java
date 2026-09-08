package cn.iocoder.yudao.module.ai.common.task;

import cn.iocoder.yudao.module.ai.common.enums.TaskPriority;
import lombok.Getter;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 通用智能任务封装，支持优先级调度、超时控制、任务标签等。
 * <p>
 * 优先级数值越小越优先执行。提交时若未指定，默认使用 {@link TaskPriority#NORMAL}。
 * </p>
 *
 * @param <R> 任务返回值类型（无返回值时使用 Void）
 */
@Getter
public class SmartTask<R> implements Comparable<SmartTask<R>> {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);

    /** 任务唯一 ID（自增） */
    private final long id;
    /** 优先级（越小越优先） */
    private final int priority;
    /** 任务标签/分组，用于按业务维度统计或分类 */
    private final String tag;
    /** 提交时间戳（毫秒） */
    private final long submitTimeMs;
    /** 任务名（用于日志） */
    private final String name;
    /** 超时时间（毫秒），0 表示永不超时 */
    private final long timeoutMs;
    /** 实际待执行的任务体 */
    private final Callable<R> callable;

    private SmartTask(Builder<R> builder) {
        this.id = ID_GENERATOR.incrementAndGet();
        this.priority = builder.priority;
        this.tag = builder.tag != null ? builder.tag : "default";
        this.submitTimeMs = System.currentTimeMillis();
        this.name = builder.name != null ? builder.name : ("task-" + this.id);
        this.timeoutMs = builder.timeoutMs;
        this.callable = Objects.requireNonNull(builder.callable, "callable must not be null");
    }

    /**
     * 优先级比较：数值越小越优先；相同时按提交时间 FIFO
     */
    @Override
    public int compareTo(SmartTask<R> o) {
        int cmp = Integer.compare(this.priority, o.priority);
        if (cmp != 0) return cmp;
        return Long.compare(this.submitTimeMs, o.submitTimeMs);
    }

    // ---- 工厂方法 ----

    public static <R> Builder<R> builder(Callable<R> callable) {
        return new Builder<>(callable);
    }

    public static Builder<Void> ofRunnable(Runnable runnable) {
        return new Builder<>(() -> { runnable.run(); return null; });
    }

    /**
     * 创建带返回值的任务
     */
    @FunctionalInterface
    public interface Callable<V> {
        V call() throws Exception;
    }

    // ---- Builder ----

    public static class Builder<R> {
        private int priority = TaskPriority.NORMAL.getValue();
        private String tag;
        private String name;
        private long timeoutMs;
        private final Callable<R> callable;

        private Builder(Callable<R> callable) {
            this.callable = callable;
        }

        public Builder<R> priority(TaskPriority priority) {
            this.priority = priority.getValue();
            return this;
        }

        public Builder<R> priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder<R> tag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder<R> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<R> timeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        public SmartTask<R> build() {
            return new SmartTask<>(this);
        }
    }
}
