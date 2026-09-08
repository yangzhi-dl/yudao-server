package cn.iocoder.yudao.module.ai.core.workflow.engine.memento;

import java.util.ArrayList;
import java.util.List;

/**
 * 备忘录模式：工作流快照管理器
 * <p>
 * 负责保存和恢复工作流画布快照，支持版本回退。
 *
 * @author yudao
 */
public class WorkflowSnapshotManager {

    private final List<WorkflowSnapshot> snapshots = new ArrayList<>();
    private int currentVersion = 0;
    private final int maxSnapshots;

    public WorkflowSnapshotManager(int maxSnapshots) {
        this.maxSnapshots = maxSnapshots;
    }

    public WorkflowSnapshotManager() {
        this(20);
    }

    /**
     * 创建快照
     */
    public WorkflowSnapshot saveSnapshot(Object graph, String description) {
        currentVersion++;
        WorkflowSnapshot snapshot = new WorkflowSnapshot(currentVersion, graph, description);
        snapshots.add(snapshot);
        while (snapshots.size() > maxSnapshots) {
            snapshots.removeFirst();
        }
        return snapshot;
    }

    /**
     * 获取最新快照
     */
    public WorkflowSnapshot getLatestSnapshot() {
        if (snapshots.isEmpty()) {
            return null;
        }
        return snapshots.getLast();
    }

    /**
     * 按版本号获取快照
     */
    public WorkflowSnapshot getSnapshot(int version) {
        return snapshots.stream()
                .filter(s -> s.getVersion() == version)
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取所有快照
     */
    public List<WorkflowSnapshot> getAllSnapshots() {
        return new ArrayList<>(snapshots);
    }

    /**
     * 清空快照
     */
    public void clear() {
        snapshots.clear();
        currentVersion = 0;
    }
}