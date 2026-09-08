package cn.iocoder.yudao.module.ai.data.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据采集与治理的配置项。
 * <p>
 * 前缀：{@code yudao.ai.data}
 */
@Data
@Component
@ConfigurationProperties(prefix = "yudao.ai.data")
public class AiDataProperties {

    /**
     * 资产自动质量评分门槛（0-100）。
     * <p>
     * 归档规则：资产状态为「已治理」或「质量评分 >= 门槛」满足其一即可归档；
     * 否则禁止归档，需要人工编辑或 AI 清洗。
     */
    private Integer qualityThreshold = 60;

    /**
     * AI 清洗默认提示词。
     */
    private String defaultCleanPrompt = "请对以下 Markdown 内容进行清洗与结构化整理："
            + "去除冗余空白与无关噪声，保留原始语义；"
            + "修正明显的排版与标点问题；"
            + "保持标题层级清晰，并保留代码块、表格与图片引用；"
            + "仅输出整理后的 Markdown 内容，不要附加解释。";

    /**
     * 爬虫服务地址。
     */
    private String crawlerUrl = "http://localhost:11235";

    /**
     * 爬虫服务超时时间（秒）。
     */
    private long crawlerTimeoutSeconds = 300;

    /**
     * 采集产物（Markdown）归档文件目录。
     */
    private String warehouseDirectory = "ai-data/warehouse";

    /**
     * 文件系统采集源支持的文件扩展名（不含点号）。
     */
    private List<String> supportedFileExtensions = List.of(
            "doc", "docx", "pdf", "xls", "xlsx", "ppt", "pptx",
            "txt", "md", "html", "htm", "rtf", "odt", "ods", "odp");

}
