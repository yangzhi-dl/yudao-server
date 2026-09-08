package cn.iocoder.yudao.module.ai.knowledge.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @Author: Aitenry
 * @Date: 2023/01/22 00:00
 * @Version: v1.0.0
 * @Description: TODO
 **/
@Getter
public class ReadKnowledgeEvent extends ApplicationEvent {

    /** 知识库 ID **/
    private final Long wikiId;

    /** 文档 ID **/
    private final Long documentId;

    public ReadKnowledgeEvent(Object source, Long wikiId, Long documentId) {
        super(source);
        this.wikiId = wikiId;
        this.documentId = documentId;
    }
}