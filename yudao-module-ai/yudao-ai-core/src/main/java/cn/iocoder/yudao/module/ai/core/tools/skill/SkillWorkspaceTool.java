package cn.iocoder.yudao.module.ai.core.tools.skill;

import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiSkillDO;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiSkillResourceDO;
import cn.iocoder.yudao.module.ai.core.chat.dal.mysql.AiSkillMapper;
import cn.iocoder.yudao.module.ai.core.chat.dal.mysql.AiSkillResourceMapper;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.FileInfo;
import cn.iocoder.yudao.module.ai.core.tools.factory.AITool;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.agentscope.runtime.sandbox.box.BaseSandbox;
import io.agentscope.runtime.sandbox.manager.SandboxService;
import io.agentscope.runtime.sandbox.manager.fs.local.LocalFileSystemConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 技能工作区工具
 * <p>
 * 为 AI 技能提供完整的沙箱工作区能力，包括：
 * <ul>
 *   <li><b>工作区根目录</b> —— 获取当前用户的工作区路径</li>
 *   <li><b>文件写入</b> —— 在工作区中创建文件（JSON、Python、Shell 等）</li>
 *   <li><b>文件读取</b> —— 读取工作区中已生成的文件</li>
 *   <li><b>脚本执行</b> —— 使用 spring-ai-alibaba-sandbox（Docker 隔离）执行脚本</li>
 *   <li><b>文件列表</b> —— 列出工作区中的文件</li>
 *   <li><b>技能脚本</b> —— 列出和执行技能内置的脚本资源</li>
 *   <li><b>文件呈现</b> —— 将生成的文件上传到文件服务并呈现给用户</li>
 * </ul>
 * <p>
 * 工作区路径：{@code {java.io.tmpdir}/skill-workspace/{userId}/}，按用户隔离。
 * <p>
 * 路径规则：模型以 {@code /} 作为虚拟根目录操作文件，系统将其直接拼接到实际工作区根目录。
 * 例如：模型传入 {@code /data/plan.json}，实际路径为 {@code {workspaceRoot}/data/plan.json}。
 * <p>
 * 沙箱集成：通过 {@code ai.sandbox.enabled=true}（默认启用）启用 Docker 沙箱。
 * 每个用户复用独立的 {@link BaseSandbox} 实例，工作区通过 nonCopyMount 直接 bind mount 到容器内 {@code /data}，
 * 脚本在沙箱中写入的文件会直接出现在本地工作区，可通过 {@code presentFiles} 导出。
 * Python 脚本通过 {@code runIpythonCell} 执行，Shell 脚本通过 {@code runShellCommand} 执行。
 */
@Slf4j
@Component
public class SkillWorkspaceTool implements AITool {

    private static final int MAX_OUTPUT_LENGTH = 50_000;
    private static final Path BASE_ROOT = Paths.get(System.getProperty("java.io.tmpdir"), "skill-workspace");
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    private final AiSkillMapper skillMapper;
    private final AiSkillResourceMapper skillResourceMapper;
    private final FileService fileService;

    /**
     * 沙箱服务（可选注入，Docker 不可用时为 null）
     */
    @Autowired(required = false)
    private SandboxService sandboxService;

    /**
     * daemon 侧的工作区共享目录（跨主机场景）。
     * <p>
     * 当 app 跑在 Linux 容器、沙箱连接远端（如 Windows）Docker 时，容器内工作区路径（{@code {tmpdir}/skill-workspace}）
     * 无法被远端 daemon 解析，必须用 daemon 侧可见的共享目录路径作为 bind mount 源（如 {@code /mnt/host/d/skill-workspace}）。
     * 配置为空时回退到本机/容器内路径（本机直连时 Docker Desktop 会自动翻译 Windows 路径）。
     */
    @Value("${iims.sandbox.workspace-mount:}")
    private String workspaceMount;

    /**
     * 用户级沙箱缓存，key 为 userId
     */
    private final ConcurrentHashMap<String, BaseSandbox> sandboxCache = new ConcurrentHashMap<>();

    public SkillWorkspaceTool(AiSkillMapper skillMapper,
                              AiSkillResourceMapper skillResourceMapper,
                              FileService fileService) {
        this.skillMapper = skillMapper;
        this.skillResourceMapper = skillResourceMapper;
        this.fileService = fileService;
    }

