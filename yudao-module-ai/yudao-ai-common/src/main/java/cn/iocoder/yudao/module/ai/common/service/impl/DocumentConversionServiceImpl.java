package cn.iocoder.yudao.module.ai.common.service.impl;

import cn.iocoder.yudao.module.ai.common.enums.TaskHistoryStatus;
import cn.iocoder.yudao.module.ai.common.enums.TaskHistoryType;
import cn.iocoder.yudao.module.ai.common.enums.TaskScheduleType;
import cn.iocoder.yudao.module.ai.common.model.entity.*;
import cn.iocoder.yudao.module.ai.common.sequence.SequenceManager;
import cn.iocoder.yudao.module.ai.common.service.DocumentConversionService;
import cn.iocoder.yudao.module.ai.common.service.MarkdownConversionService;
import cn.iocoder.yudao.module.ai.common.service.PdfToImageService;
import cn.iocoder.yudao.module.ai.common.service.TaskHistoryService;
import cn.iocoder.yudao.module.ai.common.utils.ImageBase64Util;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import tools.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentConversionServiceImpl implements DocumentConversionService {

    @Autowired(required = false)
    private DocumentConverter documentConverter;
    private final PdfToImageService pdfToImageService;
    private final SequenceManager sequenceManager;
    private final BlockingQueue<ConversionResult> markdownTaskQueue;
    private final Semaphore documentConversionSemaphore;
    private final TaskHistoryService taskHistoryService;
    private final MarkdownConversionService markdownConversionService;

    /**
     * 处理Office文档转换（异步）
     */
    @Async("documentConversionExecutor")
    public void convertDocumentImageAsync(DocumentTask task) {
        long startTime = System.currentTimeMillis();
        boolean permitAcquired = false;
        try {
            // 获取信号量许可，控制并发
            documentConversionSemaphore.acquire();
            permitAcquired = true;
            Long taskId = taskHistoryService.initTask(InitTask.builder().taskName("文档转换任务")
                    .taskType(TaskHistoryType.DOCUMENT_FILE).objectId(task.getFileId())
                    .scheduleType(TaskScheduleType.IMMEDIATELY).build());
            task.setTaskId(taskId);
            log.info("开始处理文档任务: {}, 起始页码: {}, 优先级: {}",
                    task.getTaskId(), task.getPageNumber(), task.getPriority());
            // 转换文档为PDF（如果是Office格式）
            File pdfFile = convertToPdf(task.getInputStream(), task.getFileName());

            try {
                // PDF转图片
                List<BufferedImage> images = pdfToImageService.pdfToImages(pdfFile);

                List<Integer> integers = taskHistoryService.loadingUnprocessed(taskId, new TypeReference<>() {});
                boolean isReload = Objects.nonNull(integers) && !integers.isEmpty();
                int imageSize = isReload ? integers.size() : images.size();
                // 注册文档总页数
                task.extractFromEvent();
                Long documentId = task.getDocumentId();
                sequenceManager.registerDocument(documentId, imageSize);
                sequenceManager.setDocumentApplicationEvent(documentId, task.getEvent());
                // 逐页处理
                if (isReload) {
                    sequenceManager.setReloadDocumentPageNumber(documentId, integers);
                    sequenceManager.setReloadDocument(documentId, true);
                    for (Integer pageNumber : integers) {
                        processImagePage(task, images.get(pageNumber - 1), pageNumber);
                    }
                } else {
                    sequenceManager.setReloadDocument(documentId, false);
                    for (int i = 0; i < images.size(); i++) {
                        processImagePage(task, images.get(i), i + 1);
                    }
                }
                Boolean taskStatus = taskHistoryService.updateTaskStatus(UpdateTaskStatus.builder()
                        .id(taskId).status(TaskHistoryStatus.RUNNING).build());
                log.info("文档转图片处理完成: {}, 总页数: {}, 耗时: {}ms, 更新任务状态结果：{}",
                        documentId, imageSize,
                        System.currentTimeMillis() - startTime, taskStatus);

            } finally {
                // 删除临时PDF文件
                if (pdfFile != null && pdfFile.exists()) {
                    log.info("临时文档删除状态：{}", pdfFile.delete());
                }
            }

        } catch (Exception e) {
            log.error("文档处理失败: {}", task.getTaskId(), e);
            createErrorResult(task, e.getMessage());
        } finally {
            if (permitAcquired) {
                documentConversionSemaphore.release();
            }
            // 关闭输入流
            closeInputStream(task.getInputStream());
        }
    }

    /**
     * 处理文档直接转换为 Markdown（异步，任务处理形式）
     * <p>
     * 流程：Office 文档 → PDF → 将 PDF 整体入队 → 消费者线程统一调用 PDF 算法接口
     * <p>
     * 与 {@link #convertDocumentImageAsync} 模式一致：
     * 生产者（本方法）只负责文档转换和入队，实际 API 调用由 Markdown 消费者控制并发。
     */
    @Async("documentConversionExecutor")
    public void convertDocumentFileAsync(DocumentTask task) {
        long startTime = System.currentTimeMillis();
        boolean permitAcquired = false;
        try {
            // 获取信号量许可，控制并发
            documentConversionSemaphore.acquire();
            permitAcquired = true;
            Long taskId = taskHistoryService.initTask(InitTask.builder().taskName("文档直接转Markdown任务")
                    .taskType(TaskHistoryType.DOCUMENT_FILE).objectId(task.getFileId())
                    .scheduleType(TaskScheduleType.IMMEDIATELY).build());
            task.setTaskId(taskId);
            log.info("开始处理文档直接转Markdown任务: {}, 文件名: {}, 优先级: {}",
                    task.getTaskId(), task.getFileName(), task.getPriority());

            // 转换文档为PDF（如果是Office格式）
            File pdfFile = convertToPdf(task.getInputStream(), task.getFileName());

            // 注册文档元数据（重载信息、事件等由消费者处理）
            task.extractFromEvent();
            Long documentId = task.getDocumentId();
            List<Integer> integers = taskHistoryService.loadingUnprocessed(taskId, new TypeReference<>() {});
            boolean isReload = Objects.nonNull(integers) && !integers.isEmpty();
            if (isReload) {
                sequenceManager.setReloadDocumentPageNumber(documentId, integers);
                sequenceManager.setReloadDocument(documentId, true);
            } else {
                sequenceManager.setReloadDocument(documentId, false);
            }
            sequenceManager.setDocumentApplicationEvent(documentId, task.getEvent());

            // 将 PDF 文件打包成 ConversionResult 放入队列
            // 注意：pdfFile 的清理责任转移给消费者线程
            ConversionResult result = ConversionResult.builder()
                    .taskId(task.getTaskId())
                    .documentId(documentId)
                    .pdfFile(pdfFile)
                    .success(true)
                    .tenantId(TenantContextHolder.getTenantId())
                    .userId(SecurityFrameworkUtils.getLoginUserId())
                    .build();

            submitToMarkdownQueue(result);

            Boolean taskStatus = taskHistoryService.updateTaskStatus(UpdateTaskStatus.builder()
                    .id(taskId).status(TaskHistoryStatus.RUNNING).build());
            log.info("文档直接转Markdown任务已入队: {}, 耗时: {}ms, 更新任务状态结果：{}",
                    documentId, System.currentTimeMillis() - startTime, taskStatus);

        } catch (Exception e) {
            log.error("文档直接转Markdown处理失败: {}", task.getTaskId(), e);
            createErrorResult(task, e.getMessage());
            // 异常时清理可能在 convertToPdf 中创建的临时 PDF（如果还未传递给消费者）
            // 注意：正常流程中 pdfFile 生命周期已转移给消费者，此处仅处理异常路径
        } finally {
            if (permitAcquired) {
                documentConversionSemaphore.release();
            }
            // 关闭输入流
            closeInputStream(task.getInputStream());
        }
    }

    /**
     * 同步将文档转换为 Markdown 结果列表
     * @param inputStream 文档输入流
     * @param fileName 文件名（用于识别格式）
     * @return 按页码排序的 Markdown 转换结果列表
     * @throws Exception 转换失败时抛出
     */
    public List<MarkdownConversionResult> convertDocumentImageSync(InputStream inputStream,
                                                                   String fileName) throws Exception {
        try {
            File pdfFile = convertToPdf(inputStream, fileName);
            List<BufferedImage> images = pdfToImageService.pdfToImages(pdfFile);
            List<MarkdownConversionResult> results = new ArrayList<>();
            for (int i = 0; i < images.size(); i++) {
                String base64Image = ImageBase64Util.imageToBase64(images.get(i));
                DocumentResponse response = markdownConversionService.convertImageToMarkdownSync(base64Image);
                List<MarkdownConversionResult.ImageInfo> list = response.getFiles().stream()
                        .map(item -> MarkdownConversionResult.ImageInfo.builder().context(item.getContext()).build()).toList();
                MarkdownConversionResult result = MarkdownConversionResult.builder()
                        .pageNumber(i + 1)
                        .markdownContent(response.getMarkdown())
                        .imageInfos(list)
                        .build();
                results.add(result);
            }
            results.sort(Comparator.comparingInt(MarkdownConversionResult::getPageNumber));
            return results;
        } finally {
            closeInputStream(inputStream);
        }
    }

    /**
     * 同步将文档转换为 Markdown 结果列表（支持 PDF 直接上传）
     * @param inputStream 文档输入流
     * @param fileName 文件名（用于识别格式）
     * @param userId 用户ID（可选，用于上下文）
     * @return 按页码排序的 Markdown 转换结果列表（DocumentResponse 列表）
     * @throws Exception 转换失败时抛出
     */
    public List<MarkdownConversionResult> convertDocumentFileSync(InputStream inputStream,
                                                          String fileName,
                                                          Long userId) throws Exception {
        File pdfFile = null;
        try {
            pdfFile = convertToPdf(inputStream, fileName);
            List<MarkdownConversionResult> results = new ArrayList<>();
            List<DocumentResponse> documentResponses = markdownConversionService.convertPdfToMarkdownSync(pdfFile);
            for (int i = 0; i < documentResponses.size(); i++) {
                DocumentResponse documentResponse = documentResponses.get(i);
                List<MarkdownConversionResult.ImageInfo> list = documentResponse.getFiles().stream()
                        .map(item -> MarkdownConversionResult.ImageInfo.builder().context(item.getContext()).build()).toList();
                MarkdownConversionResult result = MarkdownConversionResult.builder()
                        .pageNumber(i + 1)
                        .markdownContent(documentResponse.getMarkdown())
                        .imageInfos(list)
                        .build();
                results.add(result);
            }
            results.sort(Comparator.comparingInt(MarkdownConversionResult::getPageNumber));
            return results;

        } finally {
            closeInputStream(inputStream);
            // 清理临时 PDF 文件
            if (pdfFile != null && pdfFile.exists()) {
                boolean deleted = pdfFile.delete();
                log.debug("临时PDF文件删除状态: {}", deleted);
            }
        }
    }


    /**
     * 处理单页图片
     */
    private void processImagePage(DocumentTask task, BufferedImage image, int pageNum)
            throws Exception {
        long pageStartTime = System.currentTimeMillis();

        // 转换为Base64
        String base64Image = ImageBase64Util.imageToBase64(image);

        // 创建转换结果
        ConversionResult result = ConversionResult.builder()
                .taskId(task.getTaskId())
                .documentId(task.getDocumentId())
                .pageNumber(pageNum)
                .base64Image(base64Image)
                .success(true).unprocessed(task.getUnprocessed())
                .tenantId(TenantContextHolder.getTenantId())
                .userId(SecurityFrameworkUtils.getLoginUserId())
                .completeTime(LocalDateTime.now())
                .processingTimeMs(System.currentTimeMillis() - pageStartTime)
                .build();

        // 放入Markdown转换队列
        submitToMarkdownQueue(result);

        log.debug("页面 {} 处理完成, Base64长度: {}", pageNum, base64Image.length());
    }

    /**
     * 提交到Markdown转换队列
     * <p>
     * 由队列自身容量控制背压，生产者不会无限制地将大量任务塞入队列撑爆内存。
     */
    private void submitToMarkdownQueue(ConversionResult result) {
        try {
            // 放入队列（会阻塞如果队列满）
            markdownTaskQueue.put(result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("提交到Markdown队列时中断: {}", result.getTaskId());
        }
    }

    /**
     * Office转PDF
     */
    private File convertToPdf(InputStream inputStream, String fileName) throws Exception {
        File tempPdf = File.createTempFile("doc_", ".pdf");
        // 注意：不在 tempPdf 上调用 deleteOnExit()，由调用方在 finally 块中主动删除

        String lowerFileName = fileName.toLowerCase();

        // PDF 文件直接复制返回
        if (lowerFileName.endsWith(".pdf")) {
            try (FileOutputStream fos = new FileOutputStream(tempPdf)) {
                inputStream.transferTo(fos);
            }
            return tempPdf;
        }

        // 将输入流写入临时文件（jodconverter 需要 File 作为输入）
        File tempInput = File.createTempFile("input_", "_" + fileName);

        try (FileOutputStream fos = new FileOutputStream(tempInput)) {
            inputStream.transferTo(fos);
        }

        try {
            if (documentConverter == null) {
                throw new UnsupportedOperationException("文档转换不可用：未安装 LibreOffice / JODConverter");
            }
            documentConverter
                    .convert(tempInput)
                    .as(getInputFormat(fileName))
                    .to(tempPdf)
                    .as(DefaultDocumentFormatRegistry.PDF)
                    .execute();

            log.info("文档转换成功: {} -> PDF", fileName);
            return tempPdf;

        } catch (Exception e) {
            log.error("文档转换失败: {}", fileName, e);
            // 转换失败时删除 tempPdf，避免临时文件泄漏
            if (tempPdf.exists()) {
                tempPdf.delete();
            }
            throw new RuntimeException("文档转换失败: " + e.getMessage(), e);
        } finally {
            // 3. 清理临时输入文件
            if (tempInput.exists() && !tempInput.delete()) {
                log.warn("临时输入文件删除失败: {}", tempInput.getAbsolutePath());
            }
        }
    }

    /**
     * 根据文件名获取 jodconverter 的输入格式
     */
    private DocumentFormat getInputFormat(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return switch (extension) {
            case "doc", "docx" -> DefaultDocumentFormatRegistry.DOCX;
            case "xls", "xlsx" -> DefaultDocumentFormatRegistry.XLSX;
            case "ppt", "pptx" -> DefaultDocumentFormatRegistry.PPTX;
            case "odt" -> DefaultDocumentFormatRegistry.ODT;
            case "ods" -> DefaultDocumentFormatRegistry.ODS;
            case "odp" -> DefaultDocumentFormatRegistry.ODP;
            case "txt" -> DefaultDocumentFormatRegistry.TXT;
            case "html", "htm" -> DefaultDocumentFormatRegistry.HTML;
            case "rtf" -> DefaultDocumentFormatRegistry.RTF;
            default -> throw new IllegalArgumentException("不支持的文件格式: ." + extension);
        };
    }

    /**
     * 提取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    private void createErrorResult(DocumentTask task, String errorMessage) {
        ConversionResult result = ConversionResult.builder()
                .taskId(task.getTaskId())
                .documentId(task.getDocumentId())
                .pageNumber(task.getPageNumber())
                .success(false).unprocessed(task.getUnprocessed())
                .tenantId(TenantContextHolder.getTenantId())
                .userId(SecurityFrameworkUtils.getLoginUserId())
                .errorMessage(errorMessage)
                .completeTime(LocalDateTime.now())
                .build();

        try {
            markdownTaskQueue.put(result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void closeInputStream(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.warn("关闭输入流失败", e);
            }
        }
    }
}