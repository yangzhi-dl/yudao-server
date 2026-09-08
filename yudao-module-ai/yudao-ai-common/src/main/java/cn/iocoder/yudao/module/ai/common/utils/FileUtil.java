package cn.iocoder.yudao.module.ai.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

@Slf4j
public class FileUtil {

    /**
     * 创建临时文件
     *
     * @param tempDir 临时目录
     * @param documentId 文档ID
     * @param content 文件内容
     * @return 临时文件
     */
    public static File createTempFile(File tempDir, Long documentId, String content) {
        try {

            // 使用 documentId 作为文件名的一部分，避免重复
            String tempFileName = "doc_" + documentId + "_" + System.currentTimeMillis() + ".txt";
            File tempFile = new File(tempDir, tempFileName);

            // 写入内容
            try (FileWriter writer = new FileWriter(tempFile, StandardCharsets.UTF_8)) {
                writer.write(content);
            }

            log.debug("创建临时文件成功: {}", tempFile.getAbsolutePath());
            return tempFile;

        } catch (Exception e) {
            log.error("创建临时文件失败，documentId: {}", documentId, e);
            throw new RuntimeException("创建临时文件失败", e);
        }
    }

}
