package cn.iocoder.yudao.module.ai.knowledge.common.service.impl;

import cn.iocoder.yudao.framework.datapermission.core.util.DataPermissionUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.system.dal.dataobject.acl.AclPermissionDO;
import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import cn.iocoder.yudao.module.system.service.acl.AclService;
import cn.iocoder.yudao.module.system.service.acl.engine.AclDecisionEngine;
import cn.iocoder.yudao.module.ai.knowledge.common.mapper.KnowledgeMapper;
import cn.iocoder.yudao.module.ai.knowledge.common.service.KnowledgeService;
import cn.iocoder.yudao.module.ai.knowledge.document.dal.mysql.DocumentMapper;
import cn.iocoder.yudao.module.ai.knowledge.wiki.dal.mysql.WikiMapper;
import cn.iocoder.yudao.module.ai.knowledge.wiki.dal.dataobject.Wiki;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.entity.WikiCatalogIdSet;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.entity.WikiHot;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Service
public class KnowledgeServiceImpl implements KnowledgeService {

    private final KnowledgeMapper knowledgeMapper;
    private final WikiMapper wikiMapper;
    private final DocumentMapper documentMapper;
    private final AclDecisionEngine aclDecisionEngine;
    private final AclService aclService;

    public KnowledgeServiceImpl(KnowledgeMapper knowledgeMapper, WikiMapper wikiMapper, DocumentMapper documentMapper,
                                AclDecisionEngine aclDecisionEngine, AclService aclService) {
        this.knowledgeMapper = knowledgeMapper;
        this.wikiMapper = wikiMapper;
        this.documentMapper = documentMapper;
        this.aclDecisionEngine = aclDecisionEngine;
        this.aclService = aclService;
    }

    @Override
    public List<Long> selectWikiDocumentIdByPermission(Permission permission) {
        List<Long> ids = documentMapper.selectIdsByCreator(String.valueOf(getLoginUserId()));
        Set<Long> accessibleWikiIds = getAccessibleWikiIds(permission);
        if (accessibleWikiIds.isEmpty()) {
            return ids;
        }
        List<Long> docIds = TenantUtils.executeIgnore(knowledgeMapper::selectDocumentId);
        if (docIds == null || docIds.isEmpty()) {
            return ids;
        }
        // 获取文档所属的知识库ID
        List<WikiCatalogIdSet> catalogIdSets = TenantUtils.executeIgnore(
                () -> knowledgeMapper.selectWikiCatalogIdSetByDocumentIds(docIds));
        Set<Long> accessibleWikiDocumentIds = catalogIdSets.stream()
                .filter(c -> accessibleWikiIds.contains(c.getWikiId()))
                .map(WikiCatalogIdSet::getDocumentId)
                .collect(Collectors.toSet());

        List<Long> list = docIds.stream()
                .filter(accessibleWikiDocumentIds::contains)
                .toList();

        Set<Long> restrictedDocumentIds = getRestrictedDocumentIdsWithWikiFallback(Permission.READ);

        // 过滤掉受限文件
        List<Long> filteredList;
        if (restrictedDocumentIds.isEmpty()) {
            filteredList = list;
        } else {
            filteredList = list.stream()
                    .filter(id -> !restrictedDocumentIds.contains(id))
                    .toList();
        }

        // 合并 ids 和 filteredList，并去重
        Set<Long> resultSet = new HashSet<>(ids);
        resultSet.addAll(filteredList);
        return new ArrayList<>(resultSet);
    }

    @Override
    public Boolean isPermissionByWikiDocumentId(Long id, Permission permission, boolean isCreator) {

        if (isCreator) {
            return true;
        }

        // 反查文档所属知识库
        WikiCatalogIdSet catalogIdSet = TenantUtils.executeIgnore(
                () -> knowledgeMapper.selectWikiCatalogIdSetByDocumentId(id));
        if (catalogIdSet == null || catalogIdSet.getWikiId() == null) {
            // 文档不属于任何知识库，按纯文档级 ACL 判定
            return aclDecisionEngine.canAccess(ResourceType.DOCUMENT, id, permission);
        }

        // 文档级 ACL 优先，无文档 ACL 时回退知识库授权（仅 READ / EXECUTE）
        return hasWikiDocumentAccess(catalogIdSet.getWikiId(), id, permission);
    }

