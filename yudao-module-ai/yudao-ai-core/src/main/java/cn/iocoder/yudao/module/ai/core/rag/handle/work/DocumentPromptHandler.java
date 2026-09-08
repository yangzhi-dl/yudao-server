package cn.iocoder.yudao.module.ai.core.rag.handle.work;

import cn.iocoder.yudao.module.ai.common.model.entity.MarkdownConversionResult;
import cn.iocoder.yudao.module.ai.common.service.DocumentConversionService;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ModelUseInfo;
import cn.iocoder.yudao.module.ai.core.rag.aspect.FileTypeHandler;
import cn.iocoder.yudao.module.ai.core.rag.enums.FileModelTypeEnum;
import cn.iocoder.yudao.module.ai.core.rag.handle.PromptHandler;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@FileTypeHandler({"ppt", "pptx", "ofd", "pdf", "md", "docx", "doc", "csv", "xls", "xlsx"})
public class DocumentPromptHandler implements PromptHandler {

    private final FileService fileService;

    private final DocumentConversionService documentConversionService;

    private static final JTokkitTokenCountEstimator estimator = new JTokkitTokenCountEstimator();

    public DocumentPromptHandler(FileService fileService, DocumentConversionService documentConversionService) {
        this.fileService = fileService;
        this.documentConversionService = documentConversionService;
    }

    @Override
    public ModelUseInfo handle(FileDO file) {
        Long id = file.getId();
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(file.getUrl());
        List<Document> documents = tikaDocumentReader.get();
        List<String> originalContents = documents.stream()
                .map(Document::getText).filter(Objects::nonNull)
                .filter(StringUtils::isNoneBlank)
                .toList();

        if (originalContents.isEmpty() && file.getSize() > 0) {
            try {
                byte[] fileContent = fileService.getFileContent(file.getConfigId(), file.getPath());
                List<MarkdownConversionResult> results = documentConversionService.convertDocumentImageSync(
                        new ByteArrayInputStream(fileContent), file.getName());
                originalContents = results.stream().map(MarkdownConversionResult::getMarkdownContent).toList();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        // 按段落累加，控制总 token 数不超过 30000
        List<String> truncatedContents = new ArrayList<>();
        int accumulatedEstimate = 0;
        int maxTokens = 30000;

        for (String content : originalContents) {
            int segmentEstimate = estimator.estimate(content);
            if (accumulatedEstimate + segmentEstimate > maxTokens) {
                // 当前段落加入后会超限，尝试截断该段落
                String truncatedSegment = truncateToTokenLimit(content, maxTokens - accumulatedEstimate);
                if (!truncatedSegment.trim().isEmpty()) {
                    truncatedContents.add(truncatedSegment);
                }
                break; // 达到上限，停止处理后续内容
            }
            truncatedContents.add(content);
            accumulatedEstimate += segmentEstimate;
        }

        // 如果内容为空（极端情况），提供兜底内容
        if (truncatedContents.isEmpty() && !originalContents.isEmpty()) {
            truncatedContents.add(truncateToTokenLimit(originalContents.getFirst(), maxTokens));
        }

        return ModelUseInfo.builder()
                .context(truncatedContents)
                .type(FileModelTypeEnum.TEXT)
                .build();
    }

    /**
     * 将文本截断至指定 token 限制（采用二分查找提高效率）
     */
    private String truncateToTokenLimit(String text, int maxTokens) {
        if (text == null || text.isEmpty() || maxTokens <= 0) {
            return "";
        }

        int estimate = estimator.estimate(text);
        if (estimate <= maxTokens) {
            return text;
        }

        // 二分查找最佳截断位置
        int left = 0, right = text.length();
        String bestResult = "";

        while (left <= right) {
            int mid = (left + right) / 2;
            String candidate = text.substring(0, mid);
            int tokenCount = estimator.estimate(candidate);

            if (tokenCount <= maxTokens) {
                bestResult = candidate;
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        // 尽量在句子边界截断（避免截断在单词中间）
        int lastSentenceEnd = Math.max(
                bestResult.lastIndexOf('.'),
                Math.max(bestResult.lastIndexOf('!'), bestResult.lastIndexOf('?'))
        );
        if (lastSentenceEnd > bestResult.length() - 50 && lastSentenceEnd > 0) {
            return bestResult.substring(0, lastSentenceEnd + 1).trim();
        }

        return bestResult.trim();
    }
}