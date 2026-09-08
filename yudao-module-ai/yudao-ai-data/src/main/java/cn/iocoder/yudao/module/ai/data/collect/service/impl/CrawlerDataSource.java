package cn.iocoder.yudao.module.ai.data.collect.service.impl;

import cn.iocoder.yudao.module.ai.data.collect.dal.dataobject.DataSourceConfig;
import cn.iocoder.yudao.module.ai.data.collect.enums.DataSourceType;
import cn.iocoder.yudao.module.ai.data.collect.model.CrawlerSourceConfig;
import cn.iocoder.yudao.module.ai.data.collect.model.SourceFile;
import cn.iocoder.yudao.module.ai.data.collect.model.crawler.CrawlRequest;
import cn.iocoder.yudao.module.ai.data.collect.model.crawler.CrawlResult;
import cn.iocoder.yudao.module.ai.data.collect.service.DataSource;
import cn.iocoder.yudao.module.ai.data.collect.util.SourceHashUtil;
import cn.iocoder.yudao.module.ai.data.config.AiDataProperties;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 爬虫采集源。
 * <p>
 * 调用爬虫服务 {@code POST /crawl}，将返回的 Markdown 作为采集产物。
 * <p>
 * 响应体流式下载到临时文件，再通过 Jackson 流式解析逐条读取，避免将整个响应一次性缓冲进内存。
 */
@Component
public class CrawlerDataSource implements DataSource {

    private final WebClient.Builder webClientBuilder;
    private final AiDataProperties properties;
    private final ObjectMapper objectMapper;

    public CrawlerDataSource(WebClient.Builder webClientBuilder, AiDataProperties properties,
                             ObjectMapper objectMapper) {
        this.webClientBuilder = webClientBuilder;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public DataSourceType type() {
        return DataSourceType.CRAWLER;
    }

    @Override
    public List<SourceFile> fetch(DataSourceConfig config) throws Exception {
        CrawlerSourceConfig crawlerConfig = (CrawlerSourceConfig) config.getConfig();
        if (crawlerConfig == null || crawlerConfig.getUrls() == null || crawlerConfig.getUrls().isEmpty()) {
            throw new IllegalArgumentException("爬虫采集源缺少 URL 配置");
        }

        CrawlRequest request = CrawlRequest.builder()
                .urls(crawlerConfig.getUrls())
                .browserConfig(crawlerConfig.getBrowserParams())
                .crawlerConfig(crawlerConfig.getRunParams())
                .build();

        Path tempFile = null;
        try {
            tempFile = downloadToTempFile(request);
            return parseResults(tempFile);
        } finally {
            deleteQuietly(tempFile);
        }
    }

    /**
     * 流式下载爬虫响应到临时文件，避免 WebClient 默认 256KB 内存缓冲限制。
     */
    private Path downloadToTempFile(CrawlRequest request) throws IOException {
        Path tempFile = Files.createTempFile("ai-data-crawler-", ".json");
        // 使用同步 FileChannel（参考 HttpDataSource）：Windows + JDK 22+ 下
        // Netty DataBuffer 的 DirectByteBuffer 不支持 address()，不能用 AsynchronousFileChannel。
        try (FileChannel channel = FileChannel.open(tempFile,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            DataBufferUtils.write(
                            webClientBuilder.build()
                                    .post()
                                    .uri(properties.getCrawlerUrl() + "/crawl")
                                    .bodyValue(request)
                                    .retrieve()
                                    .bodyToFlux(DataBuffer.class),
                            channel)
                    .blockLast(Duration.ofSeconds(properties.getCrawlerTimeoutSeconds()));
        } catch (Exception e) {
            deleteQuietly(tempFile);
            throw new IllegalStateException("下载爬虫响应失败: " + properties.getCrawlerUrl(), e);
        }
        return tempFile;
    }

    /**
     * 流式解析响应中的 {@code results} 数组，逐条转换为 {@link SourceFile}。
     */
    private List<SourceFile> parseResults(Path file) {
        List<SourceFile> files = new ArrayList<>();
        // 逐个读取数组元素，避免 readValue 默认校验 trailing token 将 END_ARRAY 判为非法。
        ObjectReader reader = objectMapper.readerFor(CrawlResult.class)
                .without(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        try (JsonParser parser = objectMapper.createParser(file.toFile())) {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new IllegalStateException("爬虫响应格式错误，期望 JSON 对象");
            }
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.currentName();
                parser.nextToken();
                if ("results".equals(fieldName) && parser.currentToken() == JsonToken.START_ARRAY) {
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        CrawlResult result = reader.readValue(parser);
                        SourceFile sourceFile = toSourceFile(result);
                        if (sourceFile != null) {
                            files.add(sourceFile);
                        }
                    }
                } else {
                    parser.skipChildren();
                }
            }
        }
        return files;
    }

    private SourceFile toSourceFile(CrawlResult result) {
        String markdown = result.getMarkdown() != null ? result.getMarkdown().getRawMarkdown() : null;
        if (markdown == null || markdown.isBlank()) {
            return null;
        }
        String url = result.getUrl();
        String fileName = SourceHashUtil.fileName(url);
        if (fileName.isBlank()) {
            fileName = "crawler.md";
        }
        byte[] content = markdown.getBytes(StandardCharsets.UTF_8);
        return SourceFile.builder()
                .path(url)
                .fileName(fileName)
                .fileType(SourceHashUtil.extName(fileName))
                .fileSize(content.length)
                .fileHash(SourceHashUtil.sha256Hex(content))
                .markdown(markdown)
                .build();
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // 忽略删除失败，临时文件会在进程退出后由系统清理
        }
    }

}
