package cn.iocoder.yudao.module.ai.core.chat.service;

import org.springframework.ai.document.Document;

import java.util.List;

public interface AiModelToolService {

    String imageOcrByModel(List<Long> fileIds);

    String generateSummaryByModel(List<Document> documents);

    String generateTitleByModel(String summary);

    <C, T> T generateTaxonomy(C citation, Class<T> typeRef, String summary);

    <C, T> T generateNewJSON(C citation, Class<T> typeRef, String context);

    String optimizeMarkdownContent(String markdown, String prompt);

    String formattingDocument(String content);
}
