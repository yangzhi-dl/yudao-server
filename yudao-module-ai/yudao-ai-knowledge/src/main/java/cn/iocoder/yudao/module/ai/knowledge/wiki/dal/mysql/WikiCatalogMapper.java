package cn.iocoder.yudao.module.ai.knowledge.wiki.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.ai.common.model.entity.EmbeddingCount;
import cn.iocoder.yudao.module.ai.knowledge.wiki.dal.dataobject.WikiCatalog;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.entity.WikiDeletedStatus;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.vo.WikiCatalogVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WikiCatalogMapper extends BaseMapperX<WikiCatalog> {

    List<WikiCatalog> selectByWikiId(Long wikiId);

    List<WikiCatalog> selectByWikiDocumentId(Long wikiId);

    EmbeddingCount countEmbedding(Long wikiId);

    void deleteByWikiId(WikiDeletedStatus wikiDeletedStatus);

    WikiCatalog selectFirstDocumentId(Long wikiId);

    void updateWikiCatalog(WikiCatalog wikiCatalog);

    void deleteWikiCatalogByIds(WikiDeletedStatus wikiDeletedStatus);

    void updateIsEmbedding(List<Long> ids, Boolean isEmbedding);

    WikiCatalog selectWikiCatalogByDocumentId(Long documentId);

    WikiCatalog selectFirstFilterDocumentId(Long wikiId, List<Long> restrictedDocumentIds);

    List<WikiCatalog> selectDirectoriesByWikiId(Long wikiId);

    List<WikiCatalog> selectDocumentsByParentId(Long wikiId, Long parentId, List<Long> restrictedDocumentIds);

    long countDocumentsByParentId(Long wikiId, Long parentId, List<Long> restrictedDocumentIds);

    List<WikiCatalog> selectDocumentsByParentIdPage(Long wikiId, Long parentId, List<Long> restrictedDocumentIds, int offset, int limit);

    List<WikiCatalog> selectDocumentsByParentIds(Long wikiId, List<Long> parentIds, List<Long> restrictedDocumentIds);

    void removeDocumentFromCatalog(Long wikiId, Long documentId);
}