    @Override
    public Object getToolInstance() {
        return this;
    }

    @Override
    public String getName() {
        return "skill-workspace";
    }

    @Override
    public String getTitle() {
        return "技能工作区";
    }

    @Override
    public String getDescription() {
        return "在沙箱工作区中写入文件、读取文件、执行脚本、列出文件，为技能执行提供完整的工作区能力";
    }

    // ==================== 文件操作 ====================

    /**
     * 在工作区中写入文件。路径以 / 为虚拟根目录，系统直接拼接到实际工作区根目录。
     */
    @Tool(description = """
            在沙箱工作区中创建或覆盖文件。支持创建 JSON、Python、Shell、文本等任意类型的文件。
            写入的文件可在后续步骤中通过 readFile 读取，或通过 executeScript 执行。
            工作区根目录为 "/"，传入的路径会直接拼接到实际工作区根目录。
            参数：
            - filePath: 文件路径（以 "/" 为根，如 "/plan.json"、"/scripts/run.py"）
            - content: 文件内容（完整文本）
            """)
    public String writeFile(
            @ToolParam(description = "文件路径（以 / 为根，如 '/plan.json'、'/scripts/run.py'）") String filePath,
            @ToolParam(description = "文件内容（完整文本）") String content) {

        if (filePath == null || filePath.isBlank()) {
            return "错误：请提供文件路径";
        }
        if (content == null) {
            content = "";
        }

        try {
            Path root = getUserWorkspaceRoot();
            Files.createDirectories(root);
            Path targetPath = resolveFilePath(root, filePath);

            if (!targetPath.startsWith(root)) {
                return "错误：不允许访问工作区外的路径";
            }

            Files.createDirectories(targetPath.getParent());
            Files.writeString(targetPath, content);

            log.info("写入文件: {} ({} 字符)", targetPath, content.length());
            return "文件已写入: " + toVirtualPath(root, targetPath) + " (" + content.length() + " 字符)";

        } catch (IOException e) {
            log.error("写入文件失败: {}", filePath, e);
            return "写入文件失败: " + e.getMessage();
        }
    }

    /**
     * 从工作区中读取文件
     */
    @Tool(description = """
            从沙箱工作区中读取文件内容。可读取之前 writeFile 写入的文件，或技能脚本执行后生成的输出文件。
            工作区根目录为 "/"，传入的路径会直接拼接到实际工作区根目录。
            参数：
            - filePath: 文件路径（以 "/" 为根，如 "/plan.json"、"/outputs/result.txt"）
            """)
    public String readFile(
            @ToolParam(description = "文件路径（以 / 为根，如 '/plan.json'、'/outputs/result.txt'）") String filePath) {

        if (filePath == null || filePath.isBlank()) {
            return "错误：请提供文件路径";
        }

        try {
            Path root = getUserWorkspaceRoot();
            Path targetPath = resolveFilePath(root, filePath);

            if (!targetPath.startsWith(root)) {
                return "错误：不允许访问工作区外的路径";
            }

            if (!Files.exists(targetPath)) {
                return "文件不存在: " + toVirtualPath(root, targetPath)
                        + "。请使用 listFiles 查看已有文件。";
            }

            if (Files.isDirectory(targetPath)) {
                return "路径是一个目录，请使用 listFiles: " + toVirtualPath(root, targetPath);
            }

            String content = Files.readString(targetPath);
            if (content.length() > MAX_OUTPUT_LENGTH) {
                content = content.substring(0, MAX_OUTPUT_LENGTH)
                        + "\n... (文件已截断，总大小: " + content.length() + " 字符)";
            }

            return content;

        } catch (IOException e) {
            log.error("读取文件失败: {}", filePath, e);
            return "读取文件失败: " + e.getMessage();
        }
    }

