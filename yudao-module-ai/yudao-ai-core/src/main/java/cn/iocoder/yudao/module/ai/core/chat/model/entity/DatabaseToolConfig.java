package cn.iocoder.yudao.module.ai.core.chat.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 数据库连接器工具配置
 * <p>
 * 每个连接配置可以包含多个固定执行语句（作为独立工具），
 * 也可开启自由查询（允许模型自建 SQL 语句）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseToolConfig {

    /**
     * 工具名称（唯一标识，作为自由查询工具名）
     */
    private String name;

    /**
     * 工具描述
     */
    private String description;

    /**
     * 数据库类型（如：postgresql / mysql）
     */
    private String type;

    private String jdbcUrl;

    private String username;

    private String password;

    private String driverClassName;

    /**
     * 连接池配置
     */
    private Integer maximumPoolSize;

    private Integer minimumIdle;

    private Long connectionTimeout;

    private Boolean readOnly;

    /**
     * 固定执行语句列表（每条语句对应一个工具，仅允许 SELECT）
     */
    private List<DatabaseStatementConfig> statements;

    /**
     * 是否允许自由查询（模型自建 SQL 语句，仅允许 SELECT）
     */
    private Boolean allowFreeQuery;
}