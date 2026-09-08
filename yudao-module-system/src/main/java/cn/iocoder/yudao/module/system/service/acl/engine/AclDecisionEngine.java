package cn.iocoder.yudao.module.system.service.acl.engine;

import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;

import java.util.Collection;
import java.util.Set;

/**
 * ACL 决策引擎门面
 * <p>
 * 对业务模块暴露统一的访问决策 API，内部通过责任链 + 主体匹配策略完成判定。
 * 原 {@code ResourceAclService} 中的访问判定逻辑已收敛到本引擎，避免决策逻辑散落在多个方法中。
 *
 * @author IIMS
 */
public interface AclDecisionEngine {

    /**
     * 检查当前用户是否有权访问指定资源
     */
    boolean canAccess(ResourceType resourceType, Long resourceId, Permission permission);

    /**
     * 从候选资源集合中筛选出当前用户有权访问的资源 ID
     */
    Set<Long> accessibleIds(ResourceType resourceType, Permission permission, Collection<Long> candidateIds);

    /**
     * 从候选资源集合中筛选出当前用户无权访问的资源 ID
     */
    Set<Long> restrictedIds(ResourceType resourceType, Permission permission, Collection<Long> candidateIds);

    /**
     * 获取当前用户无权访问的全部资源 ID（以该资源类型全量 ID 作为候选集）
     * <p>
     * 适用于业务侧拿不到明确候选集的场景，内部通过 {@link cn.iocoder.yudao.module.system.service.acl.metadata.ResourceMetadataProvider#selectAllIds()} 获取全量 ID。
     */
    Set<Long> restrictedIds(ResourceType resourceType, Permission permission);

    /**
     * 获取当前用户对指定资源拥有的有效 ACL 权限名称集合
     */
    Set<String> userPermissions(ResourceType resourceType, Long resourceId);

    /**
     * 判断当前用户是否为指定资源的创建者
     * <p>
     * 供列表场景标记资源是否为「授权过来」的内容：非创建者即可见即视为被授权共享。
     */
    boolean isCreator(ResourceType resourceType, Long resourceId);

}