    /**
     * 列出工作区中的文件
     */
    @Tool(description = """
            列出沙箱工作区中的文件。可按目录筛选。
            工作区根目录为 "/"，传入的路径会直接拼接到实际工作区根目录。
            参数：
            - directoryPath: 目录路径（以 "/" 为根，可选，默认列出根目录，可传空字符串 '' 或 '/'）
            """)
    public String listFiles(
            @ToolParam(description = "目录路径（以 / 为根，可选，默认列出根目录，可传空字符串 '' 或 '/'）") String directoryPath) {

        try {
            Path root = getUserWorkspaceRoot();
            Path targetPath;
            if (directoryPath != null && !directoryPath.isBlank() && !"/".equals(directoryPath)) {
                targetPath = resolveFilePath(root, directoryPath);
            } else {
                targetPath = root;
            }

            if (!targetPath.startsWith(root)) {
                return "错误：不允许访问工作区外的路径";
            }

            if (!Files.exists(targetPath)) {
                return "目录不存在: " + toVirtualPath(root, targetPath);
            }

            try (Stream<Path> stream = Files.walk(targetPath, 5)) {
                List<String> entries = stream
                        .filter(p -> !p.equals(targetPath))
                        .map(p -> {
                            String relative = toVirtualPath(root, p);
                            String type = Files.isDirectory(p) ? "[DIR]" : "[FILE]";
                            String size = "";
                            if (!Files.isDirectory(p)) {
                                try {
                                    size = " (" + Files.size(p) + " bytes)";
                                } catch (IOException ignored) {
                                }
                            }
                            return "  " + type + "  " + relative + size;
                        })
                        .collect(Collectors.toList());

                if (entries.isEmpty()) {
                    return "目录为空: " + toVirtualPath(root, targetPath)
                            + "。工作区根目录: /";
                }

                return "=== 工作区文件 (根目录: /) ===\n" + String.join("\n", entries);
            }

        } catch (IOException e) {
            log.error("列出文件失败: {}", directoryPath, e);
            return "列出文件失败: " + e.getMessage();
        }
    }

    // ==================== 脚本执行 ====================

    /**
     * 在工作区中执行脚本文件
     */
    @Tool(description = """
            在 Docker 沙箱中执行工作区中的脚本文件。支持 Python (.py) 和 Shell (.sh) 脚本。
            脚本在 Docker 隔离沙箱中运行，工作区目录已挂载到容器的 /data。
            脚本中写入的文件（如 PPT、PDF、图片等）会直接出现在本地工作区，可通过 presentFiles 导出。
            注意：脚本中引用工作区文件的路径需使用 /data 前缀（例如 /data/data/input.json）。
            脚本文件必须已通过 writeFile 写入工作区。
            工作区根目录为 "/"，传入的路径会直接拼接到实际工作区根目录。
            参数：
            - filePath: 脚本文件路径（以 "/" 为根，如 "/scripts/run.py"）
            - arguments: 传递给脚本的参数（可选）
            """)
    public String executeScript(
            @ToolParam(description = "脚本文件路径（以 / 为根，如 '/scripts/run.py'）") String filePath,
            @ToolParam(description = "传递给脚本的参数（可选）") String arguments) {

        if (filePath == null || filePath.isBlank()) {
            return "错误：请提供脚本文件路径";
        }

        try {
            Path root = getUserWorkspaceRoot();
            Path scriptPath = resolveFilePath(root, filePath);

            if (!scriptPath.startsWith(root)) {
                return "错误：不允许访问工作区外的路径";
            }

            if (!Files.exists(scriptPath)) {
                return "文件不存在: " + toVirtualPath(root, scriptPath)
                        + "。请先使用 writeFile 创建脚本文件。";
            }

            String scriptContent = Files.readString(scriptPath);
            if (scriptContent.isBlank()) {
                return "脚本文件为空: " + toVirtualPath(root, scriptPath);
            }

            String fileName = scriptPath.getFileName().toString();
            String[] args = parseArguments(arguments);

            long startTime = System.currentTimeMillis();
            SandboxResult result = executeInSandbox(scriptContent, fileName, scriptPath.getParent(), args);
            long elapsed = System.currentTimeMillis() - startTime;

            return "=== 脚本执行: " + toVirtualPath(root, scriptPath) + " (耗时 " + elapsed + "ms) ===\n"
                    + sanitizeOutput(root, result.output());

        } catch (IOException e) {
            log.error("执行脚本失败: {}", filePath, e);
            return "执行脚本失败: " + e.getMessage();
        }
    }

    // ==================== 技能资源脚本 ====================

