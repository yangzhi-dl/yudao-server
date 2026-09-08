package cn.iocoder.yudao.module.ai.core.rag.service;

import org.springframework.ai.document.Document;

import java.util.List;

public interface MilvusStoreService {

    List<Document> loadDocumentByWikiReader(List<Long> wikiIds, List<Long> restrictedDocumentIds, String question, Integer topK);

    List<Document> loadDocumentByDocumentReader(List<Long> documentIds, String question, Integer topK);

    Boolean addDocumentByWiki(Long wikiId);

    Boolean reloadDocumentByWiki(Long documentId);

    void delDocumentByWiki(Long wikiId);

    void delDocumentByDocIds(List<Long> documentIds);

}
