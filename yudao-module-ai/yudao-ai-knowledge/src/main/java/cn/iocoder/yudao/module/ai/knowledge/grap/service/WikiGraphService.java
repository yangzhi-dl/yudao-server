package cn.iocoder.yudao.module.ai.knowledge.grap.service;

import java.io.File;

public interface WikiGraphService {

    Boolean uploadWikiDocument(Long wikiId, Long documentId, File file);

    String getPermissionsToken();

    Boolean deleteWikiDocument(Long wikiId, Long documentId);

    Boolean deleteWiki(Long wikiId);

}
