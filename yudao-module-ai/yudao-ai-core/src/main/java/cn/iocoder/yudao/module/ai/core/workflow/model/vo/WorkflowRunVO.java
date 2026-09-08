package cn.iocoder.yudao.module.ai.core.workflow.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 工作流运行记录 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowRunVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long workflowId;

    private String workflowName;

    /** 运行状态：0 运行中、1 成功、2 失败、3 已停止 */
    private Integer status;

    /** 运行输入 */
    private Map<String, Object> inputs;

    /** 运行输出 */
    private Map<String, Object> outputs;

    /** 节点执行日志 */
    private Map<String, Object> nodeLogs;

    /** 最终产物（presentFiles 工具生成的文件列表） */
    private List<Object> fileInfos;

    private String error;

    private Long elapsedMs;

    private LocalDateTime createTime;
}
