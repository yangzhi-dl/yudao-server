package cn.iocoder.yudao.module.ai.knowledge.common.config;

import cn.iocoder.yudao.module.ai.knowledge.common.service.KnowledgeService;
import cn.iocoder.yudao.module.ai.knowledge.document.dal.dataobject.Document;
import cn.iocoder.yudao.module.ai.knowledge.wiki.dal.dataobject.Wiki;
import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import cn.iocoder.yudao.module.system.framework.datapermission.rule.AclDataPermissionRuleCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 知识库模块的数据权限配置
 *
 * 
 */
@Configuration(proxyBeanMethods = false)
public class AiKnowledgeDataPermissionConfiguration {

    @Bean
    public AclDataPermissionRuleCustomizer aiKnowledgeAclDataPermissionRuleCustomizer(KnowledgeService knowledgeService) {
        return rule -> {
            rule.addAclColumn(Wiki.class, ResourceType.WIKI, Permission.READ);
            // 文档读取继承知识库授权：文档级 ACL 优先，无文档 ACL 时回退到所属知识库
            rule.addAclColumn(Document.class, ResourceType.DOCUMENT, Permission.READ,
                    (resourceType, permission) -> knowledgeService.getRestrictedDocumentIdsWithWikiFallback(permission));
        };
    }

}