    /**
     * 执行技能内置的脚本资源
     */
    @Tool(description = """
            在 Docker 沙箱中执行指定技能的内置脚本资源。脚本从技能资源库中读取，在 Docker 沙箱中执行。
            工作区目录已挂载到容器的 /data，脚本写入的文件可直接通过 presentFiles 导出。
            支持的文件类型：.py (Python)、.sh (Bash)
            参数：
            - skillName: 技能名称（在 Available Skills 列表中显示的名称）
            - resourceName: 脚本资源名称（可选，不指定则按顺序执行所有脚本）
            - arguments: 传递给脚本的参数（可选）
            """)
    public String executeSkillScript(
            @ToolParam(description = "技能名称（在 Available Skills 列表中显示的名称）") String skillName,
            @ToolParam(description = "脚本资源名称（可选，不指定则执行所有脚本）") String resourceName,
            @ToolParam(description = "传递给脚本的参数（可选）") String arguments) {

        if (skillName == null || skillName.isBlank()) {
            return "错误：请提供技能名称（在 Available Skills 列表中查看）";
        }

        try {
            AiSkillDO skill = skillMapper.selectByName(skillName.trim());
            if (skill == null) {
                return "错误：未找到名称为 '" + skillName + "' 的技能。请使用 read_skill 查看可用技能名称。";
            }
            if (skill.getStatus() != null && skill.getStatus() == 0) {
                return "错误：技能 '" + skill.getDisplayName() + "' 已被禁用";
            }

            Long skillId = skill.getId();
            List<AiSkillResourceDO> resources = skillResourceMapper.selectListBySkillId(skillId);
            List<AiSkillResourceDO> scripts = resources.stream()
                    .filter(r -> r.getResourceType() != null && r.getResourceType() == 0)
                    .sorted(Comparator.comparing(r -> r.getSortOrder() != null ? r.getSortOrder() : 0))
                    .toList();

            if (scripts.isEmpty()) {
                return "技能 '" + skill.getDisplayName() + "' 没有可执行的脚本资源。"
                        + "该技能的描述为：" + skill.getDescription();
            }

            List<AiSkillResourceDO> targetScripts;
            if (resourceName != null && !resourceName.isBlank()) {
                targetScripts = scripts.stream()
                        .filter(s -> {
                            String name = getFileName(s.getFileId());
                            return name != null && name.contains(resourceName);
                        })
                        .toList();
                if (targetScripts.isEmpty()) {
                    List<String> availableNames = scripts.stream()
                            .map(s -> getFileName(s.getFileId()))
                            .toList();
                    return "未找到匹配的脚本资源 '" + resourceName + "'。"
                            + "可用的脚本资源：" + availableNames;
                }
            } else {
                targetScripts = scripts;
            }

            Path root = getUserWorkspaceRoot();
            Files.createDirectories(root);

            StringBuilder results = new StringBuilder();
            results.append("=== 技能脚本执行: ").append(skill.getDisplayName()).append(" ===\n\n");

            for (int i = 0; i < targetScripts.size(); i++) {
                AiSkillResourceDO script = targetScripts.get(i);
                try {
                    String scriptContent = readFileContent(script.getFileId());
                    if (scriptContent == null || scriptContent.isEmpty()) {
                        results.append("[脚本 ").append(i + 1).append("] 文件内容为空或无法读取\n");
                        continue;
                    }

                    String fileName = getFileName(script.getFileId());
                    String[] args = parseArguments(arguments);

                    long startTime = System.currentTimeMillis();
                    SandboxResult result = executeInSandbox(scriptContent, fileName, root, args);
                    long elapsed = System.currentTimeMillis() - startTime;

                    results.append("[脚本 ").append(i + 1).append("] ").append(fileName)
                            .append(" (耗时 ").append(elapsed).append("ms)\n");
                    results.append(sanitizeOutput(root, result.output())).append("\n");

                    if (i < targetScripts.size() - 1) {
                        results.append("\n");
                    }
                } catch (Exception e) {
                    log.error("执行技能脚本失败: skillId={}, resourceId={}", skillId, script.getId(), e);
                    results.append("[脚本 ").append(i + 1).append("] 执行失败: ").append(e.getMessage()).append("\n");
                }
            }

            results.append("\n=== 执行结束 ===");
            return results.toString();

        } catch (Exception e) {
            log.error("技能脚本执行异常: skillName={}", skillName, e);
            return "技能脚本执行异常: " + e.getMessage();
        }
    }

