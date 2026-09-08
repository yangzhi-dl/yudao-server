package cn.iocoder.yudao.module.analytics.controller.admin.aiusage.vo;

import cn.iocoder.yudao.module.analytics.controller.admin.vo.AnalyticsNameCountRespVO;
import cn.iocoder.yudao.module.analytics.controller.admin.vo.AnalyticsTrendRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI 使用分析 汇总统计 Response VO")
@Data
public class AnalyticsAiUsageSummaryRespVO {

    @Schema(description = "对话（消息）总数")
    private Long totalDialogue;

    @Schema(description = "会话数")
    private Long totalTopic;

    @Schema(description = "调用次数")
    private Long totalCall;

    @Schema(description = "Token 使用总量")
    private Long totalToken;

    @Schema(description = "对话量趋势")
    private List<AnalyticsTrendRespVO> dialogueTrend;

    @Schema(description = "会话量趋势")
    private List<AnalyticsTrendRespVO> topicTrend;

    @Schema(description = "Token 使用趋势（日期 -> token）")
    private List<AnalyticsAiTokenTrendRespVO> tokenTrend;

    @Schema(description = "模型 Token 使用 Top")
    private List<AnalyticsNameCountRespVO> modelTokenTop;

    @Schema(description = "模型调用次数 Top")
    private List<AnalyticsNameCountRespVO> modelCallTop;

    @Schema(description = "统计结果 VO - Token 使用趋势")
    @Data
    public static class AnalyticsAiTokenTrendRespVO {

        @Schema(description = "日期（yyyy-MM-dd）")
        private String date;

        @Schema(description = "Token 使用量")
        private Long token;

    }

}