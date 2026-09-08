package cn.iocoder.yudao.module.ai.core.tools;

import cn.iocoder.yudao.module.ai.common.model.entity.MarkdownConversionResult;
import cn.iocoder.yudao.module.ai.common.service.DocumentConversionService;
import cn.iocoder.yudao.module.ai.core.tools.factory.AITool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.List;
import java.util.Objects;

/**
 * 网络文件解析工具
 * 支持解析网络地址的文件内容为字符串（PDF、Word、Excel、PPT、TXT等）
 */
@Component
public class UrlFileParserTool implements AITool {

    private final DocumentConversionService documentConversionService;

    public UrlFileParserTool(DocumentConversionService documentConversionService) {
        this.documentConversionService = documentConversionService;
    }

    @Override
    public Object getToolInstance() {
        return this;
    }

    @Override
    public String getName() {
        return "url-file-parser-tools";
    }

    @Override
    public String getTitle() {
        return "网络文件解析工具箱";
    }

    @Override
    public String getDescription() {
        return "可以解析网络地址的文件内容为字符串，支持HTTP/HTTPS协议，支持PDF、Word、Excel、PPT、TXT等多种格式";
    }

    /**
     * 解析网络文件内容为字符串
     *
     * @param previewUrl 网络文件地址（http://或https://）
     * @return 解析后的文件内容字符串
     */
    @Tool(description = "解析网络地址的文件内容为字符串，支持HTTP/HTTPS协议，支持PDF、Word、Excel、PPT、TXT等格式")
    public String parseUrlFileToString(@ToolParam(description = "网络文件地址") String previewUrl,
                                       @ToolParam(description = "网络文件名称") String filename) {
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(previewUrl);
        List<Document> documents = tikaDocumentReader.get();
        List<String> originalContents = documents.stream()
                .map(Document::getText).filter(Objects::nonNull)
                .filter(StringUtils::isNoneBlank)
                .toList();

        if (originalContents.isEmpty()) {
            try {
                URL url = URI.create(previewUrl).toURL();
                try (InputStream inputStream = url.openStream()) {
                    List<MarkdownConversionResult> results = documentConversionService.convertDocumentImageSync(
                            inputStream, filename);
                    originalContents = results.stream().map(MarkdownConversionResult::getMarkdownContent).toList();
                }
            } catch (Exception e) {
                return e.getMessage();
            }
        }
        return String.join("\n", originalContents);
    }
}