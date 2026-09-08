package cn.iocoder.yudao.module.ai.core.chat.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 数据库固定执行语句配置
 * <p>
 * 每条固定语句对应一个独立工具，仅允许 SELECT 查询。
 * SQL 中可用 ${参数名} 占位符引用 {@link #parameters} 中定义的参数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseStatementConfig {

    /**
     * 语句工具名称（唯一标识）
     */
    private String name;

    /**
     * 语句工具描述
     */
    private String description;

    /**
     * 固定查询语句（仅允许 SELECT，可含 ${参数名} 占位符）
     */
    private String sql;

    /**
     * 语句参数定义
     */
    private List<DatabaseParameterDef> parameters;
}