    /**
     * 列出技能的所有脚本资源
     */
    @Tool(description = "列出指定技能的所有可执行脚本资源")
    public String listSkillScripts(
            @ToolParam(description = "技能名称（在 Available Skills 列表中显示的名称）") String skillName) {

        if (skillName == null || skillName.isBlank()) {
            return "错误：请提供技能名称（在 Available Skills 列表中查看）";
        }

        try {
            AiSkillDO skill = skillMapper.selectByName(skillName.trim());
            if (skill == null) {
                return "错误：未找到名称为 '" + skillName + "' 的技能。请使用 read_skill 查看可用技能名称。";
            }

            Long skillId = skill.getId();
            List<AiSkillResourceDO> resources = skillResourceMapper.selectListBySkillId(skillId);
            List<AiSkillResourceDO> scripts = resources.stream()
                    .filter(r -> r.getResourceType() != null && r.getResourceType() == 0)
                    .sorted(Comparator.comparing(r -> r.getSortOrder() != null ? r.getSortOrder() : 0))
                    .toList();

            if (scripts.isEmpty()) {
                return "技能 '" + skill.getDisplayName() + "' 没有可执行的脚本资源";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("=== 技能脚本: ").append(skill.getDisplayName()).append(" ===\n");
            for (int i = 0; i < scripts.size(); i++) {
                AiSkillResourceDO script = scripts.get(i);
                String fileName = getFileName(script.getFileId());
                String type = detectScriptType(fileName);
                sb.append(i + 1).append(". ").append(fileName)
                        .append(" [").append(type).append("]")
                        .append(" (资源ID: ").append(script.getId()).append(")\n");
            }
            return sb.toString();

        } catch (Exception e) {
            log.error("列出技能脚本异常: skillName={}", skillName, e);
            return "列出技能脚本异常: " + e.getMessage();
        }
    }

    // ==================== 文件呈现 ====================

    /**
     * 将工作区中的文件上传到文件服务并呈现给用户
     */
    @Tool(description = """
            将工作区中生成的文件上传到文件服务并呈现给用户。
            文件会在聊天界面中以文件卡片形式展示，用户可点击预览或下载。
            工作区根目录为 "/"，传入的路径会直接拼接到实际工作区根目录。
            参数：
            - filePaths: 文件路径列表（以 "/" 为根，如 ["/output.pptx", "/images/chart.png"]）
            """)
    public String presentFiles(
            @ToolParam(description = "文件路径列表（以 / 为根，如 ['/output.pptx', '/images/chart.png']）") List<String> filePaths) {

        if (filePaths == null || filePaths.isEmpty()) {
            return "错误：请提供至少一个文件路径";
        }

        try {
            Path root = getUserWorkspaceRoot();
            List<FileInfo> fileInfos = new java.util.ArrayList<>();
            for (String filePath : filePaths) {
                try {
                    Path targetPath = resolveFilePath(root, filePath);
                    if (!targetPath.startsWith(root)) {
                        fileInfos.add(FileInfo.builder()
                                .filename(filePath).id(null).fileSize(null)
                                .url(null).fileType("error")
                                .build());
                        continue;
                    }
                    if (!Files.exists(targetPath)) {
                        fileInfos.add(FileInfo.builder()
                                .filename(filePath).id(null).fileSize(null)
                                .url(null).fileType("not_found")
                                .build());
                        continue;
                    }
                    if (Files.isDirectory(targetPath)) {
                        fileInfos.add(FileInfo.builder()
                                .filename(filePath).id(null).fileSize(null)
                                .url(null).fileType("is_directory")
                                .build());
                        continue;
                    }

                    byte[] content = Files.readAllBytes(targetPath);
                    String fileName = targetPath.getFileName().toString();
                    String type = getMimeType(fileName);

                    FileDO fileDO = fileService.createFileToData(content, fileName, "skill-output", type);
                    String previewUrl = fileService.presignGetUrl(fileDO.getPath(), 600);

                    fileInfos.add(FileInfo.builder()
                            .id(fileDO.getId())
                            .filename(fileName)
                            .fileSize(fileDO.getSize())
                            .fileType(type)
                            .url(previewUrl)
                            .build());

                    log.info("文件已呈现: {} -> fileId={}, url={}", filePath, fileDO.getId(), previewUrl);
                } catch (Exception e) {
                    log.error("呈现文件失败: {}", filePath, e);
                    fileInfos.add(FileInfo.builder()
                            .filename(filePath).id(null).fileSize(null)
                            .url(null).fileType("upload_error")
                            .build());
                }
            }

            JSONArray array = new JSONArray();
            fileInfos.stream().map(f -> {
                JSONObject obj = new JSONObject();
                obj.put("id", f.getId());
                obj.put("filename", f.getFilename());
                obj.put("fileSize", f.getFileSize());
                obj.put("fileType", f.getFileType());
                obj.put("url", f.getUrl());
                return obj;
            }).forEach(array::add);
            return array.toJSONString();

        } catch (Exception e) {
            log.error("呈现文件异常", e);
            return "[]";
        }
    }

    private String getMimeType(String fileName) {
        if (fileName == null) return "application/octet-stream";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pptx")) return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        if (lower.endsWith(".ppt")) return "application/vnd.ms-powerpoint";
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lower.endsWith(".csv")) return "text/csv";
        if (lower.endsWith(".json")) return "application/json";
        if (lower.endsWith(".txt")) return "text/plain";
        if (lower.endsWith(".html")) return "text/html";
        if (lower.endsWith(".md")) return "text/markdown";
        if (lower.endsWith(".py")) return "text/x-python";
        if (lower.endsWith(".js")) return "text/javascript";
        return "application/octet-stream";
    }

