package cn.iocoder.yudao.module.system.controller.admin.acl.vo;

import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.system.enums.acl.PrincipalType;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AclPermissionRevokeCreateReqVO {

    private ResourceType resourceType;

    private Long resourceId;

    private PrincipalType principalType;

    private Long principalId;

    private Permission permission;
}