package cn.iocoder.yudao.module.ai.knowledge.wiki.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.ai.common.model.dto.BasePageListDTO;
import cn.iocoder.yudao.module.ai.knowledge.document.model.dto.FindDocumentDetailDTO;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.dto.*;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.entity.WikiCatalogueTransformer;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.entity.WikiDetailTransformer;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.vo.*;
import cn.iocoder.yudao.module.ai.knowledge.wiki.dal.dataobject.WikiCatalog;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.entity.WikiSettings;

import java.util.List;
import java.util.Map;
import java.util.Set;


public interface WikiService {
    /**
     * 新增知识库
     */
    void addWiki(AddWikiDTO addWikiDto);

    /**
     * 删除知识库
     */
    void deleteWiki(DeleteWikiDTO deleteWikiDto);

    PageResult<UsageRecordWikiVO> getUsageRecordWikiPage(BasePageListDTO dto);

    List<HotWikiVO> getHotWiki(Integer limit);

    /**
     * 知识库分页查询
     */
    PageResult<FindWikiPageListVO> findWikiPageList(FindWikiPageListDTO dto);

    /**
     * 更新知识库置顶状态
     */
    void updateWikiIsTop(UpdateWikiIsTopDTO updateWikiIsTopDto);

    /**
     * 更新知识库
     */
    void updateWiki(UpdateWikiDTO updateWikiDto);

    List<FindWikiCatalogListVO> findWikiCatalogList(FindWikiCatalogListDTO dto);

    List<FindWikiCatalogListVO> findWikiCatalogListByPermission(FindWikiCatalogListDTO dto);

    /**
     * 查询指定目录下的文档列表（分页）
     */
    PageResult<FindWikiCatalogListVO> findCatalogDocuments(Long wikiId, Long parentId, Integer page, Integer pageSize);

    /**
     * 获取单个知识库详情
     */
    FindWikiPageListVO getWikiDetail(Long wikiId);

    /**
     * 批量查询多个目录下的文档，返回 parentId -> 文档列表 的映射
     */
    Map<Long, List<FindBatchCatalogDocumentVO>> findBatchCatalogDocuments(Long wikiId, List<Long> parentIds);

    /**
     * 分页查询未归档的文档列表
     */
    PageResult<FindWikiCatalogListVO> findUnarchivedDocuments(Long wikiId, String title, Long categoryId, List<Long> tagIds, Integer page, Integer pageSize);

    /**
     * 归档文档到知识库目录
     */
    void archiveDocuments(ArchiveWikiDocumentDTO dto);

    /**
     * 从目录移出文档
     */
    void removeDocumentFromCatalog(Long wikiId, Long documentId);

    /**
     * 更新知识库目录
     */
    Boolean updateWikiCatalogs(UpdateWikiCatalogDTO dto);

    List<WikiCatalog> findWikiDocumentById(Long wikiId);

    List<WikiCatalogueTransformer> findWikiDetailByIds(List<Long> wikiIds);

    void setEmbeddingStatus(List<Long> ids, Boolean isEmbedding);

    default void enableEmbedding(List<Long> ids) {
        setEmbeddingStatus(ids, true);
    }

    default void disableEmbedding(List<Long> ids) {
        setEmbeddingStatus(ids, false);
    }

    WikiCatalog findWikiCatalogByDocumentId(Long documentId);

    FindWikiDocumentDetailVO findWikiDetail(FindDocumentDetailDTO articleDetailDto);

    void passageEmbedding(Long wikiId);

    PageResult<FindUserWikiPageListVO> findWikiPermissionPageList(FindAccessibleWikiPageListDTO dto);

    List<WikiDetailTransformer> findWikiDetailByPermission();

    Boolean generateWikiGraph(Long wikiId);

    String generateWikiGraphToken();

    WikiSettings getWikiSettings(Long wikiId);

    void updateWikiSettings(UpdateWikiSettingsDTO dto);

    /**
     * 检查当前用户是否有权访问指定知识库
     * <p>规则：若该知识库是本人 → 放行；若设置了 ACL → 校验当前用户是否拥有指定权限
     *
     * @param wikiId     知识库ID
     * @param permission 需要的权限
     * @return true=可访问，false=无权限
     */
    Boolean hasWikiAccess(Long wikiId, Permission permission);

    /**
     * 批量检查当前用户是否有权访问指定知识库列表
     * <p>返回有权限的知识库 ID 集合
     *
     * @param wikiIds    知识库ID列表
     * @param permission 需要的权限
     * @return 有权限的知识库 ID 集合
     */
    Set<Long> hasWikisAccess(List<Long> wikiIds, Permission permission);
}
