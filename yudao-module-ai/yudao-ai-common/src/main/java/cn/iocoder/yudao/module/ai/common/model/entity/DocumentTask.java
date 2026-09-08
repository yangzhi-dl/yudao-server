package cn.iocoder.yudao.module.ai.common.model.entity;

import cn.iocoder.yudao.module.ai.common.event.KBConversionEvent;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@Builder
@Slf4j
public class DocumentTask {
    private Long taskId;
    private @NonNull Long fileId;
    private Long documentId;      // 同一文档的标识
    private int pageNumber;         // 页码
    private int priority;           // 优先级 (1-10, 1最高)
    private List<Integer> unprocessed;
    private @NonNull ApplicationEvent event;
    private InputStream inputStream;
    private String fileName;
    private LocalDateTime createTime;
    private AtomicInteger retryCount;

    public void extractFromEvent() {
        if (event instanceof KBConversionEvent kbEvent) {
            kbEvent.setTaskId(this.taskId);
            kbEvent.setFileId(this.fileId);
            kbEvent.setDocumentId(this.documentId);
            kbEvent.setFilename(this.fileName);
        }
    }

}