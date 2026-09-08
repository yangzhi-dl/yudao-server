package cn.iocoder.yudao.module.ai.data.collect.service.impl;

import cn.iocoder.yudao.module.ai.data.collect.dal.dataobject.DataSourceConfig;
import cn.iocoder.yudao.module.ai.data.collect.enums.DataSourceType;
import cn.iocoder.yudao.module.ai.data.collect.model.HttpSourceConfig;
import cn.iocoder.yudao.module.ai.data.collect.model.SourceFile;
import cn.iocoder.yudao.module.ai.data.collect.service.DataSource;
import cn.iocoder.yudao.module.ai.data.collect.util.SourceHashUtil;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * HTTP 下载采集源。
 * <p>
 * 逐个下载配置的 URL 内容，作为采集产物。
 */
@Component
public class HttpDataSource implements DataSource {

    private final WebClient.Builder webClientBuilder;

    public HttpDataSource(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public DataSourceType type() {
        return DataSourceType.HTTP;
    }

    @Override
    public List<SourceFile> fetch(DataSourceConfig config) throws Exception {
        HttpSourceConfig httpConfig = (HttpSourceConfig) config.getConfig();
        if (httpConfig == null || httpConfig.getUrls() == null || httpConfig.getUrls().isEmpty()) {
            throw new IllegalArgumentException("HTTP 采集源缺少 URL 配置");
        }

        List<SourceFile> files = new ArrayList<>();
        try {
            for (String url : httpConfig.getUrls()) {
                String fileName = SourceHashUtil.fileName(url);
                Path tempFile = Files.createTempFile("ai-data-http-", ".tmp");
                try {
                    long size = downloadToFile(url, tempFile);
                    if (size <= 0) {
                        throw new IllegalStateException("下载失败，响应为空: " + url);
                    }
                    files.add(SourceFile.builder()
                            .path(url)
                            .fileName(fileName)
                            .fileType(SourceHashUtil.extName(fileName))
                            .fileSize(size)
                            .fileHash(SourceHashUtil.sha256Hex(tempFile))
                            .localFile(tempFile)
                            .build());
                } catch (Exception e) {
                    deleteQuietly(tempFile);
                    throw e;
                }
            }
            return files;
        } catch (Exception e) {
            for (SourceFile file : files) {
                deleteQuietly(file.getLocalFile());
            }
            throw e;
        }
    }

    /**
     * 流式下载到临时文件，避免将整个响应体缓冲进内存。
     *
     * @return 已下载字节数
     */
    private long downloadToFile(String url, Path target) {
        // 使用同步 FileChannel 而非 AsynchronousFileChannel（Path 重载）。
        // Windows + JDK 22+ 下，Netty 的 DataBuffer 走 closeable shared session，
        // AsynchronousFileChannel 需要 DirectByteBuffer.address() 会抛 UnsupportedOperationException。
        try (FileChannel channel = FileChannel.open(target,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            DataBufferUtils.write(
                            webClientBuilder.build()
                                    .get()
                                    .uri(url)
                                    .retrieve()
                                    .bodyToFlux(DataBuffer.class),
                            channel).blockLast();
        } catch (IOException e) {
            throw new IllegalStateException("下载文件失败: " + url, e);
        }
        try {
            return Files.size(target);
        } catch (IOException e) {
            throw new IllegalStateException("读取下载文件大小失败: " + url, e);
        }
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
