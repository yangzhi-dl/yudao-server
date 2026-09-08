package cn.iocoder.yudao.module.analytics.controller.admin.tenant.vo;

import cn.iocoder.yudao.module.analytics.controller.admin.vo.AnalyticsKeyCountRespVO;
import cn.iocoder.yudao.module.analytics.controller.admin.vo.AnalyticsNameCountRespVO;
import cn.iocoder.yudao.module.analytics.controller.admin.vo.AnalyticsTrendRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 租户运营分析 汇总统计 Response VO")
@Data
public class AnalyticsTenantSummaryRespVO {

    @Schema(description = "租户总数")
    private Long totalCount;

    @Schema(description = "启用租户数")
    private Long enabledCount;

    @Schema(description = "禁用租户数")
    private Long disabledCount;

    @Schema(description = "30 天内到期租户数")
    private Long expiringSoonCount;

    @Schema(description = "租户状态分布")
    private List<AnalyticsKeyCountRespVO> statusDist;

    @Schema(description = "租户套餐分布")
    private List<AnalyticsNameCountRespVO> packageDist;

    @Schema(description = "租户增长趋势")
    private List<AnalyticsTrendRespVO> trend;

    @Schema(description = "即将到期租户列表")
    private List<AnalyticsTenantExpiringRespVO> expiringTenants;

    @Schema(description = "统计结果 VO - 即将到期租户")
    @Data
    public static class AnalyticsTenantExpiringRespVO {

        @Schema(description = "租户编号")
        private Long tenantId;

        @Schema(description = "租户名称")
        private String tenantName;

        @Schema(description = "联系人")
        private String contactName;

        @Schema(description = "到期时间")
        private LocalDateTime expireTime;

    }

}