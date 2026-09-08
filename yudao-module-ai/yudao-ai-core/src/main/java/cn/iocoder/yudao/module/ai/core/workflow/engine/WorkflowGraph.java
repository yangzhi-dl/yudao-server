package cn.iocoder.yudao.module.ai.core.workflow.engine;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作流图：解析画布 JSON（{nodes, edges}）并建立节点/连线索引
 */
@Slf4j
public class WorkflowGraph {

    private final List<WorkflowGraphNode> nodes = new ArrayList<>();
    private final List<WorkflowGraphEdge> edges = new ArrayList<>();
    private final Map<String, WorkflowGraphNode> nodeMap = new HashMap<>();

    /**
     * 解析画布数据（对象或 JSON 字符串均可）
     */
    public static WorkflowGraph parse(Object graph) {
        WorkflowGraph result = new WorkflowGraph();
        if (graph == null) {
            return result;
        }
        String json = graph instanceof String str ? str : JSON.toJSONString(graph);
        JSONObject root = JSON.parseObject(json);
        if (root == null) {
            return result;
        }
        JSONArray nodesArray = root.getJSONArray("nodes");
        JSONArray edgesArray = root.getJSONArray("edges");
        if (nodesArray != null) {
            for (int i = 0; i < nodesArray.size(); i++) {
                JSONObject nodeObj = nodesArray.getJSONObject(i);
                WorkflowGraphNode node = new WorkflowGraphNode();
                node.setId(nodeObj.getString("id"));
                node.setType(nodeObj.getString("type"));
                JSONObject data = nodeObj.getJSONObject("data");
                node.setData(data == null ? new JSONObject() : data);
                node.setName(firstText(data, "title", "label"));
                if (node.getId() != null && node.getType() != null) {
                    result.nodes.add(node);
                    result.nodeMap.put(node.getId(), node);
                }
            }
        }
        if (edgesArray != null) {
            for (int i = 0; i < edgesArray.size(); i++) {
                JSONObject edgeObj = edgesArray.getJSONObject(i);
                WorkflowGraphEdge edge = new WorkflowGraphEdge();
                edge.setId(edgeObj.getString("id"));
                edge.setSource(edgeObj.getString("source"));
                edge.setTarget(edgeObj.getString("target"));
                edge.setSourceHandle(edgeObj.getString("sourceHandle"));
                JSONObject edgeData = edgeObj.getJSONObject("data");
                if (edgeData != null) {
                    edge.setCondition(edgeData.getString("condition"));
                }
                if (edge.getSource() != null && edge.getTarget() != null) {
                    result.edges.add(edge);
                    WorkflowGraphNode sourceNode = result.nodeMap.get(edge.getSource());
                    WorkflowGraphNode targetNode = result.nodeMap.get(edge.getTarget());
                    if (sourceNode != null) {
                        sourceNode.getOutEdges().add(edge);
                    }
                    if (targetNode != null) {
                        targetNode.getInEdges().add(edge);
                    }
                }
            }
        }
        return result;
    }

    private static String firstText(JSONObject data, String... keys) {
        if (data == null) {
            return null;
        }
        for (String key : keys) {
            String value = data.getString(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    public List<WorkflowGraphNode> getNodes() {
        return nodes;
    }

    public List<WorkflowGraphEdge> getEdges() {
        return edges;
    }

    public WorkflowGraphNode getNode(String id) {
        return nodeMap.get(id);
    }

    /** 起点：无入边的节点 */
    public List<WorkflowGraphNode> getStartNodes() {
        List<WorkflowGraphNode> starts = new ArrayList<>();
        for (WorkflowGraphNode node : nodes) {
            if (node.getInEdges().isEmpty()) {
                starts.add(node);
            }
        }
        return starts;
    }
}
