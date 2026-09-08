package cn.iocoder.yudao.module.ai.core.workflow.engine;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作流节点（画布 JSON node 的解析模型）
 * <p>
 * 与前端 VueFlow 画布数据契约一致：
 * <pre>
 * { "id": "xxx", "type": "yudaoLlmNode", "position": {...},
 *   "data": { "title": "...", "parameters": [...], "outputDefs": [...], "llmId": ..., ... } }
 * </pre>
 */
@Data
public class WorkflowGraphNode {

    /** 节点 ID（与画布一致） */
    private String id;

    /** 节点类型：start / end / llm / agent / knowledge / http / template / ifelse / loop */
    private String type;

    /** 节点名称（data.title 或 data.label） */
    private String name;

    /** 节点配置数据（原始 JSON，各处理器按类型读取字段） */
    private JSONObject data;

    /** 入边 */
    private final List<WorkflowGraphEdge> inEdges = new ArrayList<>();

    /** 出边 */
    private final List<WorkflowGraphEdge> outEdges = new ArrayList<>();
}
