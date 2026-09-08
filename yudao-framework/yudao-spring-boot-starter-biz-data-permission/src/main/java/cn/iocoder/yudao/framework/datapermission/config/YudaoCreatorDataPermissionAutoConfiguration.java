package cn.iocoder.yudao.framework.datapermission.config;

import cn.iocoder.yudao.framework.common.biz.system.permission.PermissionCommonApi;
import cn.iocoder.yudao.framework.datapermission.core.rule.creator.CreatorDataPermissionRule;
import cn.iocoder.yudao.framework.datapermission.core.rule.creator.CreatorDataPermissionRuleCustomizer;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * 基于创建者的数据权限 AutoConfiguration
 *
 * @author IIMS
 */
@AutoConfiguration
@ConditionalOnClass(LoginUser.class)
@ConditionalOnBean(value = {CreatorDataPermissionRuleCustomizer.class})
public class YudaoCreatorDataPermissionAutoConfiguration {

    @Bean
    public CreatorDataPermissionRule creatorDataPermissionRule(PermissionCommonApi permissionApi,
                                                               List<CreatorDataPermissionRuleCustomizer> customizers) {
        CreatorDataPermissionRule rule = new CreatorDataPermissionRule(permissionApi);
        customizers.forEach(customizer -> customizer.customize(rule));
        return rule;
    }

}