    // ==================== 沙箱执行 ====================

    /**
     * 在 Docker 沙箱中执行脚本。仅支持 Python (.py) 和 Shell (.sh) 脚本。
     */
    private SandboxResult executeInSandbox(String scriptContent, String fileName, Path workDir, String... arguments) {
        String command = detectCommand(fileName);
        if (command == null) {
            return SandboxResult.error("不支持的文件类型: " + fileName
                    + "，仅支持 .py / .sh");
        }

        if (sandboxService == null) {
            return SandboxResult.error("沙箱服务不可用，请确保 Docker 已启动且 ai.sandbox.enabled=true");
        }

        if (!command.equals("python") && !command.equals("python3") && !command.equals("bash")) {
            return SandboxResult.error("不支持的文件类型: " + fileName
                    + "，仅支持 .py 和 .sh 脚本");
        }

        return executeInDockerSandbox(scriptContent, fileName, command, arguments);
    }

    /**
     * 使用 Docker 沙箱执行 Python / Shell 脚本
     */
    private SandboxResult executeInDockerSandbox(String scriptContent, String fileName,
                                                  String command, String... arguments) {
        long startTime = System.currentTimeMillis();
        try {
            BaseSandbox sandbox = getOrCreateSandbox();
            String output;

            if (command.equals("python") || command.equals("python3")) {
                String code = buildPythonCode(scriptContent, arguments);
                output = sandbox.runIpythonCell(code);
            } else {
                String shellCommand = buildShellCommand(scriptContent, arguments);
                output = sandbox.runShellCommand(shellCommand);
            }

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("沙箱执行完成: {} ({}ms)", fileName, elapsed);
            return new SandboxResult(true, truncate(output), 0);

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("沙箱执行异常: {} ({}ms)", fileName, elapsed, e);
            return SandboxResult.error("沙箱执行异常: " + e.getMessage());
        }
    }

    /**
     * 构建 Python 代码，设置工作目录为 /data 并注入 sys.argv 参数。
     * /data 是宿主机工作区在容器内的挂载点，脚本写入的文件会直接出现在本地工作区。
     */
    private String buildPythonCode(String scriptContent, String... arguments) {
        StringBuilder code = new StringBuilder("import os\nos.chdir('/data')\n");
        if (arguments != null && arguments.length > 0) {
            code.append("import sys\nsys.argv = ['script.py'");
            for (String arg : arguments) {
                code.append(", '").append(arg.replace("'", "\\'")).append("'");
            }
            code.append("]\n");
        }
        code.append(scriptContent);
        return code.toString();
    }

