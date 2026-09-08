package cn.iocoder.yudao.module.ai.data.governance.event;

import org.springframework.context.ApplicationEvent;

/**
 * 资产转换完成事件。
 * <p>
 * 在采集产物转换为 Markdown 并生成数据资产后发布，可用于后续异步扩展
 * （例如质量评分、通知等）。
 */
public class AssetConvertedEvent extends ApplicationEvent {

    private final Long assetId;

    public AssetConvertedEvent(Object source, Long assetId) {
        super(source);
        this.assetId = assetId;
    }

    public Long getAssetId() {
        return assetId;
    }

}
