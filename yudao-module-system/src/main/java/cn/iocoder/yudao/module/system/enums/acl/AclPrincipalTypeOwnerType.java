package cn.iocoder.yudao.module.system.enums.acl;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * ACL 授权主体配置的归属类型
 *
 * @author IIMS
 */
@Getter
public enum AclPrincipalTypeOwnerType {

    ROLE(0, "角色"),
    PACKAGE(1, "租户套餐");

    @EnumValue
    private final int value;
    private final String description;

    AclPrincipalTypeOwnerType(int value, String description) {
        this.value = value;
        this.description = description;
    }
}
