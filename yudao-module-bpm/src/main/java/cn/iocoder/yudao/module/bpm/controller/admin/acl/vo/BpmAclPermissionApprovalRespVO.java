package cn.iocoder.yudao.module.bpm.controller.admin.acl.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ACL 授权审批 Response VO")
@Data
public class BpmAclPermissionApprovalRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "申请人的用户编号", example = "1")
    private Long userId;

    @Schema(description = "操作类型", example = "grant")
    private String operationType;

    @Schema(description = "资源类型", example = "FILE")
    private String resourceType;

    @Schema(description = "资源ID", example = "1024")
    private Long resourceId;

    @Schema(description = "主体类型", example = "ROLE")
    private String principalType;

    @Schema(description = "主体ID", example = "1")
    private Long principalId;

    @Schema(description = "主体名称")
    private String principalName;

    @Schema(description = "权限集合")
    private List<String> permissions;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "审批说明 / 申请理由")
    private String remark;

    @Schema(description = "审批状态，参见 BpmTaskStatusEnum 枚举", example = "1")
    private Integer status;

    @Schema(description = "对应的流程编号", example = "95f2f08b-621b-11ef-bf39-00ff4722db8b")
    private String processInstanceId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}