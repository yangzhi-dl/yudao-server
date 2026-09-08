package cn.iocoder.yudao.module.ai.knowledge.common.event;

import cn.iocoder.yudao.module.ai.knowledge.wiki.dal.dataobject.Wiki;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class WikiEmbeddingEvent extends ApplicationEvent {

    /**
     * 文档 ID
     */
    private final Wiki wiki;

    public WikiEmbeddingEvent(Object source, Wiki wiki) {
        super(source);
        this.wiki = wiki;
    }
}