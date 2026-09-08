package cn.iocoder.yudao.module.ai.knowledge.wiki.service.acl;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai.knowledge.wiki.dal.dataobject.Wiki;
import cn.iocoder.yudao.module.ai.knowledge.wiki.dal.mysql.WikiMapper;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import cn.iocoder.yudao.module.system.service.acl.metadata.ResourceMetadataProvider;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 知识库（WIKI）资源归属元数据 Provider
 *
 * <p>向 ACL 决策引擎提供知识库的创建者与归属租户信息，替代原先在
 * {@code KnowledgeServiceImpl} 中每次现算 creatorWikiIds / tenantWikiIds 的做法。
 *
 * @author IIMS
 */
@Component
public class WikiResourceMetadataProvider implements ResourceMetadataProvider {

    private final WikiMapper wikiMapper;

    public WikiResourceMetadataProvider(WikiMapper wikiMapper) {
        this.wikiMapper = wikiMapper;
    }

    @Override
    public ResourceType resourceType() {
        return ResourceType.WIKI;
    }

    @Override
    public boolean isCreator(Long resourceId, Long userId) {
        if (resourceId == null || userId == null) {
            return false;
        }
        return TenantUtils.executeIgnore(() -> {
            Wiki wiki = wikiMapper.selectById(resourceId);
            return wiki != null && !Boolean.TRUE.equals(wiki.getDeleted())
                    && String.valueOf(userId).equals(wiki.getCreator());
        });
    }

    @Override
    public Long getTenantId(Long resourceId) {
        if (resourceId == null) {
            return null;
        }
        return TenantUtils.executeIgnore(() -> {
            Wiki wiki = wikiMapper.selectById(resourceId);
            return wiki != null ? wiki.getTenantId() : null;
        });
    }

    @Override
    public Map<Long, Long> getTenantIds(Collection<Long> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return Map.of();
        }
        return TenantUtils.executeIgnore(() -> wikiMapper.selectTenantIdMap(resourceIds));
    }

    @Override
    public Set<Long> selectCreatorIds(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        return new HashSet<>(wikiMapper.selectIdsByCreator(String.valueOf(userId)));
    }

    @Override
    public Set<Long> selectTenantIds() {
        return new HashSet<>(wikiMapper.selectIdsByTenant());
    }

    @Override
    public Set<Long> selectAllIds() {
        return TenantUtils.executeIgnore(() -> new HashSet<>(wikiMapper.selectAllIds()));
    }
}