    /**
     * 构建 Shell 命令，设置工作目录为 /data 并注入参数。
     * /data 是宿主机工作区在容器内的挂载点，脚本写入的文件会直接出现在本地工作区。
     */
    private String buildShellCommand(String scriptContent, String... arguments) {
        StringBuilder cmd = new StringBuilder("cd /data\n").append(scriptContent);
        if (arguments != null) {
            for (String arg : arguments) {
                cmd.append(" '").append(arg.replace("'", "'\\''")).append("'");
            }
        }
        return cmd.toString();
    }

    /**
     * 获取或创建当前用户的沙箱实例，并将工作区目录挂载到容器的 /workspace。
     */
    private BaseSandbox getOrCreateSandbox() {
        String userId;
        try {
            userId = String.valueOf(SecurityFrameworkUtils.getLoginUserId());
        } catch (Exception e) {
            userId = "default";
        }
        return sandboxCache.computeIfAbsent(userId, id -> {
            Path workspaceRoot = getUserWorkspaceRoot();
            try {
                Files.createDirectories(workspaceRoot);
            } catch (IOException e) {
                log.warn("创建工作区目录失败: {}", workspaceRoot, e);
            }
            String mountSource = resolveMountSource(workspaceRoot, id);
            // 库创建容器前会对非拷贝挂载源做 File.exists() 检查，不存在则跳过挂载导致容器内 /data 缺失。
            // 跨主机场景 daemon 侧路径（如 /run/desktop/mnt/host/d/skill-workspace/{id}）在 app 容器内可能不存在，
            // 这里主动创建（仅用于通过存在性检查，daemon 仍挂载真实 D:\skill-workspace\{id}）。
            try {
                Files.createDirectories(Paths.get(mountSource));
            } catch (IOException e) {
                log.warn("创建 daemon 挂载源目录失败，可能导致沙箱 /data 未挂载: {}", mountSource, e);
            }
            LocalFileSystemConfig fsConfig = LocalFileSystemConfig.builder()
                    .addNonCopyMount(mountSource, "/data")
                    .build();
            log.info("为用户 {} 创建沙箱实例，挂载工作区 {} -> /data", id, mountSource);
            return new BaseSandbox(sandboxService, id, "skill-" + id, fsConfig);
        });
    }

    /**
     * 计算传给 Docker daemon 的挂载源路径。
     * <p>
     * 配置了 {@code iims.sandbox.workspace-mount} 时，使用该 daemon 侧共享目录 + userId
     * （跨主机场景：app 容器内工作区与 daemon 侧共享同一份数据，需用 daemon 可解析的路径）；
     * 否则回退到本机/容器内工作区路径（本机直连时 Docker Desktop 会自动翻译 Windows 路径）。
     */
    private String resolveMountSource(Path workspaceRoot, String userId) {
        if (StringUtils.hasText(workspaceMount)) {
            return Paths.get(workspaceMount).resolve(userId).toAbsolutePath().toString();
        }
        return workspaceRoot.toAbsolutePath().toString();
    }

    /**
     * 清理指定用户的沙箱实例
     */
    public void cleanupSandbox(String userId) {
        BaseSandbox sandbox = sandboxCache.remove(userId);
        if (sandbox != null) {
            try {
                log.info("清理用户 {} 的沙箱实例", userId);
                sandbox.close();
            } catch (Exception e) {
                log.warn("清理沙箱实例失败: userId={}", userId, e);
            }
        }
    }

    /**
     * 应用启动后预初始化沙箱，提前拉取 Docker 镜像并创建容器，
     * 避免首次脚本执行时等待。不挂载工作区，仅做预热。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void preInitializeSandbox() {
        if (sandboxService == null) {
            log.info("沙箱服务未启用，跳过预初始化");
            return;
        }
        try {
            log.info("预初始化沙箱（拉取镜像 + 创建容器）...");
            BaseSandbox warmup = new BaseSandbox(sandboxService, "warmup", "warmup");
            warmup.close();
            log.info("沙箱预初始化完成");
        } catch (Exception e) {
            log.warn("沙箱预初始化失败: {}", e.getMessage());
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取当前用户的工作区根目录。若无法获取用户 ID，使用 "default" 作为默认值。
     */
    private Path getUserWorkspaceRoot() {
        try {
            Long userId = SecurityFrameworkUtils.getLoginUserId();
            return BASE_ROOT.resolve(String.valueOf(userId));
        } catch (Exception e) {
            log.warn("获取登录用户 ID 失败，使用默认工作区", e);
            return BASE_ROOT.resolve("default");
        }
    }

