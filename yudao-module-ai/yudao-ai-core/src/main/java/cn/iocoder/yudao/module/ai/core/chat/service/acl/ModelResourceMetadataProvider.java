package cn.iocoder.yudao.module.ai.core.chat.service.acl;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiModelDO;
import cn.iocoder.yudao.module.ai.core.chat.dal.mysql.AiModelMapper;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import cn.iocoder.yudao.module.system.service.acl.metadata.ResourceMetadataProvider;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 模型（MODEL）资源归属元数据 Provider
 *
 * <p>向 ACL 决策引擎提供模型的创建者与归属租户信息，替代原先在
 * {@code AiModelServiceImpl} 中每次现算 creatorIds / tenantIds 的做法。
 *
 * @author IIMS
 */
@Component
public class ModelResourceMetadataProvider implements ResourceMetadataProvider {

    private final AiModelMapper aiModelMapper;

    public ModelResourceMetadataProvider(AiModelMapper aiModelMapper) {
        this.aiModelMapper = aiModelMapper;
    }

    @Override
    public ResourceType resourceType() {
        return ResourceType.MODEL;
    }

    @Override
    public boolean isCreator(Long resourceId, Long userId) {
        if (resourceId == null || userId == null) {
            return false;
        }
        return TenantUtils.executeIgnore(() -> {
            AiModelDO model = aiModelMapper.selectById(resourceId);
            return model != null && !Boolean.TRUE.equals(model.getDeleted())
                    && String.valueOf(userId).equals(model.getCreator());
        });
    }

    @Override
    public Long getTenantId(Long resourceId) {
        if (resourceId == null) {
            return null;
        }
        return TenantUtils.executeIgnore(() -> {
            AiModelDO model = aiModelMapper.selectById(resourceId);
            return model != null ? model.getTenantId() : null;
        });
    }

    @Override
    public Map<Long, Long> getTenantIds(Collection<Long> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return Map.of();
        }
        return TenantUtils.executeIgnore(() -> aiModelMapper.selectTenantIdMap(resourceIds));
    }

    @Override
    public Set<Long> selectCreatorIds(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        return new HashSet<>(aiModelMapper.selectIdsByCreator(String.valueOf(userId)));
    }

    @Override
    public Set<Long> selectTenantIds() {
        return new HashSet<>(aiModelMapper.selectIdsByTenant());
    }

    @Override
    public Set<Long> selectAllIds() {
        return TenantUtils.executeIgnore(() -> new HashSet<>(aiModelMapper.selectAllIds()));
    }
}
