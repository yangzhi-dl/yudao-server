package cn.iocoder.yudao.module.ai.core.workflow.engine.command;

import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.function.Consumer;

/**
 * 命令模式：添加节点命令
 *
 * @author yudao
 */
public class AddNodeCommand implements WorkflowCommand {

    private final List<JSONObject> nodes;
    private final JSONObject node;
    private final Consumer<List<JSONObject>> onNodesChanged;

    public AddNodeCommand(List<JSONObject> nodes, JSONObject node,
                           Consumer<List<JSONObject>> onNodesChanged) {
        this.nodes = nodes;
        this.node = node;
        this.onNodesChanged = onNodesChanged;
    }

    @Override
    public void execute() {
        nodes.add(node);
        if (onNodesChanged != null) {
            onNodesChanged.accept(nodes);
        }
    }

    @Override
    public void undo() {
        nodes.remove(node);
        if (onNodesChanged != null) {
            onNodesChanged.accept(nodes);
        }
    }

    @Override
    public String description() {
        return "添加节点: " + node.getString("id");
    }
}