    /**
     * 解析文件路径。将模型传入的路径（以 / 为虚拟根）直接拼接到实际工作区根目录。
     * <ul>
     *   <li>{@code "/data/plan.json"} → {@code workspaceRoot/data/plan.json}</li>
     *   <li>{@code "plan.json"} → {@code workspaceRoot/plan.json}</li>
     *   <li>{@code "./plan.json"} → {@code workspaceRoot/plan.json}</li>
     * </ul>
     */
    private Path resolveFilePath(Path root, String filePath) {
        String normalized = filePath;
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.startsWith("./")) {
            normalized = normalized.substring(2);
        }
        return root.resolve(normalized).normalize();
    }

    /**
     * 将相对于实际工作区根目录的路径转换为虚拟路径（以 / 开头）
     */
    private String toVirtualPath(Path root, Path targetPath) {
        return "/" + root.relativize(targetPath).toString().replace("\\", "/");
    }

    /**
     * 将脚本输出中的绝对路径替换为虚拟路径，让模型看到的都是工作区相对路径。
     * 处理 Windows 路径分隔符（反斜杠）和正斜杠两种形式。
     */
    private String sanitizeOutput(Path root, String output) {
        if (output == null || output.isEmpty()) {
            return output;
        }
        String rootAbs = root.toAbsolutePath().normalize().toString();
        String rootAbsForward = rootAbs.replace("\\", "/");
        // 替换 Windows 反斜杠格式的绝对路径
        String sanitized = output.replace(rootAbs, "/");
        // 替换正斜杠格式的绝对路径
        sanitized = sanitized.replace(rootAbsForward, "/");
        return sanitized;
    }

    private String readFileContent(Long fileId) {
        try {
            FileDO file = fileService.getFile(fileId);
            if (file == null) return null;
            byte[] content = fileService.getFileContent(file.getConfigId(), file.getPath());
            return content != null ? new String(content, StandardCharsets.UTF_8) : null;
        } catch (Exception e) {
            log.error("读取文件内容失败: fileId={}", fileId, e);
            return null;
        }
    }

    private String getFileName(Long fileId) {
        try {
            FileDO file = fileService.getFile(fileId);
            return file != null ? file.getName() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String detectCommand(String fileName) {
        if (fileName == null) return null;
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".py")) return IS_WINDOWS ? "python" : "python3";
        if (lower.endsWith(".js")) return "node";
        if (lower.endsWith(".sh")) return "bash";
        if (lower.endsWith(".bat") || lower.endsWith(".cmd")) {
            return IS_WINDOWS ? "cmd /c" : null;
        }
        if (lower.endsWith(".ps1")) return IS_WINDOWS ? "powershell -File" : "pwsh -File";
        return null;
    }

    private String detectScriptType(String fileName) {
        if (fileName == null) return "未知";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".py")) return "Python";
        if (lower.endsWith(".js")) return "Node.js";
        if (lower.endsWith(".sh")) return "Shell";
        if (lower.endsWith(".bat")) return "Batch";
        if (lower.endsWith(".cmd")) return "Batch";
        if (lower.endsWith(".ps1")) return IS_WINDOWS ? "PowerShell" : "PowerShell Core";
        return "未知";
    }

    private String[] parseArguments(String arguments) {
        if (arguments == null || arguments.isBlank()) {
            return new String[0];
        }
        return arguments.trim().split("\\s+");
    }

    private String truncate(String output) {
        if (output.length() > MAX_OUTPUT_LENGTH) {
            return output.substring(0, MAX_OUTPUT_LENGTH)
                    + "\n... (输出已截断，超过 " + MAX_OUTPUT_LENGTH + " 字符)";
        }
        return output;
    }

    public record SandboxResult(boolean success, String output, int exitCode) {
        public static SandboxResult error(String message) {
            return new SandboxResult(false, message, -1);
        }
    }
}