package cn.iocoder.yudao.module.system.enums.acl;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

@Getter
public enum PrincipalType {
    USER(0, 1, "用户"),
    ORGANIZATION(1, 2, "组织"),
    POST(2, 4, "岗位"),
    ROLE(3, 8, "角色"),
    TENANT(4, 16, "租户");

    @EnumValue
    private final int value;
    /**
     * 授权主体掩码位，用于 {@code system_acl_principal_type_config.principal_type_mask}
     */
    private final int mask;
    private final String description;

    PrincipalType(int value, int mask, String description) {
        this.value = value;
        this.mask = mask;
        this.description = description;
    }

    /**
     * 将主体集合合并为掩码
     */
    public static int toMask(Collection<PrincipalType> types) {
        int mask = 0;
        for (PrincipalType type : types) {
            mask |= type.mask;
        }
        return mask;
    }

    /**
     * 从掩码解析出主体集合
     */
    public static Set<PrincipalType> fromMask(int mask) {
        Set<PrincipalType> result = EnumSet.noneOf(PrincipalType.class);
        for (PrincipalType type : values()) {
            if ((mask & type.mask) != 0) {
                result.add(type);
            }
        }
        return result;
    }

    /**
     * 判断掩码是否包含指定主体
     */
    public static boolean contains(int mask, PrincipalType type) {
        return (mask & type.mask) != 0;
    }
}
