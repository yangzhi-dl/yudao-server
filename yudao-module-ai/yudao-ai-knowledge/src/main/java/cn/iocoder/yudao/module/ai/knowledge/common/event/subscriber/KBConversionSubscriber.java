package cn.iocoder.yudao.module.ai.knowledge.common.event.subscriber;

import cn.iocoder.yudao.module.ai.common.event.KBConversionEvent;
import cn.iocoder.yudao.module.ai.common.model.entity.MarkdownConversionResult;
import cn.iocoder.yudao.module.ai.common.service.TaskHistoryService;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.module.ai.knowledge.common.utils.UmoDocConverter;
import cn.iocoder.yudao.module.ai.knowledge.document.model.entity.ConversionUpdateDocument;
import cn.iocoder.yudao.module.ai.knowledge.document.model.entity.DocumentTaxonomy;
import cn.iocoder.yudao.module.ai.knowledge.document.service.DocumentService;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class KBConversionSubscriber implements ApplicationListener<KBConversionEvent> {

    private final TaskHistoryService taskHistoryService;
    private final FileService fileService;
    private final DocumentService documentService;

    /** KB 转换消费者线程数，默认 2 */
    @Value("${iims.document.parser.kb-conversion-consumer.core-pool-size:2}")
    private int consumerThreads;

    /** 事件队列容量，默认 200 */
    @Value("${iims.document.parser.kb-conversion-consumer.queue-capacity:200}")
    private int queueCapacity;

    /**
     * 事件队列：缓存 KBConversionEvent，由线程池并发消费。
     */
    private LinkedBlockingQueue<KBConversionEvent> eventQueue;

    private volatile boolean running = true;
    private ExecutorService consumerExecutor;

    public KBConversionSubscriber(TaskHistoryService taskHistoryService, FileService fileService, DocumentService documentService) {
        this.taskHistoryService = taskHistoryService;
        this.fileService = fileService;
        this.documentService = documentService;
    }

    @PostConstruct
    public void init() {
        // 初始化事件队列
        this.eventQueue = new LinkedBlockingQueue<>(queueCapacity);
        // 启动线程池消费者，并发处理 KBConversionEvent
        consumerExecutor = new ThreadPoolExecutor(
                consumerThreads, consumerThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(200),
                r -> {
                    Thread t = new Thread(r, "kb-conversion-consumer");
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.AbortPolicy());
        for (int i = 0; i < consumerThreads; i++) {
            consumerExecutor.submit(() -> {
                log.info("KBConversionEvent 消费者线程已启动");
                while (running) {
                    try {
                        KBConversionEvent event = eventQueue.poll(1, TimeUnit.SECONDS);
                        if (event != null) {
                            // 恢复租户和用户上下文（消费者线程中无上下文，需按事件逐条恢复）
                            Long oldTenantId = TenantContextHolder.getTenantId();
                            Boolean oldIgnore = TenantContextHolder.isIgnore();
                            try {
                                TenantContextHolder.setTenantId(event.getTenantId());
                                TenantContextHolder.setIgnore(false);
                                if (event.getUserId() != null) {
                                    LoginUser loginUser = new LoginUser();
                                    loginUser.setId(event.getUserId());
                                    SecurityContextHolder.getContext().setAuthentication(
                                            new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
                                }
                                processEvent(event);
                            } finally {
                                TenantContextHolder.setTenantId(oldTenantId);
                                TenantContextHolder.setIgnore(oldIgnore);
                                SecurityContextHolder.clearContext();
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        log.error("处理 KBConversionEvent 异常", e);
                    }
                }
                log.info("KBConversionEvent 消费者线程已停止");
            });
        }
    }

    @PreDestroy
    public void destroy() {
        running = false;
        if (consumerExecutor != null) {
            consumerExecutor.shutdownNow();
        }
    }

    @Override
    public void onApplicationEvent(@NonNull KBConversionEvent event) {
        // 入队由线程池并发消费
        if (!eventQueue.offer(event)) {
            log.warn("KBConversionEvent 队列已满 ({})，丢弃事件: documentId={}", queueCapacity, event.getDocumentId());
        }
    }

    /**
     * 单线程串行处理单个转换完成事件
     */
    private void processEvent(KBConversionEvent event) {
        Long taskId = event.getTaskId();
        Long documentId = event.getDocumentId();

        try {

            // 加载转换结果并更新文档内容
            List<MarkdownConversionResult> results = taskHistoryService.loadingResult(taskId, new TypeReference<>() {});
            String markdownContent = this.aggregateMarkdownByPage(results);

            // 将 Markdown 转换为 UMO Doc 格式 HTML，存入文档内容供前端 UMO 编辑器/查看器渲染
            String umoHtmlContent = UmoDocConverter.convert(markdownContent);
            documentService.updateDocumentToConversion(ConversionUpdateDocument.builder()
                    .id(documentId).fileId(event.getFileId())
                    .content(umoHtmlContent).build(), false);
            log.info("文档 {} 已完成转换任务", documentId);

            if (StringUtils.isBlank(markdownContent)) {
                return;
            }

            String summary = documentService.generateSummaryByContent(markdownContent);
            String title = documentService.generateTitleBySummary(summary);
            DocumentTaxonomy documentTaxonomy = documentService.generateTaxonomy(summary);

            documentService.updateDocumentToConversion(ConversionUpdateDocument.builder()
                    .id(documentId).title(title).categoryId(documentTaxonomy.getCategoryId())
                    .tagIds(documentTaxonomy.getTagIds()).summary(summary).build(), true);
            log.info("文档 {} 已完成摘要生成、标题生成、分类任务", documentId);

        } catch (Exception e) {
            log.error("处理文档转换完成事件失败: documentId={}", documentId, e);
        }
    }

    /**
     * 按页码顺序汇总Markdown内容
     *
     * @param results 文档转化结果集
     * @return 按页码顺序拼接的完整Markdown字符串
     */
    private String aggregateMarkdownByPage(List<MarkdownConversionResult> results) {

        StringBuilder aggregatedContent = new StringBuilder();

        for (MarkdownConversionResult result : results) {
            List<MarkdownConversionResult.ImageInfo> imageInfos = result.getImageInfos();

            String content = result.getMarkdownContent();

            // 替换所有图片占位符
            for (MarkdownConversionResult.ImageInfo imageInfo : imageInfos) {
                String placeholder = String.format("![](%s)", imageInfo.getIndex());
                FileDO file = fileService.getFile(imageInfo.getImageId());
                String replacement = String.format("![%s.png](%s \"%s\")",
                        imageInfo.getImageId(), file.getUrl(), formatImageTitle(imageInfo.getContext()));
                content = content.replace(placeholder, replacement);
            }

            aggregatedContent.append(content);
            aggregatedContent.append("\n\n");
        }
        return aggregatedContent.toString();
    }
    /**
     * 格式化 Markdown 图片标题 - HTML 实体编码版
     */
    public static String formatImageTitle(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "图片";
        }
        String encoded = StringEscapeUtils.escapeHtml4(text);
        return encoded
                .replace("\\", "\\\\")      // 反斜杠
                .replace("\"", "\\\"")      // 双引号
                .replaceAll("[\\r\\n]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

}