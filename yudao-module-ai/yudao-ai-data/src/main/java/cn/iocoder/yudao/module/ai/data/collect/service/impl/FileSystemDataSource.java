package cn.iocoder.yudao.module.ai.data.collect.service.impl;

import cn.iocoder.yudao.module.ai.data.collect.dal.dataobject.DataSourceConfig;
import cn.iocoder.yudao.module.ai.data.collect.enums.DataSourceType;
import cn.iocoder.yudao.module.ai.data.collect.model.FileSystemSourceConfig;
import cn.iocoder.yudao.module.ai.data.collect.model.SourceFile;
import cn.iocoder.yudao.module.ai.data.collect.service.DataSource;
import cn.iocoder.yudao.module.ai.data.collect.util.SourceHashUtil;
import cn.iocoder.yudao.module.ai.data.config.AiDataProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * 文件系统采集源。
 * <p>
 * 扫描指定目录（可选递归），拉取受支持扩展名的文件内容。
 */
@Component
public class FileSystemDataSource implements DataSource {

    private final AiDataProperties properties;

    public FileSystemDataSource(AiDataProperties properties) {
        this.properties = properties;
    }

    @Override
    public DataSourceType type() {
        return DataSourceType.FILESYSTEM;
    }

    @Override
    public List<SourceFile> fetch(DataSourceConfig config) throws Exception {
        FileSystemSourceConfig fsConfig = (FileSystemSourceConfig) config.getConfig();
        if (fsConfig == null || fsConfig.getDirectory() == null || fsConfig.getDirectory().isBlank()) {
            throw new IllegalArgumentException("文件系统采集源缺少目录配置");
        }

        Path root = Paths.get(fsConfig.getDirectory());
        if (!Files.isDirectory(root)) {
            throw new IllegalArgumentException("采集目录不存在或不是目录: " + fsConfig.getDirectory());
        }

        List<SourceFile> files = new ArrayList<>();
        try (Stream<Path> stream = Boolean.TRUE.equals(fsConfig.getRecursive())
                ? Files.walk(root)
                : Files.list(root)) {
            stream.filter(Files::isRegularFile)
                    .filter(this::isSupported)
                    .forEach(path -> {
                        try {
                            byte[] content = Files.readAllBytes(path);
                            String fileName = path.getFileName().toString();
                            files.add(SourceFile.builder()
                                    .path(path.toString())
                                    .fileName(fileName)
                                    .fileType(SourceHashUtil.extName(fileName))
                                    .fileSize(content.length)
                                    .fileHash(SourceHashUtil.sha256Hex(content))
                                    .content(content)
                                    .build());
                        } catch (IOException e) {
                            throw new RuntimeException("读取文件失败: " + path, e);
                        }
                    });
        }
        return files;
    }

    private boolean isSupported(Path path) {
        String ext = SourceHashUtil.extName(path.getFileName().toString()).toLowerCase(Locale.ROOT);
        return properties.getSupportedFileExtensions().stream()
                .map(String::toLowerCase)
                .anyMatch(ext::equals);
    }

}
