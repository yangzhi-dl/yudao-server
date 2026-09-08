package cn.iocoder.yudao.module.ai.data.collect.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 采集源配置参数的抽象基类。
 * <p>
 * 使用 Jackson 多态，{@code type} 字段与 {@code DataSourceType} 枚举名一致，
 * 存储于 {@code ai_data_source_config.config} 的 JSON 列中。
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FileSystemSourceConfig.class, name = "FILESYSTEM"),
        @JsonSubTypes.Type(value = HttpSourceConfig.class, name = "HTTP"),
        @JsonSubTypes.Type(value = CrawlerSourceConfig.class, name = "CRAWLER"),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class SourceConfigParam {

}
