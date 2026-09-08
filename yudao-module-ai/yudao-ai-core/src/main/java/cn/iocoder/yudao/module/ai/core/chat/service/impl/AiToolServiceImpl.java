package cn.iocoder.yudao.module.ai.core.chat.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.core.chat.enums.McpTransmissionProtocol;
import cn.iocoder.yudao.module.ai.core.chat.dal.mysql.AiToolMapper;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.ModelToolDO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.CreateToolDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ToolPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.UpdateToolDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.DatabaseStatementConfig;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.DatabaseToolConfig;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.McpConfig;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ToolFunction;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ToolSettings;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.SelectToolVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.ToolDetailVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.ToolPageVO;
import cn.iocoder.yudao.module.ai.core.chat.service.AiToolService;
import cn.iocoder.yudao.module.ai.core.chat.utils.ChatToolUtil;
import cn.iocoder.yudao.module.ai.core.tools.factory.AITool;
import cn.iocoder.yudao.module.ai.core.tools.factory.ToolFactory;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.client.transport.customizer.McpSyncHttpClientRequestCustomizer;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Slf4j
@Service
public class AiToolServiceImpl implements AiToolService {

    private final AiToolMapper aiToolMapper;

    private final ToolFactory toolFactory;

    private final ChatToolUtil chatToolUtil;

    public AiToolServiceImpl(AiToolMapper aiToolMapper, ToolFactory toolFactory, ChatToolUtil chatToolUtil) {
        this.aiToolMapper = aiToolMapper;
        this.toolFactory = toolFactory;
        this.chatToolUtil = chatToolUtil;
    }

    @Override
    public List<ToolPageVO> getAllSystemTools() {
        List<AITool> allTools = toolFactory.getAllTools();
        List<ToolPageVO> tools = new ArrayList<>();
        List<String> existingNames = new ArrayList<>(aiToolMapper.hasUniqueNames(
                allTools.stream().map(AITool::getName).toList()
        ));
        existingNames.add("skill-workspace");
        allTools = allTools.stream()
                .filter(aiTool -> !existingNames.contains(aiTool.getName()))
                .toList();
        for (AITool allTool : allTools) {
            ToolPageVO build = ToolPageVO.builder()
                    .name(allTool.getName())
                    .rename(allTool.getTitle())
                    .description(allTool.getDescription())
                    .build();

            Object toolInstance = allTool.getToolInstance();
            Class<?> clazz = toolInstance.getClass();

            List<ToolFunction> functions = Arrays.stream(clazz.getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(Tool.class))
                    .map(chatToolUtil::extractToolFunctionFromMethod)
                    .toList();

            build.setFunctions(functions);
            tools.add(build);
        }
        return tools;
    }

    @Override
    public PageResult<ToolPageVO> pageQuery(ToolPageQueryDTO dto) {
        List<ToolPageVO> list = new ArrayList<>();
        Page<ModelToolDO> modelTools = aiToolMapper.pageQuery(new Page<>(dto.getPage(), dto.getPageSize()), dto);
        List<ModelToolDO> records = modelTools.getRecords();
        records.forEach(modelTool -> list.add(ToolPageVO.builder().id(modelTool.getId())
                .isOnline(modelTool.getIsOnline()).name(modelTool.getName())
                .rename(modelTool.getRename()).type(modelTool.getType())
                .description(modelTool.getDescription()).createTime(modelTool.getCreateTime())
                .functions(modelTool.getFunctions()).build()));
        return new PageResult<>(list, modelTools.getTotal());
    }

    @Override
    public Boolean createTool(CreateToolDTO dto) {
        return aiToolMapper.insert(ModelToolDO.builder().name(dto.getName())
                .rename(dto.getRename()).functions(dto.getFunctions()).description(dto.getDescription())
                .settings(dto.getSettings()).type(dto.getType()).build()) > 0;
    }
    @Override
    public Boolean pingTool(Long id) {
        ModelToolDO modelTool = aiToolMapper.selectById(id);
        if (Objects.isNull(modelTool)) {
            return false;
        }

        List<ToolFunction> functions;
        try {
            functions = getToolFunctions(modelTool);
        } catch (Exception e) {
            if (modelTool.getIsOnline()) {
                aiToolMapper.updateOnlineStatus(id, false);
            }
            return false;
        }

        // 更新在线状态
        if (!modelTool.getIsOnline()) {
            aiToolMapper.updateOnlineStatus(id, true);
        }

        aiToolMapper.update(ModelToolDO.builder().id(id).functions(functions).build());
        return true;
    }

    private List<ToolFunction> getToolFunctions(ModelToolDO modelTool) {
        return switch (modelTool.getType()) {
            case MCP_TOOL -> getMcpFunctions(modelTool);
            case HTTP_DYNAMIC_TOOL -> getHttpFunctions(modelTool);
            case DATABASE_TOOL -> getDatabaseFunctions(modelTool);
            case SYSTEM_TOOL -> getSystemFunctions(modelTool);
        };
    }

