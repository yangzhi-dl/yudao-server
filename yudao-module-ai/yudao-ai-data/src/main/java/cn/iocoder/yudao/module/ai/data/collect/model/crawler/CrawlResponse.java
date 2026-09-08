package cn.iocoder.yudao.module.ai.data.collect.model.crawler;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 爬虫服务响应体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlResponse {

    @JsonProperty("results")
    private List<CrawlResult> results;

}
