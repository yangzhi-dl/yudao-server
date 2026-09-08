package cn.iocoder.yudao.module.ai.core.workflow.engine.command;

import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.function.Consumer;

/**
 * 命令模式：删除节点命令
 *
 * @author yudao
 */
public class RemoveNodeCommand implements WorkflowCommand {

    private final List<JSONObject> nodes;
    private final JSONObject node;
    private final int originalIndex;
    private final Consumer<List<JSONObject>> onNodesChanged;

    public RemoveNodeCommand(List<JSONObject> nodes, JSONObject node,
                              Consumer<List<JSONObject>> onNodesChanged) {
        this.nodes = nodes;
        this.node = node;
        this.originalIndex = nodes.indexOf(node);
        this.onNodesChanged = onNodesChanged;
    }

    @Override
    public void execute() {
        nodes.remove(node);
        if (onNodesChanged != null) {
            onNodesChanged.accept(nodes);
        }
    }

    @Override
    public void undo() {
        if (originalIndex >= 0 && originalIndex <= nodes.size()) {
            nodes.add(originalIndex, node);
        } else {
            nodes.add(node);
        }
        if (onNodesChanged != null) {
            onNodesChanged.accept(nodes);
        }
    }

    @Override
    public String description() {
        return "删除节点: " + node.getString("id");
    }
}