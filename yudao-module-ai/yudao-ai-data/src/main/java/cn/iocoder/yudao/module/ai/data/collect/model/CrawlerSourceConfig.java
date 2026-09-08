package cn.iocoder.yudao.module.ai.data.collect.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 爬虫采集源配置。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CrawlerSourceConfig extends SourceConfigParam {

    /**
     * 待抓取的 URL 列表。
     */
    private List<String> urls;

    /**
     * 浏览器配置参数（透传给爬虫服务）。
     */
    private Map<String, Object> browserParams;

    /**
     * 运行配置参数（透传给爬虫服务）。
     */
    private Map<String, Object> runParams;

}
