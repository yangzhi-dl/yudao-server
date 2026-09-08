package cn.iocoder.yudao.module.ai.knowledge.common.mapper;

import cn.iocoder.yudao.module.ai.knowledge.wiki.dal.dataobject.Wiki;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.entity.WikiCatalogIdSet;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.entity.WikiHot;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface KnowledgeMapper {

    List<Long> selectDocumentId();

    List<WikiHot> selectHotWiki(Integer limit);

    List<Wiki> selectWikiEmbedding();

    void deleteWikiCatalogsByDocumentIds(List<Long> documentIds);

    List<WikiCatalogIdSet> selectWikiCatalogIdSetByDocumentIds(List<Long> documentIds);

    WikiCatalogIdSet selectWikiCatalogIdSetByDocumentId(Long documentId);
}
