package cn.iocoder.yudao.module.ai.common.model.entity;

import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ConversionResult {
    private Long taskId;
    private Long documentId;
    private int pageNumber;
    private List<Integer> unprocessed;
    /** 租户 ID，用于异步线程中恢复租户上下文 */
    private Long tenantId;
    /** 用户 ID，用于异步线程中恢复用户上下文 */
    private Long userId;
    /** 图片 base64 编码（图片算法接口模式使用） */
    private String base64Image;
    /** PDF 文件引用（PDF 算法接口模式使用，整体传给消费者线程处理） */
    private File pdfFile;
    private DocumentResponse documentResponse;
    private boolean success;
    private String errorMessage;
    private LocalDateTime completeTime;
    private long processingTimeMs;
    /**
     * 已废弃：原用于标记生产者是否持有 markdownConversionSemaphore 许可。
     * 当前背压由队列容量控制，此字段保留仅为避免旧数据反序列化异常。
     */
    @Builder.Default
    private boolean ownsSemaphore = false;
}