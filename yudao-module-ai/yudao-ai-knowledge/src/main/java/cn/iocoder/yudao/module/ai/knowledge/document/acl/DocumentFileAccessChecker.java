package cn.iocoder.yudao.module.ai.knowledge.document.acl;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai.knowledge.common.service.KnowledgeService;
import cn.iocoder.yudao.module.ai.knowledge.document.dal.dataobject.Document;
import cn.iocoder.yudao.module.ai.knowledge.document.dal.mysql.DocumentMapper;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.entity.WikiCatalogIdSet;
import cn.iocoder.yudao.module.infra.service.file.FileAccessChecker;
import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import cn.iocoder.yudao.module.system.service.acl.engine.AclDecisionEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文档文件访问权限检查器
 * <p>
 * 当文件通过 {@code ai_document.file_id}（封面/主文件）或
 * {@code ai_document.resource_ids}（正文引用的图片/视频/音频/附件资源）关联到文档时，
 * 只要当前用户对该文档拥有 READ 权限，即可访问该文件。
 * <p>
 * 判定口径与 {@link KnowledgeService#getRestrictedDocumentIdsWithWikiFallback} 保持一致：
 * 文档级 ACL 优先（含创建者豁免、超管、文档 ACL 授权）；文档无 ACL 记录时，
 * 回退到所属知识库授权（仅 READ / EXECUTE 支持回退）。
 * <p>
 * 性能：文档 → 知识库映射与权限判定均按批量查询（一次文件关联查询 + 一次映射查询 +
 * 按知识库分组的批量判定），避免逐文档循环查询。
 *
 * @author IIMS
 */
@Component
@RequiredArgsConstructor
public class DocumentFileAccessChecker implements FileAccessChecker {

    private final DocumentMapper documentMapper;
    private final KnowledgeService knowledgeService;
    private final AclDecisionEngine aclDecisionEngine;

    @Override
    public boolean canAccess(Long fileId) {
        if (fileId == null) {
            return false;
        }
        List<Document> documents = TenantUtils.executeIgnore(() ->
                documentMapper.selectByFileId(fileId));
        if (documents == null || documents.isEmpty()) {
            return false;
        }
        return hasAnyDocumentReadAccess(documents);
    }

    /**
     * 批量判定任一文档可读：无知识库归属的文档按文档级 ACL 判定；
     * 归属知识库的文档按知识库分组，含文档级判定与知识库继承回退。
     */
    private boolean hasAnyDocumentReadAccess(List<Document> documents) {
        List<Long> documentIds = documents.stream().map(Document::getId).toList();
        // 1) 批量查文档 → 知识库映射（跨租户，与 getRestrictedDocumentIdsWithWikiFallback 口径一致）
        Map<Long, Long> wikiIdMap = TenantUtils.executeIgnore(() ->
                        knowledgeService.selectWikiCatalogIdSetByDocumentIds(documentIds)).stream()
                .collect(Collectors.toMap(WikiCatalogIdSet::getDocumentId, WikiCatalogIdSet::getWikiId,
                        (a, b) -> a));
        // 2) 未归属任何知识库的文档：仅按文档级判定（批量）
        List<Long> standaloneIds = documentIds.stream()
                .filter(id -> !wikiIdMap.containsKey(id))
                .toList();
        if (!standaloneIds.isEmpty()) {
            Set<Long> accessible = aclDecisionEngine.accessibleIds(
                    ResourceType.DOCUMENT, Permission.READ, standaloneIds);
            if (!accessible.isEmpty()) {
                return true;
            }
        }
        // 3) 归属知识库的文档：按知识库分组批量判定（含文档级 ACL 与知识库继承回退）
        Map<Long, List<Long>> byWiki = documentIds.stream()
                .filter(wikiIdMap::containsKey)
                .collect(Collectors.groupingBy(wikiIdMap::get));
        for (Map.Entry<Long, List<Long>> entry : byWiki.entrySet()) {
            Set<Long> accessible = knowledgeService.accessibleWikiDocumentIds(
                    entry.getKey(), entry.getValue(), Permission.READ);
            if (!accessible.isEmpty()) {
                return true;
            }
        }
        return false;
    }

}
