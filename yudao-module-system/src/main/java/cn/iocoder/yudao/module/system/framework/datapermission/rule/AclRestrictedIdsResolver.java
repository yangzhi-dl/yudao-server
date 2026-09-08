package cn.iocoder.yudao.module.system.framework.datapermission.rule;

import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;

import java.util.Set;

/**
 * ACL 数据权限受限 ID 集合的自定义解析器
 * <p>
 * 默认情况下 {@link AclDataPermissionRule} 通过 {@code AclDecisionEngine.restrictedIds} 计算
 * 当前用户无权访问的资源 ID。部分资源存在「继承」语义（如知识库文档可回退到知识库授权），
 * 业务模块可通过实现本接口注入自定义的受限集合计算逻辑。
 *
 * @author IIMS
 */
@FunctionalInterface
public interface AclRestrictedIdsResolver {

    /**
     * 计算当前用户无权访问的资源 ID 集合
     *
     * @param resourceType       资源类型
     * @param requiredPermission 需要的权限
     * @return 无权访问的资源 ID 集合
     */
    Set<Long> resolveRestrictedIds(ResourceType resourceType, Permission requiredPermission);

}
