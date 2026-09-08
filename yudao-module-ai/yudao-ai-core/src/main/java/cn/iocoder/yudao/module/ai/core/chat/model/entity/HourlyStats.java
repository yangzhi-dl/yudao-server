package cn.iocoder.yudao.module.ai.core.chat.model.entity;

import lombok.Data;

@Data
public class HourlyStats {
    private Integer hour;           // 小时 (0-23)
    private Long callCount;         // 调用次数
    private Double avgResponseTime; // 平均响应时间(ms)
    private Long positiveCount;     // 正反馈数量 (feedback_status = 1)
    private Long negativeCount;     // 负反馈数量 (feedback_status = -1)
    private Long feedbackTotal;     // 总反馈数量 (正+负)
}