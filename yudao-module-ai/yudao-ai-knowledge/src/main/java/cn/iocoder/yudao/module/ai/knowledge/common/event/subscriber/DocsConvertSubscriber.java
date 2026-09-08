package cn.iocoder.yudao.module.ai.knowledge.common.event.subscriber;

import cn.iocoder.yudao.module.ai.common.event.KBConversionEvent;
import cn.iocoder.yudao.module.ai.common.model.entity.DocumentTask;
import cn.iocoder.yudao.module.ai.common.service.DocumentConversionService;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.ai.knowledge.common.event.DocsConvertEvent;
import cn.iocoder.yudao.module.ai.knowledge.document.dal.dataobject.Document;
import cn.iocoder.yudao.module.ai.knowledge.document.dal.mysql.DocumentContentMapper;
import cn.iocoder.yudao.module.ai.knowledge.document.dal.mysql.DocumentMapper;
import cn.iocoder.yudao.module.ai.knowledge.document.dal.dataobject.DocumentContent;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 采集文档转换订阅者 —— 监听 DocsConvertEvent，创建知识库文档并触发 PDF→Markdown 转换
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocsConvertSubscriber implements ApplicationListener<DocsConvertEvent> {

    private final DocumentMapper documentMapper;
    private final DocumentContentMapper documentContentMapper;
    private final FileService fileService;
    private final DocumentConversionService documentConversionService;

    @Async
    @Override
    public void onApplicationEvent(@NonNull DocsConvertEvent event) {
        Long fileId = event.getFileId();
        FileDO file = fileService.getFile(fileId);
        String fileName = file.getName();
        try {
            log.info("开始处理采集文档转换: event={}, fileName={}", event, fileName);

            // 创建知识库文档
            Document document = Document.builder()
                    .title(fileName).build();
            documentMapper.insert(document);
            Long documentId = document.getId();

            // 创建空的文档内容
            DocumentContent documentContent = DocumentContent.builder()
                    .documentId(documentId)
                    .content("")
                    .build();
            documentContentMapper.insert(documentContent);
            byte[] fileContent = fileService.getFileContent(file.getConfigId(), file.getPath());
            // 构建转换任务
            KBConversionEvent kbEvent = new KBConversionEvent(this);
            kbEvent.setTenantId(TenantContextHolder.getTenantId());
            kbEvent.setUserId(SecurityFrameworkUtils.getLoginUserId());
            DocumentTask task = DocumentTask.builder()
                    .event(kbEvent)
                    .fileId(fileId)
                    .inputStream(new ByteArrayInputStream(fileContent))
                    .pageNumber(1)
                    .priority(1)
                    .documentId(documentId)
                    .fileName(fileName)
                    .retryCount(new AtomicInteger(0))
                    .createTime(LocalDateTime.now())
                    .build();

            // 异步执行文档转换（PDF/Office → Markdown）
            documentConversionService.convertDocumentFileAsync(task);

            log.info("采集文档转换任务已提交: fileId={}, documentId={}", fileId, documentId);

        } catch (Exception e) {
            log.error("采集文档转换失败: fileId={}, fileName={}", fileId, fileName, e);
        }
    }
}
