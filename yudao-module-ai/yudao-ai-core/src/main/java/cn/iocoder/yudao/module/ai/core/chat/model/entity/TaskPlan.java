package cn.iocoder.yudao.module.ai.core.chat.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskPlan {
    @JsonProperty("overall_analysis")
    private String overallAnalysis;

    @JsonProperty("execution_order")
    private List<String> executionOrder;

    @JsonProperty("expert_tasks")
    private Map<String, String> expertTasks;
}
