package cn.iocoder.yudao.module.system.controller.admin.acl.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AclPermissionPageReqVO {

    @NotNull(message = "资源类型不能为空")
    private String resourceType;

    @NotNull(message = "资源ID不能为空")
    private Long resourceId;

    private int page = 1;

    private int pageSize = 10;
}