package cn.iocoder.yudao.module.bpm.controller.admin.acl.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ACL 授权审批分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class BpmAclPermissionApprovalPageReqVO extends PageParam {

    @Schema(description = "审批状态，参见 BpmTaskStatusEnum 枚举", example = "2")
    private Integer status;

    @Schema(description = "操作类型", example = "grant")
    private String operationType;

    @Schema(description = "资源类型", example = "FILE")
    private String resourceType;

    @Schema(description = "资源ID", example = "1024")
    private Long resourceId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}