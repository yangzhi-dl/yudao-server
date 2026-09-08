package cn.iocoder.yudao.module.ai.data.collect.model.crawler;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 爬虫单条抓取结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlResult {

    @JsonProperty("url")
    private String url;

    @JsonProperty("title")
    private String title;

    @JsonProperty("markdown")
    private MarkdownContext markdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarkdownContext {

        @JsonProperty("raw_markdown")
        private String rawMarkdown;

    }

}
