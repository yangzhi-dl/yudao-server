package cn.iocoder.yudao.module.system.service.acl.metadata;

import cn.iocoder.yudao.module.system.enums.acl.ResourceType;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 资源归属元数据 Provider
 * <p>
 * 每个 {@link ResourceType} 由对应业务模块实现并注册，向 ACL 决策引擎提供资源的
 * 归属租户与创建者信息，从而替代原先各业务模块每次调用时现算 creatorIds / tenantIds 的做法。
 *
 * @author IIMS
 */
public interface ResourceMetadataProvider {

    /**
     * 当前 Provider 支持的资源类型
     */
    ResourceType resourceType();

    /**
     * 判断指定用户是否为指定资源的创建者
     */
    boolean isCreator(Long resourceId, Long userId);

    /**
     * 获取单个资源的归属租户
     */
    Long getTenantId(Long resourceId);

    /**
     * 批量获取资源的归属租户
     */
    Map<Long, Long> getTenantIds(Collection<Long> resourceIds);

    /**
     * 获取指定用户创建的资源 ID 集合
     */
    Set<Long> selectCreatorIds(Long userId);

    /**
     * 获取当前租户内的资源 ID 集合
     */
    Set<Long> selectTenantIds();

    /**
     * 获取该资源类型下的全部资源 ID 集合（跨租户）
     * <p>
     * 用于无候选集的全量受限 / 可访问判定，例如 {@code restrictedIds(type, permission)}。
     */
    Set<Long> selectAllIds();

}
