package cn.iocoder.yudao.module.ai.knowledge.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class WriteWikiDocEvent extends ApplicationEvent {

    /**
     * 文档 ID
     */
    private final Long documentId;

    public WriteWikiDocEvent(Object source, Long documentId) {
        super(source);
        this.documentId = documentId;
    }

}
