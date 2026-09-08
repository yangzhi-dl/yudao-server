package cn.iocoder.yudao.module.ai.knowledge.document.service.acl;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai.knowledge.document.dal.dataobject.Document;
import cn.iocoder.yudao.module.ai.knowledge.document.dal.mysql.DocumentMapper;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import cn.iocoder.yudao.module.system.service.acl.metadata.ResourceMetadataProvider;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 文档（DOCUMENT）资源归属元数据 Provider
 *
 * <p>向 ACL 决策引擎提供文档的创建者与归属租户信息，替代原先在
 * {@code KnowledgeServiceImpl} 中每次现算 creatorIds / tenantIds 的做法。
 *
 * @author IIMS
 */
@Component
public class DocumentResourceMetadataProvider implements ResourceMetadataProvider {

    private final DocumentMapper documentMapper;

    public DocumentResourceMetadataProvider(DocumentMapper documentMapper) {
        this.documentMapper = documentMapper;
    }

    @Override
    public ResourceType resourceType() {
        return ResourceType.DOCUMENT;
    }

    @Override
    public boolean isCreator(Long resourceId, Long userId) {
        if (resourceId == null || userId == null) {
            return false;
        }
        return TenantUtils.executeIgnore(() -> {
            Document document = documentMapper.selectById(resourceId);
            return document != null && !Boolean.TRUE.equals(document.getDeleted())
                    && String.valueOf(userId).equals(document.getCreator());
        });
    }

    @Override
    public Long getTenantId(Long resourceId) {
        if (resourceId == null) {
            return null;
        }
        return TenantUtils.executeIgnore(() -> {
            Document document = documentMapper.selectById(resourceId);
            return document != null ? document.getTenantId() : null;
        });
    }

    @Override
    public Map<Long, Long> getTenantIds(Collection<Long> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return Map.of();
        }
        return TenantUtils.executeIgnore(() -> documentMapper.selectTenantIdMap(resourceIds));
    }

    @Override
    public Set<Long> selectCreatorIds(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        return new HashSet<>(documentMapper.selectIdsByCreator(String.valueOf(userId)));
    }

    @Override
    public Set<Long> selectTenantIds() {
        return new HashSet<>(documentMapper.selectIdsByTenant());
    }

    @Override
    public Set<Long> selectAllIds() {
        return TenantUtils.executeIgnore(() -> new HashSet<>(documentMapper.selectAllIds()));
    }
}
