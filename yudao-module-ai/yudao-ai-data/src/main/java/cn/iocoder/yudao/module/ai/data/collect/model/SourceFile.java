package cn.iocoder.yudao.module.ai.data.collect.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

/**
 * 采集到的源文件。
 * <p>
 * 文件系统 / HTTP 源通过 {@link #content} 承载原始字节；
 * 爬虫源直接将 Markdown 文本放入 {@link #markdown}。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceFile {

    /**
     * 来源路径（文件路径或 URL）。
     */
    private String path;

    /**
     * 文件名（含扩展名）。
     */
    private String fileName;

    /**
     * 文件扩展名（小写，不含点号）。
     */
    private String fileType;

    /**
     * 文件大小（字节）。
     */
    private long fileSize;

    /**
     * 文件内容 SHA-256 摘要（用于去重）。
     */
    private String fileHash;

    /**
     * 原始文件内容（文件系统 / HTTP 源）。
     */
    private byte[] content;

    /**
     * 本地临时文件路径（HTTP 源流式下载的落盘位置，避免全量内存缓冲）。
     */
    private Path localFile;

    /**
     * 已转好的 Markdown（爬虫源）。
     */
    private String markdown;

}
