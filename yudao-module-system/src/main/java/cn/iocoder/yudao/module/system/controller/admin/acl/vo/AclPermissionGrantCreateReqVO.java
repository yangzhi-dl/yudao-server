package cn.iocoder.yudao.module.system.controller.admin.acl.vo;

import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.system.enums.acl.PrincipalType;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AclPermissionGrantCreateReqVO {

    private ResourceType resourceType;

    private Long resourceId;

    private PrincipalType principalType;

    private Long principalId;

    private Permission permission;

    private LocalDateTime expireTime;

    private Long grantBy;

    private String remark;
}