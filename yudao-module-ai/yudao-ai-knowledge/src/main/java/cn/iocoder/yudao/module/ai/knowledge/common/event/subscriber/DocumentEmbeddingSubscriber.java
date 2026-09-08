package cn.iocoder.yudao.module.ai.knowledge.common.event.subscriber;

import cn.iocoder.yudao.module.ai.core.rag.service.MilvusStoreService;
import cn.iocoder.yudao.module.ai.knowledge.common.event.WikiEmbeddingEvent;
import cn.iocoder.yudao.module.ai.knowledge.wiki.dal.dataobject.Wiki;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
@Slf4j
public class DocumentEmbeddingSubscriber implements ApplicationListener<WikiEmbeddingEvent> {

    private final MilvusStoreService milvusStoreService;

    public DocumentEmbeddingSubscriber(MilvusStoreService milvusStoreService) {
        this.milvusStoreService = milvusStoreService;
    }

    @Async
    @Override
    public void onApplicationEvent(WikiEmbeddingEvent event) {
        // 在这里处理收到的事件，可以是任何逻辑操作
        Wiki wiki = event.getWiki();
        Long wikiId = wiki.getId();

        log.info("==> 知识库向量化事件: {}", Thread.currentThread().getName());
        // 文档向量化
        Boolean aBoolean = milvusStoreService.addDocumentByWiki(wikiId);
        log.info("==> 知识库向量化事件消费成功，wikiId: {}，知识库向量化结果: {}", wikiId, aBoolean);
    }
}