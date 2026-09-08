package cn.iocoder.yudao.module.ai.core.chat.service.acl;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiAgentDO;
import cn.iocoder.yudao.module.ai.core.chat.dal.mysql.AiAgentMapper;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import cn.iocoder.yudao.module.system.service.acl.metadata.ResourceMetadataProvider;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 智能体（AGENT）资源归属元数据 Provider
 *
 * <p>向 ACL 决策引擎提供智能体的创建者与归属租户信息，替代原先在
 * {@code AiAgentServiceImpl} 中每次现算 creatorIds / tenantIds 的做法。
 *
 * @author IIMS
 */
@Component
public class AgentResourceMetadataProvider implements ResourceMetadataProvider {

    private final AiAgentMapper aiAgentMapper;

    public AgentResourceMetadataProvider(AiAgentMapper aiAgentMapper) {
        this.aiAgentMapper = aiAgentMapper;
    }

    @Override
    public ResourceType resourceType() {
        return ResourceType.AGENT;
    }

    @Override
    public boolean isCreator(Long resourceId, Long userId) {
        if (resourceId == null || userId == null) {
            return false;
        }
        return TenantUtils.executeIgnore(() -> {
            AiAgentDO agent = aiAgentMapper.selectAgentById(resourceId);
            return agent != null && String.valueOf(userId).equals(agent.getCreator());
        });
    }

    @Override
    public Long getTenantId(Long resourceId) {
        if (resourceId == null) {
            return null;
        }
        return TenantUtils.executeIgnore(() -> {
            AiAgentDO agent = aiAgentMapper.selectAgentById(resourceId);
            return agent != null ? agent.getTenantId() : null;
        });
    }

    @Override
    public Map<Long, Long> getTenantIds(Collection<Long> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return Map.of();
        }
        return TenantUtils.executeIgnore(() -> aiAgentMapper.selectTenantIdMap(resourceIds));
    }

    @Override
    public Set<Long> selectCreatorIds(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        return new HashSet<>(aiAgentMapper.selectIdsByCreator(String.valueOf(userId)));
    }

    @Override
    public Set<Long> selectTenantIds() {
        return new HashSet<>(aiAgentMapper.selectIdsByTenant());
    }

    @Override
    public Set<Long> selectAllIds() {
        return TenantUtils.executeIgnore(() -> new HashSet<>(aiAgentMapper.selectAllIds()));
    }
}
