package cn.iocoder.yudao.module.ai.core.tools.callback;

import cn.iocoder.yudao.module.ai.core.chat.model.entity.DatabaseParameterDef;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.DatabaseStatementConfig;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.DatabaseToolConfig;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.DefaultToolMetadata;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 可配置的数据库连接器工具回调
 * <p>
 * 一个数据库连接配置（{@link DatabaseToolConfig}）可生成多个工具：
 * <ul>
 *   <li><b>固定执行语句</b>：{@link DatabaseStatementConfig} 每条语句对应一个独立工具，
 *       模型按配置参数调用，执行配置好的查询模板。</li>
 *   <li><b>自由查询</b>：开启 {@code allowFreeQuery} 后，工具名取连接配置的 name，
 *       模型传入 sql 自行构建查询语句。</li>
 * </ul>
 * 所有执行的 SQL 均限制为 SELECT 查询。
 */
public class DatabaseToolCallback implements ToolCallback {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}");

    private final DatabaseStatementConfig statement;
    private final ToolDefinition toolDefinition;
    private final ToolMetadata toolMetadata;
    private final HikariDataSource dataSource;

    /**
     * 自由查询模式：工具名取连接配置 name，由模型传入 sql。
     */
    public DatabaseToolCallback(DatabaseToolConfig config) {
        this(config, null);
    }

    /**
     * 固定语句模式：按 {@link DatabaseStatementConfig} 生成一个工具。
     */
    public DatabaseToolCallback(DatabaseToolConfig config, DatabaseStatementConfig statement) {
        this.statement = statement;
        this.dataSource = new HikariDataSource(buildDataSourceConfig(config));

        this.toolDefinition = DefaultToolDefinition.builder()
                .name(statement != null ? statement.getName() : config.getName())
                .description(statement != null ? statement.getDescription() : config.getDescription())
                .inputSchema(buildInputSchema())
                .build();

        this.toolMetadata = DefaultToolMetadata.builder().build();
    }

    @Override
    @NotNull
    public String call(@NotNull String toolInput) {
        try {
            JSONObject arguments = JSONObject.parseObject(toolInput);
            Map<String, Object> argMap = arguments != null ? arguments : new HashMap<>();

            String sql;
            if (statement != null) {
                // 固定语句：将参数替换进 SQL 模板
                Map<String, Object> resolvedArgs = applyDefaultValues(argMap);
                sql = resolveStatementSql(statement.getSql(), resolvedArgs);
            } else {
                // 自由查询：模型直接提供 sql
                sql = arguments != null ? arguments.getString("sql") : null;
            }

            if (!StringUtils.hasText(sql)) {
                return "参数错误: 缺少SQL查询语句";
            }

            // 安全检查：只允许 SELECT 查询语句
            validateSqlIsSelect(sql);

            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
            return JSON.toJSONString(result);
        } catch (IllegalArgumentException e) {
            return "参数错误: " + e.getMessage();
        } catch (SecurityException e) {
            return "安全错误: " + e.getMessage();
        } catch (Exception e) {
            return "查询错误: " + e.getMessage();
        }
    }

    private String buildInputSchema() {
        if (statement == null || statement.getParameters() == null || statement.getParameters().isEmpty()) {
            // 自由查询或无参数：仅需 sql
            Map<String, Object> schema = new HashMap<>();
            schema.put("$schema", "https://json-schema.org/draft/2020-12/schema");
            schema.put("type", "object");

            Map<String, Object> sqlSchema = new HashMap<>();
            sqlSchema.put("type", "string");
            sqlSchema.put("description", "要执行的SELECT查询SQL语句（必须使用数据库中真实存在的表名和列名）");
            schema.put("properties", Map.of("sql", sqlSchema));
            schema.put("required", List.of("sql"));
            schema.put("additionalProperties", false);
            return JSON.toJSONString(schema);
        }

        // 固定语句：按参数定义构建输入结构
        Map<String, Object> schema = new HashMap<>();
        schema.put("$schema", "https://json-schema.org/draft/2020-12/schema");
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        List<String> required = new ArrayList<>();
        for (DatabaseParameterDef param : statement.getParameters()) {
            Map<String, Object> paramSchema = new HashMap<>();
            paramSchema.put("type", getJsonType(param.getType()));
            paramSchema.put("description", param.getDescription() != null ? param.getDescription() : "");
            if (param.getEnumValues() != null && !param.getEnumValues().isEmpty()) {
                paramSchema.put("enum", param.getEnumValues());
            }
            properties.put(param.getName(), paramSchema);

            if (Boolean.TRUE.equals(param.getRequired())) {
                required.add(param.getName());
            }
        }

        schema.put("properties", properties);
        if (!required.isEmpty()) {
            schema.put("required", required);
        }
        schema.put("additionalProperties", false);
        return JSON.toJSONString(schema);
    }

    private String getJsonType(String type) {
        if (type == null) return "string";
        return switch (type.toLowerCase()) {
            case "integer", "long", "int", "short", "byte" -> "integer";
            case "number", "double", "float", "decimal" -> "number";
            case "boolean", "bool" -> "boolean";
            case "array", "list", "set" -> "array";
            case "object", "map" -> "object";
            default -> "string";
        };
    }

    /**
     * 应用固定语句参数的默认值，并转换为目标类型。
     */
    private Map<String, Object> applyDefaultValues(Map<String, Object> args) {
        Map<String, Object> inputArgs = args != null ? args : Collections.emptyMap();
        Map<String, Object> result = new HashMap<>(inputArgs);

        if (statement.getParameters() != null) {
            for (DatabaseParameterDef param : statement.getParameters()) {
                String paramName = param.getName();
                if (!result.containsKey(paramName) || result.get(paramName) == null) {
                    if (param.getDefaultValue() != null) {
                        result.put(paramName, convertToType(param.getDefaultValue().toString(), param.getType()));
                    }
                }
            }
        }
        return result;
    }

    /**
     * 将固定语句 SQL 模板中的 ${参数名} 替换为实际参数值。
     */
    private String resolveStatementSql(String sql, Map<String, Object> args) {
        if (sql == null) {
            throw new IllegalArgumentException("固定语句的 SQL 不能为空");
        }
        Matcher matcher = PLACEHOLDER.matcher(sql);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String paramName = matcher.group(1);
            Object value = args.get(paramName);
            if (value == null) {
                throw new IllegalArgumentException("缺少语句参数: " + paramName);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(value.toString()));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private Object convertToType(String value, String targetType) {
        if (value == null || targetType == null) {
            return value;
        }
        try {
            return switch (targetType.toLowerCase()) {
                case "integer", "long", "int", "short", "byte" -> Long.parseLong(value);
                case "number", "double", "float", "decimal" -> Double.parseDouble(value);
                case "boolean", "bool" -> Boolean.parseBoolean(value);
                default -> value;
            };
        } catch (NumberFormatException e) {
            return value;
        }
    }

    /**
     * 安全检查 - 只允许 SELECT 查询语句
     */
    private void validateSqlIsSelect(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL 语句不能为空");
        }

        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (!(statement instanceof Select)) {
                throw new SecurityException("只允许执行 SELECT 查询语句，不允许执行 " +
                        statement.getClass().getSimpleName());
            }
        } catch (JSQLParserException e) {
            throw new IllegalArgumentException("SQL 语法错误: " + e.getMessage(), e);
        }
    }

    @NotNull
    private static HikariConfig buildDataSourceConfig(DatabaseToolConfig config) {
        if (!StringUtils.hasText(config.getJdbcUrl())) {
            throw new IllegalArgumentException("数据库连接地址(jdbcUrl)不能为空");
        }
        if (!StringUtils.hasText(config.getDriverClassName())) {
            throw new IllegalArgumentException("数据库驱动配置不能为空");
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(config.getDriverClassName());
        hikariConfig.setJdbcUrl(config.getJdbcUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setMaximumPoolSize(config.getMaximumPoolSize() != null ? config.getMaximumPoolSize() : 5);
        hikariConfig.setMinimumIdle(config.getMinimumIdle() != null ? config.getMinimumIdle() : 1);
        hikariConfig.setConnectionTimeout(config.getConnectionTimeout() != null ? config.getConnectionTimeout() : 10000);
        hikariConfig.setReadOnly(config.getReadOnly() != null ? config.getReadOnly() : true);
        return hikariConfig;
    }

    @Override
    @NotNull
    public ToolDefinition getToolDefinition() {
        return toolDefinition;
    }

    @Override
    @NotNull
    public ToolMetadata getToolMetadata() {
        return toolMetadata;
    }
}