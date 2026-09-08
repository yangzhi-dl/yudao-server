package cn.iocoder.yudao.module.ai.common.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.ai.common.enums.TaskHistoryStatus;
import cn.iocoder.yudao.module.ai.common.model.entity.*;
import cn.iocoder.yudao.module.ai.common.sequence.SequenceManager;
import cn.iocoder.yudao.module.ai.common.service.MarkdownConversionService;
import cn.iocoder.yudao.module.ai.common.service.TaskHistoryService;
import cn.iocoder.yudao.module.infra.framework.file.core.client.FileClient;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FileCreateReqVO;
import cn.iocoder.yudao.module.infra.service.file.FileConfigService;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import com.alibaba.fastjson.JSONArray;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static cn.hutool.core.date.DatePattern.PURE_DATE_PATTERN;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarkdownConversionServiceImpl implements MarkdownConversionService {

    private final BlockingQueue<ConversionResult> markdownTaskQueue;
    private final SequenceManager sequenceManager;
    private final Semaphore markdownConversionSemaphore;
    private final RestTemplate restTemplate;
    private final TaskHistoryService taskHistoryService;
    private final ApplicationEventPublisher eventPublisher;
    private final FileService fileService;
    private final FileConfigService fileConfigService;

    /**
     * 存储每个文档的转换结果（按页码排序）
     * Key: 文档ID, Value: 按页码排序的结果Map
     */
    private final ConcurrentHashMap<Long, ConcurrentSkipListMap<Integer, DocumentResponse>> documentResults = new ConcurrentHashMap<>();

    @Value("${iims.document.parser.url:http://localhost:8070}")
    private String markdownApiUrl;

    @Value("${iims.document.parser.pdf-path:/ocr/pdf}")
    private String markdownApiFilePath;

    @Value("${iims.document.parser.image-path:/ocr}")
    private String markdownApiImagePath;

    @Value("${iims.document.parser.timeout:30}")
    private Integer apiTimeout; // 秒

    /**
     * 启动消费者线程，持续从队列获取任务
     */
    @Override
    @Async("markdownConversionExecutor")
    public void startConsumer() {
        log.debug("验证 ImageToMarkdown 转换消费者是否已启动");

        while (true) {
            try {
                // 从队列获取任务（阻塞等待）
                ConversionResult result = markdownTaskQueue.poll(1, TimeUnit.SECONDS);

                if (result != null) {
                    Long documentId = result.getDocumentId();
                    if (!documentResults.containsKey(documentId)) {
                        documentResults.put(documentId, new ConcurrentSkipListMap<>());
                    }
                    // 恢复租户上下文（消费者线程中无上下文，需按任务逐条恢复）
                    // 同时恢复用户上下文，否则 TaskHistoryService 中 getLoginUserId() 返回 null
                    Long oldTenantId = TenantContextHolder.getTenantId();
                    Boolean oldIgnore = TenantContextHolder.isIgnore();
                    try {
                        TenantContextHolder.setTenantId(result.getTenantId());
                        TenantContextHolder.setIgnore(false);
                        if (result.getUserId() != null) {
                            LoginUser loginUser = new LoginUser();
                            loginUser.setId(result.getUserId());
                            SecurityContextHolder.getContext().setAuthentication(
                                    new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
                        }
                        processMarkdownConversion(result);
                    } finally {
                        TenantContextHolder.setTenantId(oldTenantId);
                        TenantContextHolder.setIgnore(oldIgnore);
                        SecurityContextHolder.clearContext();
                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Markdown消费者被中断");
                break;
            } catch (Exception e) {
                log.error("Markdown转换处理异常", e);
            }
        }
    }

    /**
     * 处理单个转换任务
     * <p>
     * 支持三种模式：
     * <ul>
     *   <li>PDF 文件模式：调用 PDF 算法接口获取全部页结果，批量处理</li>
     *   <li>图片 Base64 模式：调用图片算法接口逐页转换</li>
     *   <li>已携带结果模式：跳过 API 调用，直接走完成流程</li>
     * </ul>
     */
    private void processMarkdownConversion(ConversionResult result) {

        Long documentId = result.getDocumentId();

        if (!result.isSuccess()) {
            log.warn("跳过失败的任务: {}", result.getTaskId());
            sequenceManager.markPageCompleted(documentId);
            // 检查是否所有页都已完成（包括失败），清理资源
            if (sequenceManager.tryMarkDocumentCompleted(documentId)) {
                sequenceManager.cleanup(documentId);
                documentResults.remove(documentId);
            }
            return;
        }

        // === PDF 文件模式：整体 PDF 传给消费者，由消费者统一调用 PDF 算法接口 ===
        if (result.getPdfFile() != null) {
            processPdfFileTask(result);
            return;
        }

        // === 图片 Base64 模式：逐页调用图片算法接口 ===
        boolean permitAcquired = false;
        try {
            // 检查是否已经有 documentResponse（兼容旧逻辑）
            if (result.getDocumentResponse() == null) {
                // 获取信号量许可，控制 Markdown API 并发调用数
                markdownConversionSemaphore.acquire();
                permitAcquired = true;
                log.debug("开始Markdown转换: 文档={}, 页码={}",
                        documentId, result.getPageNumber());

                // 调用图片算法 API
                DocumentResponse documentResponse = callMarkdownApi(result.getBase64Image());

                // 更新结果
                result.setDocumentResponse(documentResponse);

                log.debug("Markdown转换完成: 文档={}, 页码={}, 内容长度={}",
                        documentId, result.getPageNumber(),
                        documentResponse.getMarkdown().length());
            } else {
                log.debug("使用已有的Markdown转换结果: 文档={}, 页码={}, 内容长度={}",
                        documentId, result.getPageNumber(),
                        result.getDocumentResponse().getMarkdown().length());
            }

            handleConversionComplete(result);

        } catch (Exception e) {
            log.error("Markdown转换失败: {}", result.getTaskId(), e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            handleConversionComplete(result);
        } finally {
            if (permitAcquired) {
                markdownConversionSemaphore.release();
            }
        }
    }

    /**
     * 处理 PDF 大任务：调用 PDF 算法接口获取全部页结果，逐页完成处理
     */
    private void processPdfFileTask(ConversionResult result) {
        Long documentId = result.getDocumentId();
        Long taskId = result.getTaskId();
        File pdfFile = result.getPdfFile();
        boolean permitAcquired = false;

        try {
            // 获取信号量许可，控制 PDF API 并发调用
            markdownConversionSemaphore.acquire();
            permitAcquired = true;

            log.info("开始PDF转Markdown: 文档={}, taskId={}", documentId, taskId);

            // 调用 PDF 算法接口，获取全部页结果
            List<DocumentResponse> documentResponses = callMarkdownPdfApi(pdfFile);

            // 注册文档总页数
            int totalPages = documentResponses.size();
            sequenceManager.registerDocument(documentId, totalPages);

            // 确保 documentResults 中该文档的容器存在
            if (!documentResults.containsKey(documentId)) {
                documentResults.put(documentId, new ConcurrentSkipListMap<>());
            }
            ConcurrentSkipListMap<Integer, DocumentResponse> pageResults = documentResults.get(documentId);

            // 解析重载信息，确定需要处理的页码
            List<Integer> unprocessed = taskHistoryService.loadingUnprocessed(taskId, new TypeReference<>() {});
            boolean isReload = Objects.nonNull(unprocessed) && !unprocessed.isEmpty();

            if (isReload) {
                sequenceManager.setReloadDocumentPageNumber(documentId, new ArrayList<>(unprocessed));
                sequenceManager.setReloadDocument(documentId, true);
                for (Integer pageNumber : unprocessed) {
                    DocumentResponse docResp = documentResponses.get(pageNumber - 1);
                    pageResults.put(pageNumber, docResp);
                    sequenceManager.markPageCompleted(documentId);
                }
            } else {
                sequenceManager.setReloadDocument(documentId, false);
                for (int i = 0; i < totalPages; i++) {
                    int pageNumber = i + 1;
                    pageResults.put(pageNumber, documentResponses.get(i));
                    sequenceManager.markPageCompleted(documentId);
                }
            }

            // 记录未处理页面（全部完成时应为空列表）
            List<Integer> remainingUnprocessed = new ArrayList<>();
            taskHistoryService.recordTaskUnprocessed(RecordTaskUnprocessed.builder()
                    .id(taskId).unprocessed(JSONArray.toJSONString(remainingUnprocessed)).build());

            // 尝试标记文档完成（所有页已处理）
            if (sequenceManager.tryMarkDocumentCompleted(documentId)) {
                try {
                    onDocumentComplete(result, sequenceManager.isReloadDocument(documentId), pageResults);
                } finally {
                    sequenceManager.cleanup(documentId);
                    documentResults.remove(documentId);
                }
            }

            log.info("PDF转Markdown完成: 文档={}, 总页数={}", documentId, totalPages);

        } catch (Exception e) {
            log.error("PDF转Markdown失败: documentId={}", documentId, e);
            // 失败时记录错误并尝试标记文档失败
            try {
                sequenceManager.registerDocument(documentId, 0);
                if (sequenceManager.tryMarkDocumentCompleted(documentId)) {
                    onDocumentComplete(result, sequenceManager.isReloadDocument(documentId),
                            documentResults.getOrDefault(documentId, new ConcurrentSkipListMap<>()));
                }
            } catch (Exception ex) {
                log.error("处理PDF失败后的清理异常: documentId={}", documentId, ex);
            } finally {
                // 确保异常路径也清理 documentResults 和 sequenceManager
                sequenceManager.cleanup(documentId);
                documentResults.remove(documentId);
            }
        } finally {
            if (permitAcquired) {
                markdownConversionSemaphore.release();
            }
            // 清理临时 PDF 文件
            if (pdfFile != null && pdfFile.exists()) {
                boolean deleted = pdfFile.delete();
                log.info("临时PDF文件删除状态: {}, documentId={}", deleted, documentId);
            }
        }
    }

    /**
     * 调用Markdown转换API
     * @param base64Image Base64编码的图片内容
     * @return 转换后的Markdown字符串
     * @throws IOException 调用失败时抛出
     */
    private DocumentResponse callMarkdownApi(String base64Image) throws IOException {
        try {
            // 构建表单参数
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("image", base64Image);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> requestEntity =
                    new HttpEntity<>(formData, headers);

            // 发起 POST 请求
            ResponseEntity<DocumentResponse> response = restTemplate.postForEntity(
                    markdownApiUrl + markdownApiImagePath,
                    requestEntity,
                    DocumentResponse.class
            );

            DocumentResponse body = response.getBody();
            if (body == null) {
                throw new IOException("API返回空响应");
            }
            if (!body.getSuccess()) {
                throw new IOException("API处理失败: " + body.getMessage());
            }

            return body;

        } catch (RuntimeException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                throw new IOException("API调用超时: " + apiTimeout + "秒", e);
            }
            if (e instanceof WebClientResponseException ex) {
                throw new IOException("API调用失败 [" + ex.getStatusCode() + "]: " + ex.getResponseBodyAsString(), e);
            }
            throw new IOException("调用Markdown API异常", e);
        }
    }

    /**
     * 同步将图片 Base64 转换为 Markdown
     * @param base64Image 图片的 Base64 字符串
     * @return 转换结果（包含 Markdown 内容及提取的图片信息）
     * @throws IOException API 调用失败时抛出
     */
    public DocumentResponse convertImageToMarkdownSync(String base64Image) throws IOException {
        // 直接调用 API，不经过队列和信号量
        return callMarkdownApi(base64Image);
    }

    /**
     * 处理转换完成（保存结果、检查文档完整性等）
     */
    private void handleConversionComplete(ConversionResult result) {
        Long documentId = result.getDocumentId();
        sequenceManager.markPageCompleted(documentId);
        ConcurrentSkipListMap<Integer, DocumentResponse> pageResults = documentResults.get(documentId);
        DocumentResponse documentResponse = result.getDocumentResponse();
        if (Objects.nonNull(documentResponse)) {
            pageResults.put(result.getPageNumber(), documentResponse);
        }
        List<Integer> unprocessed;
        Boolean reloadDocument = sequenceManager.isReloadDocument(documentId);
        if (reloadDocument) {
            List<Integer> reloadDocumentPageNumber = sequenceManager.getReloadDocumentPageNumber(documentId);
            reloadDocumentPageNumber.remove((Integer) result.getPageNumber());
            unprocessed = reloadDocumentPageNumber;
        } else {
            unprocessed = this.findPendingPages(pageResults, sequenceManager.getTotalPages(documentId));
        }
        Boolean recordTaskUnprocessed = taskHistoryService.recordTaskUnprocessed(RecordTaskUnprocessed.builder().id(result.getTaskId())
                .unprocessed(JSONArray.toJSONString(unprocessed)).build());
        log.debug("文档 {} 未处理页码结果保存状态为：{}", documentId, recordTaskUnprocessed);
        // 检查文档是否全部完成
        if (sequenceManager.tryMarkDocumentCompleted(documentId)) {
            try {
                // 执行文档完成回调
                onDocumentComplete(result, sequenceManager.isReloadDocument(documentId),
                        documentResults.get(documentId));
            } finally {
                sequenceManager.cleanup(documentId);
                documentResults.remove(documentId);
            }
        }
    }

    private List<Integer> findPendingPages(ConcurrentSkipListMap<Integer, DocumentResponse> pageResults, int totalPages) {
        Set<Integer> allPages = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .collect(Collectors.toSet());
        allPages.removeAll(pageResults.keySet()); // 差集
        return new ArrayList<>(allPages);
    }

    /**
     * 文档全部完成回调
     */
    private void onDocumentComplete(ConversionResult result, Boolean reloadDocument,
                                    ConcurrentSkipListMap<Integer, DocumentResponse> pageResults) {
        Long documentId = result.getDocumentId();
        Long taskId = result.getTaskId();
        int size = pageResults.size();
        int totalPages = sequenceManager.getTotalPages(documentId);

        TaskHistoryStatus taskHistoryStatus;
        if (totalPages != 0 && size == 0) {
            taskHistoryStatus = TaskHistoryStatus.FAILURE;
        } else if (Objects.equals(size, totalPages)) {
            taskHistoryStatus = TaskHistoryStatus.SUCCESS;
        } else {
            taskHistoryStatus = TaskHistoryStatus.PARTIAL_FAILURE;
        }
        Boolean taskResult = taskHistoryService.initTaskResult(LoadingTaskResult.builder().id(taskId).status(taskHistoryStatus)
                .result(loadingMarkdownConversionResult(pageResults, taskId, reloadDocument)).build());
        ApplicationEvent documentApplicationEvent = sequenceManager.getDocumentApplicationEvent(documentId);
        if (documentApplicationEvent != null) {
            if (!taskResult) {
                log.warn("保存结果失败，已取消 {} 事件发布！", documentApplicationEvent.getClass().getSimpleName());
                return;
            }
            try {
                eventPublisher.publishEvent(documentApplicationEvent);
                log.info("已发布 {} 事件", documentApplicationEvent.getClass().getSimpleName());
            } catch (Exception e) {
                log.error("事件发布失败: {}", e.getMessage());
            }
        }
        // 实现文档完成后的逻辑
        log.info("文档 {} 所有页面处理完成, 处理结果为：{}, 保存结果为：{}", documentId, taskHistoryStatus, taskResult);
    }

    private String loadingMarkdownConversionResult(ConcurrentSkipListMap<Integer, DocumentResponse> pageResults,
                                                   Long taskId, Boolean reloadDocument) {
        // 将当前页结果转换为对象列表
        List<MarkdownConversionResult> currentResults = pageResults.entrySet().stream()
                .map(entry -> {
                    List<DocumentResponse.ImageInfo> files = entry.getValue().getFiles();
                    List<MarkdownConversionResult.ImageInfo> imageInfos = new ArrayList<>();
                    for (DocumentResponse.ImageInfo file : files) {
                        String base64 = file.getBase64();
                        MarkdownConversionResult.ImageInfo build = MarkdownConversionResult.ImageInfo.builder()
                                .index(file.getIndex()).context(file.getContext()).build();
                        try {
                            byte[] imageBytes = Base64.getDecoder().decode(base64);
                            String fileName = "image_" + entry.getKey() + file.getIndex() + ".png";
                            String directory = "ai-conversion";
                            String datePrefix = LocalDateTimeUtil.format(LocalDateTimeUtil.now(), PURE_DATE_PATTERN);
                            String path = directory + "/" + taskId + "/" + datePrefix + "/" + fileName;
                            FileClient client = fileConfigService.getMasterFileClient();
                            String url = client.upload(imageBytes, path, "image/png");
                            Long fileId = fileService.createFile(
                                    new FileCreateReqVO().setConfigId(client.getId())
                                            .setPath(path).setName(fileName).setUrl(url)
                                            .setType("image/png").setSize((long) imageBytes.length));
                            build.setImageId(fileId);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        imageInfos.add(build);
                    }
                    return MarkdownConversionResult.builder()
                            .pageNumber(entry.getKey())
                            .markdownContent(entry.getValue().getMarkdown())
                            .imageInfos(imageInfos)
                            .build();
                })
                .toList();

        // 如果不需要重载，直接返回当前结果
        if (!Boolean.TRUE.equals(reloadDocument)) {
            return JSONArray.toJSONString(currentResults);
        }

        // 获取当前已处理的页码集合
        Set<Integer> currentPageNumbers = pageResults.keySet();

        // 加载历史结果并合并
        List<MarkdownConversionResult> historyResults = taskHistoryService.loadingResult(taskId, new TypeReference<>() {});

        if (CollectionUtils.isEmpty(historyResults)) {
            return JSONArray.toJSONString(currentResults);
        }

        List<MarkdownConversionResult> finalResults = historyResults.stream()
                .filter(result -> !currentPageNumbers.contains(result.getPageNumber()))
                .collect(Collectors.toList());
        finalResults.addAll(currentResults);
        finalResults.sort(Comparator.comparingInt(MarkdownConversionResult::getPageNumber));
        return JSONArray.toJSONString(finalResults);
    }

    /**
     * 同步将 PDF 文件转换为 Markdown 结果列表
     * @param pdfFile PDF 文件
     * @return 按页码排序的 Markdown 转换结果列表
     * @throws IOException API 调用失败时抛出
     */
    public List<DocumentResponse> convertPdfToMarkdownSync(File pdfFile) throws IOException {
        return callMarkdownPdfApi(pdfFile);
    }

    /**
     * 调用 PDF 转 Markdown API
     * @param pdfFile PDF 文件
     * @return 每页 OCR 结果的列表
     * @throws IOException 调用失败时抛出
     */
    private List<DocumentResponse> callMarkdownPdfApi(File pdfFile) throws IOException {
        try {
            // 构建 multipart/form-data 请求
            MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();

            // 创建文件资源
            FileSystemResource fileResource = new FileSystemResource(pdfFile);
            formData.add("file", fileResource);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(formData, headers);

            // 发起 POST 请求，期望返回列表
            ResponseEntity<DocumentResponse[]> response = restTemplate.postForEntity(
                    markdownApiUrl + markdownApiFilePath,
                    requestEntity,
                    DocumentResponse[].class
            );

            DocumentResponse[] body = response.getBody();
            if (body == null || body.length == 0) {
                throw new IOException("API返回空响应");
            }

            // 检查每个响应是否成功
            for (DocumentResponse docResponse : body) {
                if (!docResponse.getSuccess()) {
                    throw new IOException("API处理失败: " + docResponse.getMessage());
                }
            }

            return Arrays.asList(body);

        } catch (RuntimeException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                throw new IOException("API调用超时: " + apiTimeout + "秒", e);
            }
            if (e instanceof WebClientResponseException ex) {
                throw new IOException("API调用失败 [" + ex.getStatusCode() + "]: " + ex.getResponseBodyAsString(), e);
            }
            throw new IOException("调用PDF Markdown API异常", e);
        }
    }

}
