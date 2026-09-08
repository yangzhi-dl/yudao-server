package cn.iocoder.yudao.module.system.framework.datapermission.config;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.module.system.framework.datapermission.rule.AclDataPermissionRule;
import cn.iocoder.yudao.module.system.framework.datapermission.rule.AclDataPermissionRuleCustomizer;
import cn.iocoder.yudao.module.system.service.acl.IPrincipalNameResolver;
import cn.iocoder.yudao.module.system.service.acl.engine.AclDecisionEngine;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * ACL 数据权限自动配置
 * <p>
 * 当存在 {@link AclDataPermissionRuleCustomizer} Bean 时，自动创建 {@link AclDataPermissionRule}，
 * 为业务表注入 ACL 粒度的数据权限过滤条件。
 *
 * @author IIMS
 */
@AutoConfiguration
@ConditionalOnClass(LoginUser.class)
@ConditionalOnBean(value = {AclDataPermissionRuleCustomizer.class})
public class AclDataPermissionAutoConfiguration {

    @Bean
    public AclDataPermissionRule aclDataPermissionRule(AclDecisionEngine aclDecisionEngine,
                                                       IPrincipalNameResolver principalNameResolver,
                                                       List<AclDataPermissionRuleCustomizer> customizers) {
        AclDataPermissionRule rule = new AclDataPermissionRule(aclDecisionEngine, principalNameResolver);
        customizers.forEach(customizer -> customizer.customize(rule));
        return rule;
    }

}