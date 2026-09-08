package cn.iocoder.yudao.module.ai.data.governance.service;

import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.ai.common.model.entity.MarkdownConversionResult;
import cn.iocoder.yudao.module.ai.common.service.DocumentConversionService;
import cn.iocoder.yudao.module.ai.data.collect.model.SourceFile;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 采集产物 Markdown 组装器。
 * <p>
 * 负责把不同来源的采集产物统一组装为 Markdown 正文：
 * <ul>
 *   <li>爬虫源：直接使用 {@link SourceFile#getMarkdown()}</li>
 *   <li>纯文本（md/txt）：按 UTF-8 读取</li>
 *   <li>Office / PDF：复用 {@link DocumentConversionService#convertDocumentFileSync} 转换</li>
 * </ul>
 */
@Component
public class DataMarkdownAssembler {

    private final DocumentConversionService documentConversionService;

    public DataMarkdownAssembler(DocumentConversionService documentConversionService) {
        this.documentConversionService = documentConversionService;
    }

    /**
     * 将采集产物组装为 Markdown。
     */
    public String assemble(SourceFile file) throws Exception {
        // 爬虫源已是 Markdown
        if (file.getMarkdown() != null) {
            return file.getMarkdown();
        }
        // 流式下载的临时文件
        if (file.getLocalFile() != null) {
            return assembleFromFile(file);
        }
        if (file.getContent() == null || file.getContent().length == 0) {
            return "";
        }
        // 纯文本直接读取
        if (isPlainText(file.getFileType())) {
            return new String(file.getContent(), StandardCharsets.UTF_8);
        }
        // Office / PDF 转换
        return joinMarkdown(documentConversionService.convertDocumentFileSync(
                new ByteArrayInputStream(file.getContent()),
                file.getFileName(),
                SecurityFrameworkUtils.getLoginUserId()));
    }

    private String assembleFromFile(SourceFile file) throws Exception {
        if (isPlainText(file.getFileType())) {
            return Files.readString(file.getLocalFile(), StandardCharsets.UTF_8);
        }
        try (InputStream in = Files.newInputStream(file.getLocalFile())) {
            return joinMarkdown(documentConversionService.convertDocumentFileSync(
                    in, file.getFileName(), SecurityFrameworkUtils.getLoginUserId()));
        }
    }

    private String joinMarkdown(List<MarkdownConversionResult> results) {
        return results.stream()
                .map(MarkdownConversionResult::getMarkdownContent)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n\n"));
    }

    private boolean isPlainText(String fileType) {
        return "md".equalsIgnoreCase(fileType) || "txt".equalsIgnoreCase(fileType);
    }

}
