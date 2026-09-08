package cn.iocoder.yudao.module.ai.knowledge.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 采集文件转换事件 —— 采集文件上 后发布，监听并创建知识库文档、触发 PDF→Markdown 转换
 */
@Getter
public class DocsConvertEvent extends ApplicationEvent {

    private final Long fileId;

    public DocsConvertEvent(Object source, Long fileId) {
        super(source);
        this.fileId = fileId;
    }
}
