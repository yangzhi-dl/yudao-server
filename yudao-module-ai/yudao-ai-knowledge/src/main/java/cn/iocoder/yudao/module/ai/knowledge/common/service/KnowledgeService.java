package cn.iocoder.yudao.module.ai.knowledge.common.service;

import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.ai.knowledge.wiki.dal.dataobject.Wiki;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.entity.WikiCatalogIdSet;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.entity.WikiHot;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface KnowledgeService {

    List<Long> selectWikiDocumentIdByPermission(Permission permission);

    Boolean isPermissionByWikiDocumentId(Long id, Permission permission, boolean isCreator);

    List<Long> filterWikiDocumentIdsByPermission(List<Long> id, Permission permission);

    List<WikiHot> selectHotWikiByPermission(Integer limit);

    List<Wiki> getAccessibleEmbeddedWikis(Permission permission);

    void deleteWikiCatalogsByDocumentIds(List<Long> documentIds);

    List<WikiCatalogIdSet> selectWikiCatalogIdSetByDocumentIds(List<Long> documentIds);

    WikiCatalogIdSet selectWikiCatalogIdSetByDocumentId(Long documentId);

    /**
     * 查询当前用户无权访问的文档 ID 列表（含知识库继承回退）
     *
     * <p>文档级 ACL 优先：有 ACL 记录的文档按文档级判定；
     * 无 ACL 记录的文档回退到所属知识库授权（仅 READ / EXECUTE 支持回退）。
     * <p>供 {@code Document} 表 ACL 数据权限规则使用，通过 NOT IN 排除用户无权查看的文档。
     *
     * @param permission 用户执行操作所需的最小权限
     * @return 无权访问的文档 ID 集合
     */
    Set<Long> getRestrictedDocumentIdsWithWikiFallback(Permission permission);

    /**
     * 获取当前用户有权访问的文档 ID 集合
     *
     * @param permission 需要的权限（如 Permission.VIEW）
     * @return 有权访问的文档 ID 集合
     */
    Set<Long> getAccessibleDocumentIds(Permission permission);

    /**
     * 知识库内文档访问判定：文档存在 ACL 记录时按文档级 ACL 判定，
     * 无 ACL 记录时回退到所属知识库授权（仅 READ / EXECUTE 支持回退）。
     * 文档创建者始终豁免。
     *
     * @param wikiId     所属知识库 ID
     * @param documentId 文档 ID
     * @param permission 所需权限
     * @return true=可访问
     */
    boolean hasWikiDocumentAccess(Long wikiId, Long documentId, Permission permission);

    /**
     * 知识库内候选文档过滤：返回当前用户可访问的文档 ID 集合（含文档级 ACL 与知识库回退）。
     *
     * @param wikiId      所属知识库 ID
     * @param documentIds 候选文档 ID 集合
     * @param permission  所需权限（仅 READ / EXECUTE 支持回退）
     * @return 可访问的文档 ID 集合
     */
    Set<Long> accessibleWikiDocumentIds(Long wikiId, Collection<Long> documentIds, Permission permission);

    /**
     * 知识库内候选文档过滤：返回当前用户不可访问的文档 ID 集合（含文档级 ACL 与知识库回退）。
     *
     * @param wikiId      所属知识库 ID
     * @param documentIds 候选文档 ID 集合
     * @param permission  所需权限（仅 READ / EXECUTE 支持回退）
     * @return 不可访问的文档 ID 集合
     */
    Set<Long> restrictedWikiDocumentIds(Long wikiId, Collection<Long> documentIds, Permission permission);

    /**
     * 查询当前用户无权访问的知识库 ID 列表（谨慎使用）
     *
     * <p><b>注意：</b>未设置 ACL 的知识库不会出现在返回结果中，等同于不受限访问。
     * <p>本方法供数据访问层过滤使用，通过 NOT IN 排除用户无权查看的条目，
     * 确保分页总数与列表数据保持一致。
     *
     * @param permission 用户执行操作所需的最小权限
     * @return 无权访问的知识库 ID 集合（不包含 null），若全有权访问则返回空集合
     */
    Set<Long> getRestrictedWikiIds(Permission permission);

    /**
     * 获取当前用户有权访问的知识库 ID 集合
     *
     * @param permission 需要的权限（如 Permission.VIEW）
     * @return 有权访问的知识库 ID 集合
     */
    Set<Long> getAccessibleWikiIds(Permission permission);
}