    private List<ToolFunction> getMcpFunctions(ModelToolDO modelTool) {
        McpConfig mcpConfig = modelTool.getSettings().getMcpConfig();
        McpSyncHttpClientRequestCustomizer authCustomizer = (builder, method, uri, body, ctx) -> {
            if (mcpConfig.getHeader() != null && !mcpConfig.getHeader().isEmpty()) {
                mcpConfig.getHeader().forEach(builder::header);
            }
        };

        McpClientTransport transport = createTransport(mcpConfig, authCustomizer);
        if (Objects.isNull(transport)) {
            throw new RuntimeException("不支持的传输协议类型：" + mcpConfig.getTp());
        }
        try (McpSyncClient client = McpClient.sync(transport)
                .clientInfo(new McpSchema.Implementation(mcpConfig.getName(), mcpConfig.getVersion()))
                .capabilities(McpSchema.ClientCapabilities.builder().roots(true).build())
                .build()) {

            return client.listTools().tools().stream()
                    .map(tool -> ToolFunction.builder()
                            .name(tool.name())
                            .title(tool.title())
                            .description(tool.description())
                            .build())
                    .toList();
        }
    }

    private List<ToolFunction> getHttpFunctions(ModelToolDO modelTool) {
        return modelTool.getSettings().getHttpConfigs().stream()
                .map(httpConfig -> ToolFunction.builder()
                        .name(httpConfig.getName())
                        .description(httpConfig.getDescription())
                        .build())
                .toList();
    }

    private List<ToolFunction> getDatabaseFunctions(ModelToolDO modelTool) {
        List<ToolFunction> functions = new ArrayList<>();
        for (DatabaseToolConfig databaseConfig : modelTool.getSettings().getDatabaseConfigs()) {
            // 固定执行语句作为独立功能
            if (databaseConfig.getStatements() != null) {
                for (DatabaseStatementConfig statement : databaseConfig.getStatements()) {
                    functions.add(ToolFunction.builder()
                            .name(statement.getName())
                            .description(statement.getDescription())
                            .build());
                }
            }
            // 允许自由查询时，连接配置本身作为一个功能
            if (Boolean.TRUE.equals(databaseConfig.getAllowFreeQuery())) {
                functions.add(ToolFunction.builder()
                        .name(databaseConfig.getName())
                        .description(databaseConfig.getDescription())
                        .build());
            }
        }
        return functions;
    }

    private List<ToolFunction> getSystemFunctions(ModelToolDO modelTool) {
        return toolFactory.getAllTools().stream()
                .filter(tool -> Objects.equals(tool.getName(), modelTool.getName()))
                .findFirst()
                .map(AITool::getToolInstance)
                .map(this::extractToolFunctions)
                .orElseGet(() -> {
                    aiToolMapper.updateOnlineStatus(modelTool.getId(), false);
                    return Collections.emptyList();
                });
    }

