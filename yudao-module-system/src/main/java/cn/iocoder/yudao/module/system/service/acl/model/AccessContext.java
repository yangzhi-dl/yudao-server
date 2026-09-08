package cn.iocoder.yudao.module.system.service.acl.model;

import cn.iocoder.yudao.module.system.dal.dataobject.acl.AclPermissionDO;
import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;

import java.util.List;
import java.util.Objects;

/**
 * 一次 ACL 判定的完整上下文
 * <p>
 * 由决策引擎负责填充资源归属租户、创建者标记以及 ACL 授权记录，策略链只读取不修改。
 *
 * @author IIMS
 */
public record AccessContext(
        AclPrincipal principal,
        ResourceType resourceType,
        Long resourceId,
        Permission permission,
        Long ownerTenantId,
        boolean creator,
        List<AclPermissionDO> aclEntries) {

    /**
     * 资源是否属于当前主体所在租户
     */
    public boolean sameTenant() {
        return ownerTenantId != null && Objects.equals(ownerTenantId, principal.tenantId());
    }

    /**
     * 资源是否已被 ACL 管控（存在授权记录）
     */
    public boolean hasAcl() {
        return aclEntries != null && !aclEntries.isEmpty();
    }
}
