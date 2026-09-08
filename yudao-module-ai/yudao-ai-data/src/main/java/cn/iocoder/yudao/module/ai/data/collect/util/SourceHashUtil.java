package cn.iocoder.yudao.module.ai.data.collect.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 采集源文件工具。
 */
public final class SourceHashUtil {

    private SourceHashUtil() {
    }

    /**
     * 计算字节数组的 SHA-256 十六进制摘要。
     */
    public static String sha256Hex(byte[] data) {
        return hex(digest().digest(data));
    }

    /**
     * 流式计算文件内容的 SHA-256 十六进制摘要，避免将整个文件读入内存。
     */
    public static String sha256Hex(Path file) throws IOException {
        MessageDigest digest = digest();
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) != -1) {
                digest.update(buffer, 0, n);
            }
        }
        return hex(digest.digest());
    }

    private static MessageDigest digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 算法不可用", e);
        }
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    /**
     * 提取文件扩展名（小写，不含点号）；无扩展名返回空字符串。
     */
    public static String extName(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * 从路径 / URL 中提取文件名；失败时返回原串。
     */
    public static String fileName(String path) {
        if (path == null) {
            return "";
        }
        String clean = path;
        int query = clean.indexOf('?');
        if (query != -1) {
            clean = clean.substring(0, query);
        }
        int slash = Math.max(clean.lastIndexOf('/'), clean.lastIndexOf('\\'));
        return slash == -1 ? clean : clean.substring(slash + 1);
    }

}
