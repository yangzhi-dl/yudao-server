package cn.iocoder.yudao.module.analytics.controller.admin.overview.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 平台分析总览 租户统计 Response VO")
@Data
public class AnalyticsOverviewTenantStatsRespVO {

    @Schema(description = "租户编号")
    private Long tenantId;

    @Schema(description = "租户名称")
    private String tenantName;

    @Schema(description = "用户数量")
    private Long userCount;

    @Schema(description = "部门数量")
    private Long deptCount;

    @Schema(description = "模型数量")
    private Long modelCount;

    @Schema(description = "知识库数量")
    private Long knowledgeCount;

    @Schema(description = "智能体数量")
    private Long agentCount;

    @Schema(description = "文档数量")
    private Long documentCount;

    @Schema(description = "Token 使用量")
    private Long tokenUsage;

}