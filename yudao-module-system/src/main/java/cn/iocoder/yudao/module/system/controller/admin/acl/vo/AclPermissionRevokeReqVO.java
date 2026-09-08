package cn.iocoder.yudao.module.system.controller.admin.acl.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AclPermissionRevokeReqVO {

    @NotNull(message = "资源类型不能为空")
    private String resourceType;

    @NotNull(message = "资源ID不能为空")
    private Long resourceId;

    /** 主体类型：USER / ORGANIZATION / POST / ROLE */
    @NotNull(message = "主体类型不能为空")
    private String principalType;

    @NotNull(message = "主体ID不能为空")
    private Long principalId;

    @NotNull(message = "撤销的权限不能为空")
    private List<String> permissions;
}