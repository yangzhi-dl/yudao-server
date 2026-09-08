package cn.iocoder.yudao.module.ai.data.collect.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * HTTP 下载采集源配置。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HttpSourceConfig extends SourceConfigParam {

    /**
     * 待下载的文件 URL 列表。
     */
    private List<String> urls;

}