    private List<ToolFunction> extractToolFunctions(Object toolInstance) {
        return Arrays.stream(toolInstance.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Tool.class))
                .map(chatToolUtil::extractToolFunctionFromMethod)
                .toList();
    }

    @Override
    public List<ToolFunction> getMcpServiceToolsByConfig(McpConfig config) {
        return getMcpServiceTools(config);
    }

    @Override
    public void updateOnlineStatus(Long id, Boolean isOnline) {
        aiToolMapper.updateOnlineStatus(id, true);
    }

    @Override
    public Boolean updateTool(UpdateToolDTO dto) {
        return aiToolMapper.update(ModelToolDO.builder().id(dto.getId()).name(dto.getName())
                .rename(dto.getRename()).functions(dto.getFunctions()).description(dto.getDescription())
                .settings(dto.getSettings()).type(dto.getType()).build());
    }

    @Override
    public ToolDetailVO detail(Long id) {
        ModelToolDO modelTool = aiToolMapper.selectById(id);
        return ToolDetailVO.builder().id(modelTool.getId()).type(modelTool.getType())
                .name(modelTool.getName()).rename(modelTool.getRename())
                .description(modelTool.getDescription()).functions(modelTool.getFunctions())
                .settings(modelTool.getSettings()).build();
    }

    @Override
    public Boolean delete(List<Long> ids) {
        return aiToolMapper.delete(ids);
    }

    @Override
    public PageResult<SelectToolVO> selectPageQuery(ToolPageQueryDTO dto) {
        List<SelectToolVO> list = new ArrayList<>();
        Page<ModelToolDO> modelTools = aiToolMapper.pageQuery(new Page<>(dto.getPage(), dto.getPageSize()), dto);
        List<ModelToolDO> records = modelTools.getRecords();
        records.forEach(modelTool -> {
            ToolSettings toolConfig = modelTool.getSettings();
            SelectToolVO build = SelectToolVO.builder().id(modelTool.getId()).isOnline(modelTool.getIsOnline())
                    .name(modelTool.getName()).rename(modelTool.getRename()).type(modelTool.getType())
                    .description(modelTool.getDescription()).functions(modelTool.getFunctions()).build();
            if (Objects.nonNull(toolConfig)) {
                build.setStyle(toolConfig.getStyle());
            }
            list.add(build);
        });
        return new PageResult<>(list, modelTools.getTotal());
    }

    @Override
    public List<ModelToolDO> getOnlineToolsByIds(List<Long> enabledToolIds) {
        if (Objects.isNull(enabledToolIds) || enabledToolIds.isEmpty()) {
            return new ArrayList<>();
        }
        return aiToolMapper.getOnlineToolsByIds(enabledToolIds);
    }

    @Override
    public List<McpSyncClient> loadingMcpSyncClients(List<ModelToolDO> modelTools) {
        List<McpSyncClient> mcpSyncClients = new ArrayList<>();

        modelTools.forEach(modelTool -> {
            ToolSettings toolConfig = modelTool.getSettings();
            if (Objects.nonNull(toolConfig)) {
                McpConfig mcpConfig = toolConfig.getMcpConfig();
                if (Objects.nonNull(mcpConfig)) {
                    try {
                        McpSyncClient client = createMcpSyncClient(mcpConfig);
                        mcpSyncClients.add(client);
                    } catch (Exception e) {
                        log.error("创建MCP同步客户端失败，工具名称：{}，配置：{}",
                                modelTool.getName(), mcpConfig, e);
                    }
                }
            }
        });

        return mcpSyncClients;
    }

    @Override
    public Integer getToolSize() {
        return aiToolMapper.getToolSize();
    }

    /**
     * 创建MCP同步客户端
     */
    private McpSyncClient createMcpSyncClient(McpConfig config) {
        McpSyncHttpClientRequestCustomizer authCustomizer = (builder, method, uri, body, ctx) -> {
            if (config.getHeader() != null && !config.getHeader().isEmpty()) {
                config.getHeader().forEach(builder::header);
            }
        };

        McpClientTransport transport = createTransport(config, authCustomizer);

        if (transport == null) {
            throw new RuntimeException("不支持的传输协议类型：" + config.getTp());
        }

        return McpClient.sync(transport)
                .clientInfo(new McpSchema.Implementation(config.getName(), config.getVersion()))
                .capabilities(McpSchema.ClientCapabilities.builder().roots(true).build())
                .build();
    }

    private List<ToolFunction> getMcpServiceTools(McpConfig config) {
        McpSyncHttpClientRequestCustomizer authCustomizer = (builder, method, uri, body, ctx) -> {
            if (config.getHeader() != null && !config.getHeader().isEmpty()) {
                config.getHeader().forEach(builder::header);
            }
        };

        McpClientTransport transport = createTransport(config, authCustomizer);

        if (Objects.isNull(transport)) {
            throw new RuntimeException("不支持的传输协议类型：" + config.getTp());
        }

        try (McpSyncClient build = McpClient.sync(transport)
                .clientInfo(new McpSchema.Implementation(config.getName(), config.getVersion()))
                .capabilities(McpSchema.ClientCapabilities.builder().roots(true).build())
                .build()) {
            List<ToolFunction> toolFunctions = new ArrayList<>();
            McpSchema.ListToolsResult listToolsResult = build.listTools();
            listToolsResult.tools().forEach(tool -> toolFunctions.add(ToolFunction.builder().name(tool.name())
                    .title(tool.title()).description(tool.description()).build()));
            return toolFunctions;
        } catch (Exception e) {
            throw new RuntimeException(String.format("连接MCP服务失败：%s", e.getMessage()));
        }
    }

    /**
     * 创建传输对象
     */
    private McpClientTransport createTransport(McpConfig config, McpSyncHttpClientRequestCustomizer authCustomizer) {
        String url = config.getUrl();
        String endpoint = config.getEndpoint();
        Duration connectTimeout = Duration.ofSeconds(config.getConnectTimeout());
        if (Objects.equals(config.getTp(), McpTransmissionProtocol.STREAMABLE_HTTP)) {
            return HttpClientStreamableHttpTransport.builder(url)
                    .endpoint(endpoint)
                    .httpRequestCustomizer(authCustomizer)
                    .connectTimeout(connectTimeout)
                    .build();
        } else if (Objects.equals(config.getTp(), McpTransmissionProtocol.SSE)) {
            return HttpClientSseClientTransport.builder(url)
                    .sseEndpoint(endpoint)
                    .httpRequestCustomizer(authCustomizer)
                    .connectTimeout(connectTimeout)
                    .build();
        }
        return null;
    }


}
