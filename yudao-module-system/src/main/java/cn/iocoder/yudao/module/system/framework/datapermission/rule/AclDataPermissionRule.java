package cn.iocoder.yudao.module.system.framework.datapermission.rule;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.datapermission.core.rule.DataPermissionRule;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import cn.iocoder.yudao.module.system.service.acl.IPrincipalNameResolver;
import cn.iocoder.yudao.module.system.service.acl.engine.AclDecisionEngine;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基于 ACL 的 {@link DataPermissionRule} 数据权限规则实现
 * <p>
 * 通过 {@link AclDecisionEngine} 计算当前用户无权访问的资源 ID 列表，
 * 为业务表注入 WHERE resource_id NOT IN (...) 条件，实现 ACL 粒度的数据权限控制。
 * <p>
 * 底层租户隔离由租户插件负责，本规则只负责在租户隔离之上的细粒度授权过滤；
 * 创建者豁免、租管、跨租户授权等判定逻辑统一收敛在 {@link AclDecisionEngine} 中。
 * <p>
 * 使用方式：
 * <pre>{@code
 * rule.addAclColumn(Document.class, ResourceType.DOCUMENT, Permission.READ);
 * }</pre>
 *
 * @author IIMS
 */
@RequiredArgsConstructor
@Slf4j
public class AclDataPermissionRule implements DataPermissionRule {

    /**
     * ACL 决策引擎，用于计算当前用户无权访问的资源 ID
     */
    private final AclDecisionEngine aclDecisionEngine;

    /**
     * 主体名称解析器，用于判断超管
     */
    private final IPrincipalNameResolver principalNameResolver;

    /**
     * 表名 → ACL 表配置 的映射
     */
    private final Map<String, AclTableConfig> tableConfigs = new HashMap<>();

    /**
     * 所有注册的表名集合
     */
    private final Set<String> tableNames = new HashSet<>();

    @Override
    public Set<String> getTableNames() {
        return tableNames;
    }

    @Override
    public Expression getExpression(String tableName, Alias tableAlias) {
        // 只有有登陆用户的情况下，才进行数据权限的处理
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser == null) {
            return null;
        }

        // 获取该表的 ACL 配置
        AclTableConfig config = tableConfigs.get(tableName);
        if (config == null) {
            return null;
        }

        // 超管无需过滤
        if (principalNameResolver.isSuperAdmin(loginUser.getId())) {
            return null;
        }

        // 计算当前用户无权访问的资源 ID（租户隔离由租户插件负责）
        // 支持自定义解析器，用于知识库文档等「继承」语义的资源
        Set<Long> restrictedIds = config.restrictedIdsResolver() != null
                ? config.restrictedIdsResolver().resolveRestrictedIds(config.resourceType(), config.requiredPermission())
                : aclDecisionEngine.restrictedIds(config.resourceType(), config.requiredPermission());
        if (CollUtil.isEmpty(restrictedIds)) {
            // 无受限资源，无需过滤
            return null;
        }

        // 构建 resource_id NOT IN (...) 条件
        InExpression inExpression = new InExpression(
                MyBatisUtils.buildColumn(tableName, tableAlias, config.resourceIdColumn()),
                new ParenthesedExpressionList<>(
                        new ExpressionList<>(
                                restrictedIds.stream()
                                        .map(LongValue::new)
                                        .collect(Collectors.toList())
                        )
                )
        );
        inExpression.setNot(true);
        return inExpression;
    }

    // ==================== 添加配置 ====================

    /**
     * 添加 ACL 数据权限映射（资源 ID 列默认为 "id"）
     *
     * @param entityClass        实体类
     * @param resourceType       资源类型
     * @param requiredPermission 需要的权限
     */
    public void addAclColumn(Class<?> entityClass, ResourceType resourceType, Permission requiredPermission) {
        addAclColumn(entityClass, "id", resourceType, requiredPermission, null);
    }

    /**
     * 添加 ACL 数据权限映射（资源 ID 列默认为 "id"，并使用自定义受限集合解析器）
     *
     * @param entityClass          实体类
     * @param resourceType         资源类型
     * @param requiredPermission   需要的权限
     * @param restrictedIdsResolver 受限 ID 集合自定义解析器
     */
    public void addAclColumn(Class<?> entityClass, ResourceType resourceType, Permission requiredPermission,
                             AclRestrictedIdsResolver restrictedIdsResolver) {
        addAclColumn(entityClass, "id", resourceType, requiredPermission, restrictedIdsResolver);
    }

    /**
     * 添加 ACL 数据权限映射（自定义资源 ID 列名）
     *
     * @param entityClass        实体类
     * @param resourceIdColumn   资源 ID 列名
     * @param resourceType       资源类型
     * @param requiredPermission 需要的权限
     */
    public void addAclColumn(Class<?> entityClass, String resourceIdColumn, ResourceType resourceType, Permission requiredPermission) {
        addAclColumn(entityClass, resourceIdColumn, resourceType, requiredPermission, null);
    }

    /**
     * 添加 ACL 数据权限映射（自定义资源 ID 列名 + 自定义受限集合解析器）
     */
    public void addAclColumn(Class<?> entityClass, String resourceIdColumn, ResourceType resourceType, Permission requiredPermission,
                             AclRestrictedIdsResolver restrictedIdsResolver) {
        String tableName = TableInfoHelper.getTableInfo(entityClass).getTableName();
        addAclColumn(tableName, resourceIdColumn, resourceType, requiredPermission, restrictedIdsResolver);
    }

    /**
     * 添加 ACL 数据权限映射（直接指定表名 + 资源 ID 列名）
     *
     * @param tableName          表名
     * @param resourceIdColumn   资源 ID 列名
     * @param resourceType       资源类型
     * @param requiredPermission 需要的权限
     */
    public void addAclColumn(String tableName, String resourceIdColumn, ResourceType resourceType, Permission requiredPermission) {
        addAclColumn(tableName, resourceIdColumn, resourceType, requiredPermission, null);
    }

    /**
     * 添加 ACL 数据权限映射（直接指定表名 + 资源 ID 列名 + 自定义受限集合解析器）
     */
    public void addAclColumn(String tableName, String resourceIdColumn, ResourceType resourceType, Permission requiredPermission,
                             AclRestrictedIdsResolver restrictedIdsResolver) {
        tableConfigs.put(tableName, new AclTableConfig(resourceType, resourceIdColumn, requiredPermission, restrictedIdsResolver));
        tableNames.add(tableName);
    }

    // ==================== 内部类 ====================

    private record AclTableConfig(ResourceType resourceType, String resourceIdColumn,
                                  Permission requiredPermission, AclRestrictedIdsResolver restrictedIdsResolver) {
    }

}
