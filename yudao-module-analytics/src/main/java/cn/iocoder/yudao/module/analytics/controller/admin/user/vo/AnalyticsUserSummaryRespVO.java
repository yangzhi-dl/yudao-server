package cn.iocoder.yudao.module.analytics.controller.admin.user.vo;

import cn.iocoder.yudao.module.analytics.controller.admin.vo.AnalyticsKeyCountRespVO;
import cn.iocoder.yudao.module.analytics.controller.admin.vo.AnalyticsTrendRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 用户与登录分析 汇总统计 Response VO")
@Data
public class AnalyticsUserSummaryRespVO {

    @Schema(description = "用户总数")
    private Long totalUser;

    @Schema(description = "启用用户数")
    private Long enabledUser;

    @Schema(description = "禁用用户数")
    private Long disabledUser;

    @Schema(description = "在线用户数")
    private Long onlineUser;

    @Schema(description = "用户增长趋势")
    private List<AnalyticsTrendRespVO> userTrend;

    @Schema(description = "登录趋势（日期 -> 登录次数/成功次数）")
    private List<AnalyticsLoginTrendRespVO> loginTrend;

    @Schema(description = "登录类型分布")
    private List<AnalyticsKeyCountRespVO> loginTypeDist;

    @Schema(description = "登录结果分布")
    private List<AnalyticsKeyCountRespVO> resultDist;

    @Schema(description = "统计结果 VO - 登录趋势")
    @Data
    public static class AnalyticsLoginTrendRespVO {

        @Schema(description = "日期（yyyy-MM-dd）")
        private String date;

        @Schema(description = "登录次数")
        private Long loginCount;

        @Schema(description = "登录成功次数")
        private Long successCount;

    }

}