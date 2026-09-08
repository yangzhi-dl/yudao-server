package cn.iocoder.yudao.module.bpm.enums.acl;

import cn.hutool.core.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ACL 授权审批的操作类型
 *
 * @author IIMS
 */
@Getter
@AllArgsConstructor
public enum BpmAclPermissionApprovalOperationTypeEnum {

    GRANT("grant", "授予权限"),
    SET("set", "设置权限（覆盖）"),
    REVOKE("revoke", "撤销权限");

    /**
     * 操作类型
     */
    private final String type;
    /**
     * 描述
     */
    private final String description;

    public static BpmAclPermissionApprovalOperationTypeEnum valueOfType(String type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}