package cn.iocoder.yudao.module.system.framework.datapermission.rule;

/**
 * {@link AclDataPermissionRule} 的自定义配置接口
 * <p>
 * 各业务模块通过实现该接口，注册需要 ACL 数据权限过滤的表与资源类型映射。
 *
 * @author IIMS
 */
@FunctionalInterface
public interface AclDataPermissionRuleCustomizer {

    /**
     * 自定义 AclDataPermissionRule 配置
     *
     * @param rule AclDataPermissionRule 实例
     */
    void customize(AclDataPermissionRule rule);

}