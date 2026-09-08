package cn.iocoder.yudao.framework.datapermission.core.rule.creator;

/**
 * {@link CreatorDataPermissionRule} 的自定义配置接口
 *
 * @author IIMS
 */
@FunctionalInterface
public interface CreatorDataPermissionRuleCustomizer {

    /**
     * 自定义 CreatorDataPermissionRule 配置
     *
     * @param rule CreatorDataPermissionRule 实例
     */
    void customize(CreatorDataPermissionRule rule);

}