package cn.iocoder.yudao.module.ai.core.workflow.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工作流列表 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowVO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String name;

    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long icon;

    /** 状态：0 草稿、1 已发布、2 已下架 */
    private Integer status;

    /** 版本号 */
    private Integer version;

    /** 使用次数 */
    private Long usageCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
