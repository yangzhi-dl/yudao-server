package cn.iocoder.yudao.module.system.enums.acl;

import lombok.Getter;

@Getter
public enum Permission {
    READ(1, "读权限"),
    WRITE(2, "写权限"),
    EXECUTE(4, "执行权限"),
    DELETE(8, "删除权限"),
    MANAGE(16, "管理权限"),
    DOWNLOAD(32, "下载权限"),
    COPY(64, "复制权限"),
    PRINT(128, "打印权限"),
    AUTH(256, "ACL授权权限");

    private final int mask;
    private final String description;

    Permission(int mask, String description) {
        this.mask = mask;
        this.description = description;
    }

    /**
     * 添加权限
     * <p>通过按位或运算，将新权限合并到现有权限掩码中。
     * 如果该权限已存在，则不会重复添加。
     *
     * @param currentMask 当前权限掩码（已有的所有权限）
     * @param permission  需要添加的权限
     * @return 添加后的新权限掩码
     *
     */
    public static int addPermission(int currentMask, Permission permission) {
        return currentMask | permission.mask;
    }

    /**
     * 移除权限
     * <p>通过按位与和按位取反运算，从现有权限掩码中移除指定权限。
     * 如果该权限不存在，则权限掩码保持不变。
     *
     * @param currentMask 当前权限掩码（已有的所有权限）
     * @param permission  需要移除的权限
     * @return 移除后的新权限掩码
     */
    public static int removePermission(int currentMask, Permission permission) {
        return currentMask & ~permission.mask;
    }

    /**
     * 检查是否拥有指定权限
     * <p>通过按位与运算判断权限掩码中是否包含目标权限。
     * 如果结果不为0，表示拥有该权限；结果为0表示没有该权限。
     *
     * @param mask      权限掩码（通常是某个用户或角色的权限集合）
     * @param permission 需要检查的权限
     * @return true：拥有该权限；false：没有该权限
     */
    public static boolean hasPermission(int mask, Permission permission) {
        return (mask & permission.mask) != 0;
    }
}