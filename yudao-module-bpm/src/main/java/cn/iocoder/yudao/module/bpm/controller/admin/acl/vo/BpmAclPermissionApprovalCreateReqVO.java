package cn.iocoder.yudao.module.bpm.controller.admin.acl.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - ACL 授权审批创建 Request VO")
@Data
public class BpmAclPermissionApprovalCreateReqVO {

    @Schema(description = "操作类型，参见 BpmAclPermissionApprovalOperationTypeEnum 枚举：grant 授予 / set 设置（覆盖）/ revoke 撤销",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "grant")
    @NotEmpty(message = "操作类型不能为空")
    private String operationType;

    @Schema(description = "资源类型（大写枚举名，如 FILE / WIKI / DOCUMENT）", requiredMode = Schema.RequiredMode.REQUIRED, example = "FILE")
    @NotNull(message = "资源类型不能为空")
    private String resourceType;

    @Schema(description = "资源ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "资源ID不能为空")
    private Long resourceId;

    @Schema(description = "主体类型（USER/ORGANIZATION/POST/ROLE/TENANT）", requiredMode = Schema.RequiredMode.REQUIRED, example = "ROLE")
    @NotNull(message = "主体类型不能为空")
    private String principalType;

    @Schema(description = "主体ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "主体ID不能为空")
    private Long principalId;

    @Schema(description = "权限集合（如 [\"READ\",\"WRITE\"]）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "权限不能为空")
    private List<String> permissions;

    @Schema(description = "资源标题（用于审批表单展示，前端传入；同 resourceType+resourceId 对应的实体名称）", example = "季度财报.docx")
    private String resourceTitle;

    @Schema(description = "过期时间（yyyy-MM-dd HH:mm:ss），空表示继承父级/长期", example = "2026-12-31 23:59:59")
    private String expireTime;

    @Schema(description = "审批说明 / 申请理由", example = "给销售部开通该文档的阅读权限")
    private String remark;

    @Schema(description = "发起人自选审批人 Map", example = "{taskKey1: [1, 2]}")
    private Map<String, List<Long>> startUserSelectAssignees;

}