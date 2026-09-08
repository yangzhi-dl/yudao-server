package cn.iocoder.yudao.module.ai.core.workflow.engine.memento;

import java.time.LocalDateTime;

/**
 * 备忘录模式（Memento Pattern）：工作流画布快照
 * <p>
 * 保存工作流在某个时间点的完整画布状态，用于版本回退和历史查看。
 *
 * @author yudao
 */
public class WorkflowSnapshot {

    /** 快照版本号 */
    private final int version;

    /** 快照时间 */
    private final LocalDateTime snapshotTime;

    /** 画布数据（JSON） */
    private final Object graph;

    /** 快照描述 */
    private final String description;

    public WorkflowSnapshot(int version, Object graph, String description) {
        this.version = version;
        this.snapshotTime = LocalDateTime.now();
        this.graph = graph;
        this.description = description;
    }

    public int getVersion() {
        return version;
    }

    public LocalDateTime getSnapshotTime() {
        return snapshotTime;
    }

    public Object getGraph() {
        return graph;
    }

    public String getDescription() {
        return description;
    }
}