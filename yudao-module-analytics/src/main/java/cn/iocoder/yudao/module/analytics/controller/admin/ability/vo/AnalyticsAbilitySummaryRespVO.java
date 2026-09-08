package cn.iocoder.yudao.module.analytics.controller.admin.ability.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI 能力分析 汇总统计 Response VO")
@Data
public class AnalyticsAbilitySummaryRespVO {

    @Schema(description = "模型数量")
    private Long modelCount;

    @Schema(description = "智能体数量")
    private Long agentCount;

    @Schema(description = "工具数量")
    private Long toolCount;

    @Schema(description = "技能数量")
    private Long skillCount;

    @Schema(description = "知识库数量")
    private Long knowledgeCount;

    @Schema(description = "文档数量")
    private Long documentCount;

    @Schema(description = "工作流数量")
    private Long workflowCount;

    @Schema(description = "Token 使用总量")
    private Long tokenUsage;

    @Schema(description = "模型接入接口类型分布（AiApiType -> 数量）")
    private List<AnalyticsAbilityKeyCountRespVO> modelApiTypeDist;

    @Schema(description = "模型能力类型分布（AiModelType -> 数量）")
    private List<AnalyticsAbilityKeyCountRespVO> modelTypeDist;

    @Schema(description = "模型在线状态分布（0 离线、1 在线 -> 数量）")
    private List<AnalyticsAbilityKeyCountRespVO> modelOnlineDist;

    @Schema(description = "智能体类型分布（AgentType -> 数量）")
    private List<AnalyticsAbilityKeyCountRespVO> agentTypeDist;

    @Schema(description = "工具类型分布（ToolType -> 数量）")
    private List<AnalyticsAbilityKeyCountRespVO> toolTypeDist;

}