    @Override
    public List<Long> filterWikiDocumentIdsByPermission(List<Long> documentIds, Permission permission) {
        List<Long> ids = documentMapper.selectIdsByCreator(String.valueOf(getLoginUserId()));
        // 参数校验
        if (CollectionUtils.isEmpty(documentIds)) {
            return ids;
        }

        // 批量查询所有文档的知识库信息
        List<WikiCatalogIdSet> catalogIdSets = TenantUtils.executeIgnore(
                () -> knowledgeMapper.selectWikiCatalogIdSetByDocumentIds(documentIds));
        if (CollectionUtils.isEmpty(catalogIdSets)) {
            return ids;
        }

        // 获取用户有权限的知识库ID集合
        Set<Long> accessibleWikiIds = getAccessibleWikiIds(permission);
        if (CollectionUtils.isEmpty(accessibleWikiIds)) {
            return ids;
        }

        // 过滤出有权限的文档ID
        Map<Long, Long> documentWikiMap = catalogIdSets.stream()
                .filter(set -> set != null && set.getWikiId() != null)
                .collect(Collectors.toMap(
                        WikiCatalogIdSet::getDocumentId,
                        WikiCatalogIdSet::getWikiId,
                        (existing, replacement) -> existing
                ));

        // 过滤文档ID：保留有知识库映射且在可访问集合中的文档
        List<Long> collect = documentIds.stream()
                .filter(docId -> {
                    Long wikiId = documentWikiMap.get(docId);
                    return wikiId != null && accessibleWikiIds.contains(wikiId);
                })
                .collect(Collectors.toList());

        Set<Long> restrictedDocumentIds = getRestrictedDocumentIdsWithWikiFallback(Permission.READ);

        // 过滤掉受限文件
        List<Long> filteredList;
        if (restrictedDocumentIds.isEmpty()) {
            filteredList = collect;
        } else {
            filteredList = collect.stream()
                    .filter(id -> !restrictedDocumentIds.contains(id))
                    .toList();
        }

        // 合并 ids 和 filteredList，并去重
        Set<Long> resultSet = new LinkedHashSet<>(ids);
        resultSet.addAll(filteredList);
        return new ArrayList<>(resultSet);
    }

    @Override
    public List<WikiHot> selectHotWikiByPermission(Integer limit) {
        List<WikiHot> hotWikis = TenantUtils.executeIgnore(() -> knowledgeMapper.selectHotWiki(limit));
        if (hotWikis == null || hotWikis.isEmpty()) {
            return hotWikis;
        }
        Set<Long> accessibleWikiIds = getAccessibleWikiIds(Permission.READ);
        return hotWikis.stream()
                .filter(wiki -> accessibleWikiIds.contains(wiki.getId()))
                .toList();
    }

    @Override
    public List<Wiki> getAccessibleEmbeddedWikis(Permission permission) {
        List<Wiki> wikis = TenantUtils.executeIgnore(knowledgeMapper::selectWikiEmbedding);
        if (wikis == null || wikis.isEmpty()) {
            return List.of();
        }
        Set<Long> accessibleWikiIds = getAccessibleWikiIds(Permission.READ);
        if (accessibleWikiIds.isEmpty()) {
            return List.of();
        }
        return wikis.stream()
                .filter(wiki -> accessibleWikiIds.contains(wiki.getId()))
                .toList();
    }

    @Override
    public void deleteWikiCatalogsByDocumentIds(List<Long> documentIds) {
        if (documentIds.isEmpty()) return;
        knowledgeMapper.deleteWikiCatalogsByDocumentIds(documentIds);
    }

    @Override
    public List<WikiCatalogIdSet> selectWikiCatalogIdSetByDocumentIds(List<Long> documentIds) {
        if (documentIds.isEmpty()) return List.of();
        return knowledgeMapper.selectWikiCatalogIdSetByDocumentIds(documentIds);
    }

    @Override
    public WikiCatalogIdSet selectWikiCatalogIdSetByDocumentId(Long documentId) {
        return knowledgeMapper.selectWikiCatalogIdSetByDocumentId(documentId);
    }

    @Override
    public Set<Long> getRestrictedDocumentIdsWithWikiFallback(Permission permission) {
        List<Long> allIds = DataPermissionUtils.executeIgnore(
                () -> TenantUtils.executeIgnore(documentMapper::selectAllIds));
        if (allIds.isEmpty()) {
            return Set.of();
        }

        // 文档级受限（含创建者豁免、超管、租管、文档 ACL 授权）
        Set<Long> restricted = new HashSet<>(
                aclDecisionEngine.restrictedIds(ResourceType.DOCUMENT, permission, allIds));
        if (restricted.isEmpty()) {
            return Set.of();
        }

        // 仅 READ / EXECUTE 支持回退到知识库授权
        if (permission != Permission.READ && permission != Permission.EXECUTE) {
            return restricted;
        }

        // 文档 → 知识库映射（跨租户）
        List<WikiCatalogIdSet> catalogIdSets = TenantUtils.executeIgnore(
                () -> knowledgeMapper.selectWikiCatalogIdSetByDocumentIds(allIds));
        if (CollectionUtils.isEmpty(catalogIdSets)) {
            return restricted;
        }

        // 有文档级 ACL 记录的文档不参与回退；当前用户可访问的知识库
        Set<Long> docsWithAcl = selectDocumentIdsWithAcl(allIds);
        Set<Long> accessibleWikiIds = getAccessibleWikiIds(permission);
        if (accessibleWikiIds.isEmpty()) {
            return restricted;
        }

        // 无文档 ACL 且所属知识库可访问的文档，从受限集合中移除（继承知识库授权）
        Set<Long> result = new HashSet<>(restricted);
        for (WikiCatalogIdSet catalogIdSet : catalogIdSets) {
            Long documentId = catalogIdSet.getDocumentId();
            Long wikiId = catalogIdSet.getWikiId();
            if (documentId == null || wikiId == null) {
                continue;
            }
            if (!docsWithAcl.contains(documentId) && accessibleWikiIds.contains(wikiId)) {
                result.remove(documentId);
            }
        }
        return result;
    }

