package cn.iocoder.yudao.module.ai.knowledge.common.event.subscriber;

import cn.iocoder.yudao.module.ai.common.utils.FileUtil;
import cn.iocoder.yudao.module.ai.core.rag.service.MilvusStoreService;
import cn.iocoder.yudao.module.ai.knowledge.common.event.WriteWikiDocEvent;
import cn.iocoder.yudao.module.ai.knowledge.document.dal.dataobject.DocumentContent;
import cn.iocoder.yudao.module.ai.knowledge.document.service.DocumentService;
import cn.iocoder.yudao.module.ai.knowledge.grap.service.WikiGraphService;
import cn.iocoder.yudao.module.ai.knowledge.wiki.dal.dataobject.WikiCatalog;
import cn.iocoder.yudao.module.ai.knowledge.wiki.service.WikiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class WriteWikiDocSubscriber implements ApplicationListener<WriteWikiDocEvent> {

    private final WikiService wikiService;
    private final MilvusStoreService milvusStoreService;
    private final WikiGraphService wikiGraphService;
    private final DocumentService documentService;

    public WriteWikiDocSubscriber(WikiService wikiService, MilvusStoreService milvusStoreService,
                                  WikiGraphService wikiGraphService, DocumentService documentService) {
        this.wikiService = wikiService;
        this.milvusStoreService = milvusStoreService;
        this.wikiGraphService = wikiGraphService;
        this.documentService = documentService;
    }

    @Async
    @Override
    public void onApplicationEvent(WriteWikiDocEvent event) {
        // 在这里处理收到的事件，可以是任何逻辑操作
        Long documentId = event.getDocumentId();
        WikiCatalog wikiCatalog = wikiService.findWikiCatalogByDocumentId(documentId);
        if (Objects.isNull(wikiCatalog)) {
            return;
        }

        // 获取当前线程名称
        String threadName = Thread.currentThread().getName();
        log.info("==> 知识库文档更新向量库: {}", threadName);
        Long wikiId = wikiCatalog.getWikiId();

        File tempDir = new File(System.getProperty("java.io.tmpdir"), "kg_write_temp_" + wikiId);
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            throw new RuntimeException("创建临时目录失败: " + tempDir.getAbsolutePath());
        }

        DocumentContent documentContent = documentService.selectByDocumentId(documentId);
        Boolean isUpload = wikiGraphService.uploadWikiDocument(wikiId, documentId, FileUtil.createTempFile(tempDir, documentId, documentContent.getContent()));
        if (isUpload) {
            log.info("==> 知识库文档更新图谱内容事件消费成功，wikiId: {}，documentId: {}", wikiId, documentId);
        }

        wikiService.disableEmbedding(List.of(wikiCatalog.getId()));
        milvusStoreService.addDocumentByWiki(wikiId);
        log.info("==> 知识库文档更新向量库内容事件消费成功，wikiId: {}，documentId: {}", wikiId, documentId);
    }

}
