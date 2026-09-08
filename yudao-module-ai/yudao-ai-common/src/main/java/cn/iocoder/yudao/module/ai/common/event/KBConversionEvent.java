package cn.iocoder.yudao.module.ai.common.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class KBConversionEvent extends ApplicationEvent {

    private Long taskId;

    private Long fileId;

    private Long documentId;

    private String filename;

    /** 租户 ID，用于异步线程中恢复租户上下文 */
    private Long tenantId;

    /** 用户 ID，用于异步线程中恢复用户上下文 */
    private Long userId;

    public KBConversionEvent(Object source) {
        super(source);
    }
}