    @Override
    public Set<Long> getAccessibleDocumentIds(Permission permission) {
        List<Long> allIds = DataPermissionUtils.executeIgnore(
                () -> TenantUtils.executeIgnore(documentMapper::selectAllIds));
        if (allIds.isEmpty()) {
            return Set.of();
        }
        return aclDecisionEngine.accessibleIds(ResourceType.DOCUMENT, permission, allIds);
    }

    @Override
    public boolean hasWikiDocumentAccess(Long wikiId, Long documentId, Permission permission) {
        return accessibleWikiDocumentIds(wikiId, List.of(documentId), permission).contains(documentId);
    }

    @Override
    public Set<Long> accessibleWikiDocumentIds(Long wikiId, Collection<Long> documentIds, Permission permission) {
        if (CollectionUtils.isEmpty(documentIds)) {
            return Set.of();
        }
        // 文档级判定：含创建者豁免、超管、租管、文档 ACL 授权
        Set<Long> accessible = new HashSet<>(
                aclDecisionEngine.accessibleIds(ResourceType.DOCUMENT, permission, documentIds));

        // 仅 READ / EXECUTE 允许回退到知识库授权
        if (permission != Permission.READ && permission != Permission.EXECUTE) {
            return accessible;
        }

        // 无文档级 ACL 记录、且文档级判定未通过的文档，回退到所属知识库授权
        Set<Long> docsWithAcl = selectDocumentIdsWithAcl(documentIds);
        Set<Long> needFallback = documentIds.stream()
                .filter(id -> !docsWithAcl.contains(id))
                .filter(id -> !accessible.contains(id))
                .collect(Collectors.toSet());
        if (needFallback.isEmpty()) {
            return accessible;
        }
        if (aclDecisionEngine.canAccess(ResourceType.WIKI, wikiId, permission)) {
            accessible.addAll(needFallback);
        }
        return accessible;
    }

    @Override
    public Set<Long> restrictedWikiDocumentIds(Long wikiId, Collection<Long> documentIds, Permission permission) {
        if (CollectionUtils.isEmpty(documentIds)) {
            return Set.of();
        }
        Set<Long> accessible = accessibleWikiDocumentIds(wikiId, documentIds, permission);
        return documentIds.stream()
                .filter(id -> !accessible.contains(id))
                .collect(Collectors.toSet());
    }

    /**
     * 查询候选文档中存在 ACL 授权记录的文档 ID 集合（存在即视为已被 ACL 管控，不再回退知识库）。
     */
    private Set<Long> selectDocumentIdsWithAcl(Collection<Long> documentIds) {
        List<AclPermissionDO> aclList = aclService.listAllByResourceType(ResourceType.DOCUMENT);
        if (CollectionUtils.isEmpty(aclList)) {
            return Set.of();
        }
        Set<Long> candidates = new HashSet<>(documentIds);
        return aclList.stream()
                .map(AclPermissionDO::getResourceId)
                .filter(candidates::contains)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Long> getRestrictedWikiIds(Permission permission) {
        List<Long> allIds = DataPermissionUtils.executeIgnore(
                () -> TenantUtils.executeIgnore(wikiMapper::selectAllIds));
        if (allIds.isEmpty()) {
            return Set.of();
        }
        return aclDecisionEngine.restrictedIds(ResourceType.WIKI, permission, allIds);
    }

    @Override
    public Set<Long> getAccessibleWikiIds(Permission permission) {
        List<Long> allIds = DataPermissionUtils.executeIgnore(
                () -> TenantUtils.executeIgnore(wikiMapper::selectAllIds));
        if (allIds.isEmpty()) {
            return Set.of();
        }
        return aclDecisionEngine.accessibleIds(ResourceType.WIKI, permission, allIds);
    }
}
