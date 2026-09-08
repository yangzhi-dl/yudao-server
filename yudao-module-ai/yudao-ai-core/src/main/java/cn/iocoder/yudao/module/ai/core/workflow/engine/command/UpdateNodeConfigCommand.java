package cn.iocoder.yudao.module.ai.core.workflow.engine.command;

import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.function.Consumer;

/**
 * 命令模式：更新节点配置命令
 *
 * @author yudao
 */
public class UpdateNodeConfigCommand implements WorkflowCommand {

    private final JSONObject node;
    private final JSONObject newConfig;
    private final JSONObject oldConfig;
    private final Consumer<List<JSONObject>> onNodesChanged;

    public UpdateNodeConfigCommand(JSONObject node, JSONObject newConfig,
                                    Consumer<List<JSONObject>> onNodesChanged) {
        this.node = node;
        this.newConfig = new JSONObject(newConfig);
        this.oldConfig = node.getJSONObject("data") != null
                ? new JSONObject(node.getJSONObject("data")) : new JSONObject();
        this.onNodesChanged = onNodesChanged;
    }

    @Override
    public void execute() {
        node.put("data", newConfig);
        if (onNodesChanged != null) {
            onNodesChanged.accept(null);
        }
    }

    @Override
    public void undo() {
        node.put("data", oldConfig);
        if (onNodesChanged != null) {
            onNodesChanged.accept(null);
        }
    }

    @Override
    public String description() {
        return "更新节点配置: " + node.getString("id");
    }
}