package cn.iocoder.yudao.module.ai.core.chat.config;

import cn.iocoder.yudao.framework.datapermission.core.rule.creator.CreatorDataPermissionRuleCustomizer;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiAgentDO;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiModelDO;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiSkillDO;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.ModelToolDO;
import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import cn.iocoder.yudao.module.system.framework.datapermission.rule.AclDataPermissionRuleCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI Core 模块的数据权限配置
 *
 * @author 芋道源码
 */
@Configuration(proxyBeanMethods = false)
public class AiCoreDataPermissionConfiguration {

    @Bean
    public CreatorDataPermissionRuleCustomizer aiCoreCreatorDataPermissionRuleCustomizer() {
        return rule -> {
            rule.addCreatorColumn(AiSkillDO.class);
            rule.addCreatorColumn(ModelToolDO.class);
        };
    }

    @Bean
    public AclDataPermissionRuleCustomizer aiCoreAclDataPermissionRuleCustomizer() {
        return rule -> {
            rule.addAclColumn(AiAgentDO.class, ResourceType.AGENT, Permission.READ);
            rule.addAclColumn(AiModelDO.class, ResourceType.MODEL, Permission.READ);
        };
    }

}
