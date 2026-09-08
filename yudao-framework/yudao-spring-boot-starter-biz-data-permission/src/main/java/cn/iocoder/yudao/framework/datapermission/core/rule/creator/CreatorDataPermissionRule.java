package cn.iocoder.yudao.framework.datapermission.core.rule.creator;

import cn.iocoder.yudao.framework.common.biz.system.permission.PermissionCommonApi;
import cn.iocoder.yudao.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;
import cn.iocoder.yudao.framework.datapermission.core.rule.DataPermissionRule;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 基于 creator 创建者的数据权限规则
 * <p>
 * 集成 yudao 数据权限体系（ALL / SELF），
 * 自动为注册的表注入 WHERE creator = ? 条件。
 *
 * @author IIMS
 */
public class CreatorDataPermissionRule implements DataPermissionRule {

    private final PermissionCommonApi permissionApi;

    /**
     * 基于 creator 创建者的表字段配置
     * key：表名
     * value：字段名（默认 creator）
     */
    private final Map<String, String> creatorColumns = new HashMap<>();
    private final Set<String> tableNames = new HashSet<>();

    public CreatorDataPermissionRule(PermissionCommonApi permissionApi) {
        this.permissionApi = permissionApi;
    }

    @Override
    public Set<String> getTableNames() {
        return tableNames;
    }

    @Override
    public Expression getExpression(String tableName, Alias tableAlias) {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser == null) {
            return null;
        }

        String columnName = creatorColumns.get(tableName);
        if (columnName == null) {
            return null;
        }

        // 获取用户数据权限
        DeptDataPermissionRespDTO dataPermission = permissionApi.getDeptDataPermission(loginUser.getId());
        if (dataPermission == null) {
            return new EqualsTo(null, null); // 无权限，返回空数据
        }

        // ALL 权限：可查看全部数据，不限制
        if (Boolean.TRUE.equals(dataPermission.getAll())) {
            return null;
        }

        // SELF 权限：只能查看自己的数据
        if (Boolean.TRUE.equals(dataPermission.getSelf())) {
            return new EqualsTo(
                    MyBatisUtils.buildColumn(tableName, tableAlias, columnName),
                    new StringValue(String.valueOf(loginUser.getId()))
            );
        }

        // 既没有 ALL 也没有 SELF → 无数据
        return new EqualsTo(null, null);
    }

    // ==================== 添加配置 ====================

    public void addCreatorColumn(Class<?> entityClass) {
        addCreatorColumn(entityClass, "creator");
    }

    public void addCreatorColumn(Class<?> entityClass, String columnName) {
        String tableName = TableInfoHelper.getTableInfo(entityClass).getTableName();
        addCreatorColumn(tableName, columnName);
    }

    public void addCreatorColumn(String tableName, String columnName) {
        creatorColumns.put(tableName, columnName);
        tableNames.add(tableName);
    }

}