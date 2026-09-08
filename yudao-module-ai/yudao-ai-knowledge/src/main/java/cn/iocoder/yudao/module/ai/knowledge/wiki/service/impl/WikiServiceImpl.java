package cn.iocoder.yudao.module.ai.knowledge.wiki.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import cn.iocoder.yudao.module.system.service.acl.engine.AclDecisionEngine;
import cn.iocoder.yudao.module.ai.common.enums.TaskStatusEnum;
import cn.iocoder.yudao.module.ai.common.enums.UsageRecordType;
import cn.iocoder.yudao.module.ai.common.model.dto.BasePageListDTO;
import cn.iocoder.yudao.module.ai.common.model.entity.*;
import cn.iocoder.yudao.module.ai.common.service.UsageRecordService;
import cn.iocoder.yudao.module.ai.common.utils.FileUtil;
import cn.iocoder.yudao.module.ai.common.utils.MarkdownStatsUtil;
import cn.iocoder.yudao.module.ai.core.chat.enums.ChunkMethod;
import cn.iocoder.yudao.module.ai.core.rag.service.MilvusStoreService;
import cn.iocoder.yudao.module.ai.knowledge.common.convert.WikiConvert;
import cn.iocoder.yudao.module.ai.knowledge.common.event.ReadKnowledgeEvent;
import cn.iocoder.yudao.module.ai.knowledge.common.event.WikiEmbeddingEvent;
import cn.iocoder.yudao.module.ai.knowledge.common.service.KnowledgeService;
import cn.iocoder.yudao.module.ai.knowledge.common.utils.UmoDocConverter;
import cn.iocoder.yudao.module.ai.knowledge.document.dal.dataobject.Document;
import cn.iocoder.yudao.module.ai.knowledge.document.enums.DocumentTypeEnum;
import cn.iocoder.yudao.module.ai.knowledge.document.model.dto.FindDocumentDetailDTO;
import cn.iocoder.yudao.module.ai.knowledge.document.dal.dataobject.DocumentContent;
import cn.iocoder.yudao.module.ai.knowledge.document.model.vo.FindPreNextArticleVO;
import cn.iocoder.yudao.module.ai.knowledge.document.service.DocumentService;
import cn.iocoder.yudao.module.ai.knowledge.grap.service.WikiGraphService;
import cn.iocoder.yudao.module.ai.knowledge.wiki.enums.WikiCatalogLevelEnum;
import cn.iocoder.yudao.module.ai.knowledge.wiki.dal.mysql.WikiCatalogMapper;
import cn.iocoder.yudao.module.ai.knowledge.wiki.dal.mysql.WikiMapper;
import cn.iocoder.yudao.module.ai.knowledge.wiki.dal.dataobject.Wiki;
import cn.iocoder.yudao.module.ai.knowledge.wiki.dal.dataobject.WikiCatalog;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.dto.*;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.entity.*;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.vo.*;
import cn.iocoder.yudao.module.ai.knowledge.wiki.service.WikiService;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import cn.iocoder.yudao.module.ai.knowledge.document.dal.mysql.DocumentMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.common.constans.ErrorCodeConstants.DOCUMENT_NOT_FOUND;
import static cn.iocoder.yudao.module.ai.common.constans.ErrorCodeConstants.NO_DELETE_PERMISSION;
import static cn.iocoder.yudao.module.ai.common.constans.ErrorCodeConstants.NO_MANAGE_PERMISSION;
import static cn.iocoder.yudao.module.ai.common.constans.ErrorCodeConstants.NO_READ_PERMISSION;
import static cn.iocoder.yudao.module.ai.common.constans.ErrorCodeConstants.NO_WRITE_PERMISSION;
import static cn.iocoder.yudao.module.ai.common.constans.ErrorCodeConstants.WIKI_GRAPH_NOT_FOUND;
import static cn.iocoder.yudao.module.ai.common.constans.ErrorCodeConstants.WIKI_NOT_FOUND;


@Slf4j
@Service
public class WikiServiceImpl implements WikiService {

    private final WikiMapper wikiMapper;

    private final WikiCatalogMapper wikiCatalogMapper;

    private final DocumentMapper documentMapper;

    private final KnowledgeService knowledgeService;

    private final UsageRecordService usageRecordService;

    private final DocumentService documentService;

    private final FileService fileService;

    private final MilvusStoreService milvusStoreService;

    private final WikiGraphService wikiGraphService;

    private final ApplicationEventPublisher eventPublisher;

    private final AclDecisionEngine aclDecisionEngine;

    public WikiServiceImpl(WikiMapper wikiMapper, WikiCatalogMapper wikiCatalogMapper, DocumentMapper documentMapper,
                           KnowledgeService knowledgeService,
                           UsageRecordService usageRecordService, DocumentService documentService,
                           FileService fileService, MilvusStoreService milvusStoreService,
                           WikiGraphService wikiGraphService, ApplicationEventPublisher eventPublisher,
                           AclDecisionEngine aclDecisionEngine) {
        this.wikiMapper = wikiMapper;
        this.wikiCatalogMapper = wikiCatalogMapper;
        this.documentMapper = documentMapper;
        this.knowledgeService = knowledgeService;
        this.usageRecordService = usageRecordService;
        this.documentService = documentService;
        this.fileService = fileService;
        this.milvusStoreService = milvusStoreService;
        this.wikiGraphService = wikiGraphService;
        this.eventPublisher = eventPublisher;
        this.aclDecisionEngine = aclDecisionEngine;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addWiki(AddWikiDTO addWikiDto) {
        Wiki wiki = Wiki.builder()
                .cover(addWikiDto.getCover())
                .title(addWikiDto.getTitle())
                .summary(addWikiDto.getSummary())
                .type(addWikiDto.getType())
                .weight(0).deleted(false)
                .settings(WikiSettings.builder()
                        .autoVectorize(false).chunkMethod(ChunkMethod.HEADING)
                        .chunkSize(3000).overlapSize(200).build())
                .build();
        wikiMapper.insert(wiki);
        // 获取新增记录的主键 ID
        Long wikiId = wiki.getId();

        // 初始化默认目录
        wikiCatalogMapper.insert(WikiCatalog.builder()
                .wikiId(wikiId).title("概述")
                .level(1).deleted(false).isEmbedding(false)
                .sort(1).build());
        wikiCatalogMapper.insert(WikiCatalog.builder()
                .wikiId(wikiId).title("基础")
                .level(1).deleted(false).isEmbedding(false)
                .sort(2).build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWiki(DeleteWikiDTO deleteWikiDto) {
        Long wikiId = deleteWikiDto.getWikiId();
        if (!hasWikiAccess(wikiId, Permission.DELETE)) {
            throw exception(NO_DELETE_PERMISSION);
        }

        // 删除知识库
        int count = wikiMapper.deleteWikiByIds(DeletedStatus.builder()
                .ids(List.of(deleteWikiDto.getWikiId())).deleted(true).build());

        // 若知识库不存在
        if (count == 0) {
            log.warn("该知识库不存在, wikiId: {}", wikiId);
            throw exception(WIKI_NOT_FOUND);
        }

        // 查询此知识库下所有目录
        List<WikiCatalog> wikiCatalogDOS = wikiCatalogMapper.selectByWikiId(wikiId);
        // 过滤目录中所有文档的 ID
        List<Long> docIds = wikiCatalogDOS.stream()
                .filter(wikiCatalogDO -> Objects.nonNull(wikiCatalogDO.getDocumentId())  // 文档 ID 不为空
                        && Objects.equals(wikiCatalogDO.getLevel(), WikiCatalogLevelEnum.TWO.getValue())) // 二级目录
                .map(WikiCatalog::getDocumentId) // 提取文档 ID
                .collect(Collectors.toList());

        if (!wikiGraphService.deleteWiki(wikiId)) {
            throw exception(WIKI_GRAPH_NOT_FOUND);
        }

        milvusStoreService.delDocumentByWiki(wikiId);

        // 更新文档类型 type 为普通
        if (!CollectionUtils.isEmpty(docIds)) {
            documentService.updateTypeByIds(DocumentTypeEnum.NORMAL.getValue(), docIds);
            documentService.clearDocumentChunkKeysByIds(docIds);
        }

        // 删除知识库目录
        wikiCatalogMapper.deleteByWikiId(WikiDeletedStatus.builder()
                .wikiId(wikiId).deleted(1)
                .updater(String.valueOf(getLoginUserId()))
                .updateTime(LocalDateTime.now())
                .build());
    }

    @Override
    public Boolean generateWikiGraph(Long wikiId) {
        if (!hasWikiAccess(wikiId, Permission.MANAGE)) {
            throw exception(NO_MANAGE_PERMISSION);
        }
        log.info("开始为知识库构建图谱，wikiId: {}", wikiId);

        List<WikiCatalog> wikiCatalogs = wikiCatalogMapper.selectByWikiDocumentId(wikiId);

        if (wikiCatalogs.isEmpty()) {
            log.warn("知识库 {} 下没有找到文档", wikiId);
            return false;
        }
        List<Long> docIds = wikiCatalogs.stream().map(WikiCatalog::getDocumentId).toList();
        List<DocumentContent> documentContents = documentService.selectDocumentContentByDocumentIds(docIds);

        // 使用线程池异步处理
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        File tempDir = new File(System.getProperty("java.io.tmpdir"), "kg_temp_" + wikiId);
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            throw new RuntimeException("创建临时目录失败: " + tempDir.getAbsolutePath());
        }

        try {
            for (DocumentContent documentContent : documentContents) {
                CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        File tempFile = FileUtil.createTempFile(tempDir, documentContent.getDocumentId(), documentContent.getContent());
                        try {
                            return wikiGraphService.uploadWikiDocument(wikiId,
                                    documentContent.getDocumentId(), tempFile);
                        } finally {
                            if (tempFile.exists() && !tempFile.delete()) {
                                log.warn("临时文件删除失败: {}", tempFile.getAbsolutePath());
                            }
                        }
                    } catch (Exception e) {
                        log.error("处理文档失败，documentId: {}", documentContent.getDocumentId(), e);
                        return false;
                    }
                }, executor);
                futures.add(future);
            }

            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            long successCount = futures.stream().filter(f -> {
                try {
                    return f.get() != null && f.get();
                } catch (Exception e) {
                    return false;
                }
            }).count();

            log.info("知识库图谱构建完成，wikiId: {}, 总数: {}, 成功: {}",
                    wikiId, documentContents.size(), successCount);

            return successCount == documentContents.size();

        } finally {
            executor.shutdown();
            // 清理临时目录
            if (tempDir.exists() && Objects.requireNonNull(tempDir.listFiles()).length == 0) {
                log.info("清理临时目录状态: {}", tempDir.delete());
            }
        }
    }

    @Override
    public String generateWikiGraphToken() {
        return wikiGraphService.getPermissionsToken();
    }

    @Override
    public WikiSettings getWikiSettings(Long wikiId) {
        Wiki wiki = TenantUtils.executeIgnore(() -> wikiMapper.findWikiById(wikiId));

        if (Objects.isNull(wiki)) {
            throw exception(WIKI_NOT_FOUND);
        }
        if (!hasWikiAccess(wikiId, Permission.MANAGE)) {
            throw exception(NO_MANAGE_PERMISSION);
        }

        return wiki.getSettings();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWikiSettings(UpdateWikiSettingsDTO dto) {
        if (!hasWikiAccess(dto.getId(), Permission.MANAGE)) {
            throw exception(NO_MANAGE_PERMISSION);
        }
        Wiki wiki = wikiMapper.findWikiById(dto.getId());
        if (Objects.isNull(wiki)) {
            throw exception(WIKI_NOT_FOUND);
        }
        wikiMapper.updateWiki(Wiki.builder()
                .id(dto.getId())
                .settings(WikiSettings.builder()
                        .autoVectorize(dto.getAutoVectorize())
                        .chunkMethod(dto.getChunkMethod())
                        .chunkSize(dto.getChunkSize())
                        .overlapSize(dto.getOverlapSize())
                        .build())
                .build());
    }

    @Override
    public PageResult<UsageRecordWikiVO> getUsageRecordWikiPage(BasePageListDTO dto) {
        List<UsageRecord> usageRecords = usageRecordService.selectUserListByType(UsageRecordType.WIKI);
        if (Objects.isNull(usageRecords) || usageRecords.isEmpty()) {
            return new PageResult<>(List.of(), 0L);
        }
        Map<Long, LocalDateTime> usageRecordsHash = usageRecords.stream()
                .collect(Collectors.toMap(
                        UsageRecord::getObjectId,
                        UsageRecord::getUpdateTime
                ));
        List<Long> ids = new ArrayList<>(usageRecordsHash.keySet());

        if (ids.isEmpty()) {
            return new PageResult<>(List.of(), 0L);
        }

        Page<Wiki> wikis = wikiMapper.selectWikiPageByIds(new Page<>(dto.getPage(), dto.getPageSize()), ids);
        List<UsageRecordWikiVO> usageRecordWiki = getUsageRecordWikiVOS(wikis, usageRecordsHash);
        usageRecordWiki.sort(Comparator.comparing(UsageRecordWikiVO::getUsageTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return new PageResult<>(usageRecordWiki, wikis.getTotal());
    }

    private @NonNull List<UsageRecordWikiVO> getUsageRecordWikiVOS(Page<Wiki> wikis, Map<Long, LocalDateTime> usageRecordsHash) {
        List<Wiki> records = wikis.getRecords();
        List<UsageRecordWikiVO> usageRecordWiki = new ArrayList<>();
        records.forEach(wiki -> {
            // 当前知识库内，用户不可读的文档（文档级 ACL 优先，无文档 ACL 时回退知识库授权）
            List<Long> wikiDocumentIds = TenantUtils.executeIgnore(() -> wikiCatalogMapper.selectByWikiDocumentId(wiki.getId()))
                    .stream().map(WikiCatalog::getDocumentId).filter(Objects::nonNull).toList();
            List<Long> restrictedDocumentIds = knowledgeService
                    .restrictedWikiDocumentIds(wiki.getId(), wikiDocumentIds, Permission.READ).stream().toList();
            WikiCatalog wikiCatalogDO = wikiCatalogMapper.selectFirstFilterDocumentId(wiki.getId(), restrictedDocumentIds);
            usageRecordWiki.add(UsageRecordWikiVO.builder().id(wiki.getId()).summary(wiki.getSummary())
                    .usageTime(usageRecordsHash.get(wiki.getId())).type(wiki.getType())
                    .title(wiki.getTitle()).imgUrl(fileService.presignGetUrl(wiki.getCover(), 600))
                    .firstArticleId(Objects.nonNull(wikiCatalogDO) ? wikiCatalogDO.getDocumentId() : null).build());
        });
        return usageRecordWiki;
    }

    @Override
    public PageResult<FindWikiPageListVO> findWikiPageList(FindWikiPageListDTO dto) {
        Page<Wiki> wikiPage = TenantUtils.executeIgnore(() ->
                wikiMapper.pageQuery(new Page<>(dto.getPage(), dto.getPageSize()), dto));
        List<Wiki> records = wikiPage.getRecords();

        List<FindWikiPageListVO> vos = null;
        if (!CollectionUtils.isEmpty(records)) {
            vos = records.stream()
                    .map(WikiConvert.INSTANCE::convertDO2VO)
                    .collect(Collectors.toList());
        }

        // 设置每个知识库的第一篇文档 ID，方便前端跳转
        if (Objects.nonNull(vos)) {
            vos.forEach(vo -> {
                Long wikiId = vo.getId();
                WikiCatalog wikiCatalogDO = TenantUtils.executeIgnore(() -> wikiCatalogMapper.selectFirstDocumentId(wikiId));
                EmbeddingCount embeddingCounts = TenantUtils.executeIgnore(() -> wikiCatalogMapper.countEmbedding(wikiId));
                Long embeddingCount = embeddingCounts.getCount();
                Long embeddingTotal = embeddingCounts.getTotal();
                vo.setTaskStatus(TaskStatusEnum.fromCounts(embeddingCount, embeddingTotal));
                vo.setImgUrl(fileService.presignGetUrl(vo.getCover(), 600));
                vo.setFirstArticleId(Objects.nonNull(wikiCatalogDO) ? wikiCatalogDO.getDocumentId() : null);
                // 设置 ACL 权限列表
                vo.setPermissions(new ArrayList<>(aclDecisionEngine.userPermissions(ResourceType.WIKI, wikiId)));
                // 标记是否为授权过来的内容
                vo.setGranted(!aclDecisionEngine.isCreator(ResourceType.WIKI, wikiId));
            });
        }
        return new PageResult<>(vos, wikiPage.getTotal());
    }

    @Override
    public FindWikiPageListVO getWikiDetail(Long wikiId) {
        Wiki wiki = TenantUtils.executeIgnore(() -> wikiMapper.selectById(wikiId));
        if (wiki == null) {
            return null;
        }
        if (!hasWikiAccess(wikiId, Permission.READ)) {
            throw exception(NO_READ_PERMISSION);
        }
        FindWikiPageListVO vo = WikiConvert.INSTANCE.convertDO2VO(wiki);
        WikiCatalog wikiCatalogDO = TenantUtils.executeIgnore(() -> wikiCatalogMapper.selectFirstDocumentId(wikiId));
        EmbeddingCount embeddingCounts = TenantUtils.executeIgnore(() -> wikiCatalogMapper.countEmbedding(wikiId));
        vo.setTaskStatus(TaskStatusEnum.fromCounts(embeddingCounts.getCount(), embeddingCounts.getTotal()));
        vo.setImgUrl(fileService.presignGetUrl(vo.getCover(), 600));
        vo.setFirstArticleId(Objects.nonNull(wikiCatalogDO) ? wikiCatalogDO.getDocumentId() : null);
        vo.setPermissions(new ArrayList<>(aclDecisionEngine.userPermissions(ResourceType.WIKI, wikiId)));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWikiIsTop(UpdateWikiIsTopDTO updateWikiIsTopDto) {
        Long wikiId = updateWikiIsTopDto.getId();
        if (!hasWikiAccess(wikiId, Permission.MANAGE)) {
            throw exception(NO_MANAGE_PERMISSION);
        }
        Boolean isTop = updateWikiIsTopDto.getIsTop();

        // 默认权重值为 0 ，即不参与置顶
        int weight = 0;
        // 若设置为置顶
        if (isTop) {
            // 查询最大权重值
            Wiki wiki = wikiMapper.selectMaxWeight();
            Integer maxWeight = wiki.getWeight();
            // 最大权重值加一
            weight = maxWeight + 1;
        }

        // 更新该知识库的权重值
        wikiMapper.updateWiki(Wiki.builder().id(wikiId).weight(weight).build());
    }

    @Override
    public void updateWiki(UpdateWikiDTO updateWikiDto) {
        if (!hasWikiAccess(updateWikiDto.getId(), Permission.WRITE)) {
            throw exception(NO_WRITE_PERMISSION);
        }
        Wiki wiki = Wiki.builder()
                .id(updateWikiDto.getId())
                .title(updateWikiDto.getTitle())
                .cover(updateWikiDto.getCover())
                .type(updateWikiDto.getType())
                .summary(updateWikiDto.getSummary())
                .build();
        // 根据 ID 更新知识库
        wikiMapper.updateWiki(wiki);
    }

    @Override
    public List<FindWikiCatalogListVO> findWikiCatalogListByPermission(FindWikiCatalogListDTO dto) {
        if (!this.hasWikiAccess(dto.getWikiId(), Permission.READ)) {
            throw exception(NO_READ_PERMISSION);
        }
        return this.findWikiCatalogList(dto);
    }

    @Override
    public List<FindWikiCatalogListVO> findWikiCatalogList(FindWikiCatalogListDTO dto) {
        Long wikiId = dto.getWikiId();
        if (!this.hasWikiAccess(wikiId, Permission.READ)) {
            throw exception(NO_READ_PERMISSION);
        }
        Wiki wiki = TenantUtils.executeIgnore(() -> wikiMapper.findWikiById(wikiId));

        if (Objects.isNull(wiki)) {
            log.warn("==> 该知识库不存在, wikiId: {}", wikiId);
            throw exception(WIKI_NOT_FOUND);
        }
        // 查询此知识库下所有目录（仅目录节点，不含文档）
        List<WikiCatalog> catalogs = TenantUtils.executeIgnore(() -> wikiCatalogMapper.selectDirectoriesByWikiId(wikiId));

        // DO 转 VO，递归构建多级目录树
        List<FindWikiCatalogListVO> vos = null;
        if (!CollectionUtils.isEmpty(catalogs)) {
            Map<Long, List<WikiCatalog>> parentIdToChildren = catalogs.stream()
                    .collect(Collectors.groupingBy(c -> c.getParentId() == null ? 0L : c.getParentId()));

            List<WikiCatalog> rootCatalogs = parentIdToChildren.getOrDefault(0L, Collections.emptyList());
            rootCatalogs.sort(Comparator.comparing(WikiCatalog::getSort));

            vos = rootCatalogs.stream()
                    .map(root -> buildCatalogVO(root, parentIdToChildren))
                    .collect(Collectors.toList());
        }
        return vos;
    }

    @Override
    public PageResult<FindWikiCatalogListVO> findCatalogDocuments(Long wikiId, Long parentId, Integer page, Integer pageSize) {
        if (!hasWikiAccess(wikiId, Permission.READ)) {
            throw exception(NO_READ_PERMISSION);
        }
        int pageNo = page != null ? page : 1;
        int pageSizeVal = pageSize != null ? pageSize : 10;
        // 当前知识库内，用户不可读的文档（文档级 ACL 优先，无文档 ACL 时回退知识库授权）
        List<Long> wikiDocumentIds = TenantUtils.executeIgnore(() -> wikiCatalogMapper.selectByWikiDocumentId(wikiId))
                .stream()
                .map(WikiCatalog::getDocumentId)
                .filter(Objects::nonNull)
                .toList();
        List<Long> restrictedDocIdList = knowledgeService
                .restrictedWikiDocumentIds(wikiId, wikiDocumentIds, Permission.READ).stream().toList();
        // 数据库分页查询
        long total = TenantUtils.executeIgnore(() -> wikiCatalogMapper.countDocumentsByParentId(wikiId, parentId, restrictedDocIdList));
        int offset = (pageNo - 1) * pageSizeVal;
        List<WikiCatalog> pagedDocs = TenantUtils.executeIgnore(() -> wikiCatalogMapper.selectDocumentsByParentIdPage(
                wikiId, parentId, restrictedDocIdList, offset, pageSizeVal));

        // 批量查询关联的文档，获取分类和标签
        List<Long> documentIds = pagedDocs.stream()
                .map(WikiCatalog::getDocumentId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Document> documentMap = Collections.emptyMap();
        if (!documentIds.isEmpty()) {
            List<Document> documents = TenantUtils.executeIgnore(() -> documentService.selectDocumentByIds(documentIds));
            documentMap = documents.stream()
                    .collect(Collectors.toMap(Document::getId, d -> d, (a, b) -> a));
        }
        final Map<Long, Document> finalDocumentMap = documentMap;

        List<FindWikiCatalogListVO> records = pagedDocs.stream()
                .map(doc -> {
                    Document document = finalDocumentMap.get(doc.getDocumentId());
                    FindWikiCatalogListVO vo = FindWikiCatalogListVO.builder()
                            .id(doc.getId())
                            .documentId(doc.getDocumentId())
                            .title(doc.getTitle())
                            .sort(doc.getSort())
                            .level(doc.getLevel())
                            .isEmbedding(doc.getIsEmbedding())
                            .categoryId(document != null ? document.getCategoryId() : null)
                            .tagIds(document != null ? document.getTagIds() : null)
                            .build();
                    // 设置文档权限列表
                    if (document != null) {
                        vo.setPermissions(new ArrayList<>(
                                aclDecisionEngine.userPermissions(ResourceType.DOCUMENT, document.getId())));
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        return new PageResult<>(records, total);
    }

    @Override
    public Map<Long, List<FindBatchCatalogDocumentVO>> findBatchCatalogDocuments(Long wikiId, List<Long> parentIds) {
        if (!hasWikiAccess(wikiId, Permission.READ)) {
            throw exception(NO_READ_PERMISSION);
        }
        if (CollectionUtils.isEmpty(parentIds)) {
            return Collections.emptyMap();
        }
        // 当前知识库内，用户不可读的文档（文档级 ACL 优先，无文档 ACL 时回退知识库授权）
        List<Long> wikiDocumentIds = TenantUtils.executeIgnore(() -> wikiCatalogMapper.selectByWikiDocumentId(wikiId))
                .stream()
                .map(WikiCatalog::getDocumentId)
                .filter(Objects::nonNull)
                .toList();
        List<Long> restrictedDocIdList = knowledgeService
                .restrictedWikiDocumentIds(wikiId, wikiDocumentIds, Permission.READ).stream().toList();
        // 批量查询所有目录下的文档
        List<WikiCatalog> allDocs = TenantUtils.executeIgnore(() -> wikiCatalogMapper.selectDocumentsByParentIds(
                wikiId, parentIds, restrictedDocIdList));

        // 按 parentId 分组
        Map<Long, List<WikiCatalog>> groupedByParent = allDocs.stream()
                .collect(Collectors.groupingBy(WikiCatalog::getParentId));

        // 构建返回结果
        Map<Long, List<FindBatchCatalogDocumentVO>> result = new HashMap<>();
        for (Long parentId : parentIds) {
            List<WikiCatalog> docs = groupedByParent.getOrDefault(parentId, Collections.emptyList());
            List<FindBatchCatalogDocumentVO> vos = docs.stream()
                    .map(doc -> FindBatchCatalogDocumentVO.builder()
                            .id(doc.getId())
                            .documentId(doc.getDocumentId())
                            .title(doc.getTitle())
                            .build())
                    .collect(Collectors.toList());
            result.put(parentId, vos);
        }
        return result;
    }

    @Override
    public PageResult<FindWikiCatalogListVO> findUnarchivedDocuments(Long wikiId, String title, Long categoryId, List<Long> tagIds, Integer page, Integer pageSize) {
        if (!hasWikiAccess(wikiId, Permission.WRITE)) {
            throw exception(NO_WRITE_PERMISSION);
        }
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(page != null ? page : 1);
        pageParam.setPageSize(pageSize != null ? pageSize : 10);
        PageResult<Document> result = documentMapper.selectUnarchivedDocuments(pageParam, title, categoryId, tagIds);
        List<FindWikiCatalogListVO> records = result.getList().stream()
                .map(doc -> FindWikiCatalogListVO.builder()
                        .id(doc.getId())
                        .documentId(doc.getId())
                        .title(doc.getTitle())
                        .categoryId(doc.getCategoryId())
                        .tagIds(doc.getTagIds())
                        .build())
                .collect(Collectors.toList());
        return new PageResult<>(records, result.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveDocuments(ArchiveWikiDocumentDTO dto) {
        Long wikiId = dto.getWikiId();
        if (!hasWikiAccess(wikiId, Permission.WRITE)) {
            throw exception(NO_WRITE_PERMISSION);
        }
        Long parentId = dto.getParentId();
        // 获取当前父目录下已有文档的最大 sort
        Integer maxSort = 0;
        List<WikiCatalog> existingDocs = wikiCatalogMapper.selectDocumentsByParentId(wikiId, parentId, null);
        if (!CollectionUtils.isEmpty(existingDocs)) {
            maxSort = existingDocs.stream()
                    .mapToInt(d -> d.getSort() != null ? d.getSort() : 0)
                    .max().orElse(0);
        }
        List<Long> documentIds = dto.getDocumentIds();
        for (Long documentId : documentIds) {
            maxSort++;
            Document doc = documentService.selectById(documentId);
            if (doc == null) continue;
            WikiCatalog wikiCatalog = WikiCatalog.builder()
                    .wikiId(wikiId)
                    .documentId(documentId)
                    .title(doc.getTitle())
                    .level(2)
                    .parentId(parentId)
                    .sort(maxSort)
                    .deleted(Boolean.FALSE)
                    .isEmbedding(Boolean.FALSE)
                    .build();
            wikiCatalogMapper.insert(wikiCatalog);
        }
        // 批量更新文档类型为 WIKI
        documentService.updateTypeByIds(DocumentTypeEnum.WIKI.getValue(), documentIds);
        WikiSettings wikiSettings = this.getWikiSettings(wikiId);
        if (wikiSettings.getAutoVectorize() && !documentIds.isEmpty()) {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        passageEmbedding(wikiId);
                    }
                });
            } else {
                passageEmbedding(wikiId);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeDocumentFromCatalog(Long wikiId, Long documentId) {
        if (!aclDecisionEngine.canAccess(ResourceType.DOCUMENT, documentId, Permission.DELETE)) {
            throw exception(NO_DELETE_PERMISSION);
        }
        wikiCatalogMapper.removeDocumentFromCatalog(wikiId, documentId);
        // 检查文档是否还在其他知识库目录中
        List<Long> docIds = List.of(documentId);
        List<WikiCatalogIdSet> remaining = knowledgeService.selectWikiCatalogIdSetByDocumentIds(docIds);
        if (CollectionUtils.isEmpty(remaining)) {
            documentService.updateTypeByIds(DocumentTypeEnum.NORMAL.getValue(), docIds);
        }
        milvusStoreService.delDocumentByDocIds(docIds);
        documentService.clearDocumentChunkKeysByIds(docIds);
    }

    /**
     * 递归构建目录 VO 树
     */
    private FindWikiCatalogListVO buildCatalogVO(WikiCatalog catalog, Map<Long, List<WikiCatalog>> parentIdToChildren) {
        FindWikiCatalogListVO vo = FindWikiCatalogListVO.builder()
                .id(catalog.getId())
                .documentId(catalog.getDocumentId())
                .title(catalog.getTitle())
                .level(catalog.getLevel())
                .sort(catalog.getSort())
                .isEmbedding(catalog.getIsEmbedding())
                .editing(Boolean.FALSE)
                .build();

        List<WikiCatalog> children = parentIdToChildren.getOrDefault(catalog.getId(), Collections.emptyList());
        if (!children.isEmpty()) {
            children.sort(Comparator.comparing(WikiCatalog::getSort));
            vo.setChildren(children.stream()
                    .map(child -> buildCatalogVO(child, parentIdToChildren))
                    .collect(Collectors.toList()));
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateWikiCatalogs(UpdateWikiCatalogDTO dto) {
        // 知识库 ID
        Long wikiId = dto.getId();
        if (!hasWikiAccess(wikiId, Permission.WRITE)) {
            throw exception(NO_WRITE_PERMISSION);
        }
        // 目录
        List<UpdateWikiCatalogItemDTO> catalogs = dto.getCatalogs();

        // 获取当前知识库中的所有文档ID（旧数据）
        List<WikiCatalog> oldWikiCatalogDOS = wikiCatalogMapper.selectByWikiId(wikiId);
        List<Long> oldDocIds = oldWikiCatalogDOS.stream()
                .map(WikiCatalog::getDocumentId)
                .filter(Objects::nonNull)
                .toList();

        // 递归获取新目录中的所有文档ID
        List<Long> newDocIds = Lists.newArrayList();
        collectDocIds(catalogs, newDocIds);

        // 计算被移除的文档ID（在旧数据中但不在新数据中）
        List<Long> removedDocIds = oldDocIds.stream()
                .filter(docId -> !newDocIds.contains(docId))
                .collect(Collectors.toList());

        // 删除被移除文档在Milvus中的向量数据
        if (!CollectionUtils.isEmpty(removedDocIds)) {
            milvusStoreService.delDocumentByDocIds(removedDocIds);
            documentService.clearDocumentChunkKeysByIds(removedDocIds);
        }

        // 将所有旧文档类型更新为普通
        if (!CollectionUtils.isEmpty(removedDocIds)) {
            documentService.updateTypeByIds(DocumentTypeEnum.NORMAL.getValue(), removedDocIds);
        }

        Map<Long, WikiCatalog> oldCatalogMap = oldWikiCatalogDOS.stream()
                .collect(Collectors.toMap(WikiCatalog::getId, catalog -> catalog));
        // 递归收集新目录树中的所有目录 ID
        Set<Long> newCatalogIds = new HashSet<>();
        collectCatalogIds(catalogs, newCatalogIds);

        // 删除新目录中不存在的旧目录
        List<Long> removedCatalogIds = oldCatalogMap.keySet().stream()
                .filter(catalogId -> !newCatalogIds.contains(catalogId))
                .toList();
        if (!CollectionUtils.isEmpty(removedCatalogIds)) {
            wikiCatalogMapper.deleteWikiCatalogByIds(WikiDeletedStatus.builder()
                    .wikiId(wikiId).ids(removedCatalogIds).deleted(1)
                    .updater(String.valueOf(getLoginUserId()))
                    .updateTime(LocalDateTime.now())
                    .build());
        }

        // 重新设置排序并递归保存目录树
        if (CollectionUtils.isEmpty(catalogs)) {
            return true;
        }
        for (int i = 0; i < catalogs.size(); i++) {
            UpdateWikiCatalogItemDTO catalog = catalogs.get(i);
            catalog.setSort(i + 1);
            saveCatalogRecursive(wikiId, catalog, null, 1, oldCatalogMap);
        }

        return true;
    }

    /**
     * 递归收集目录树中的所有文档 ID
     */
    private void collectDocIds(List<UpdateWikiCatalogItemDTO> catalogs, List<Long> docIds) {
        if (CollectionUtils.isEmpty(catalogs)) return;
        for (UpdateWikiCatalogItemDTO catalog : catalogs) {
            if (catalog.getDocumentId() != null) {
                docIds.add(catalog.getDocumentId());
            }
            if (!CollectionUtils.isEmpty(catalog.getChildren())) {
                collectDocIds(catalog.getChildren(), docIds);
            }
        }
    }

    /**
     * 递归收集目录树中的所有目录 ID
     */
    private void collectCatalogIds(List<UpdateWikiCatalogItemDTO> catalogs, Set<Long> ids) {
        if (CollectionUtils.isEmpty(catalogs)) return;
        for (UpdateWikiCatalogItemDTO catalog : catalogs) {
            if (catalog.getId() != null) {
                ids.add(catalog.getId());
            }
            if (!CollectionUtils.isEmpty(catalog.getChildren())) {
                collectCatalogIds(catalog.getChildren(), ids);
            }
        }
    }

    /**
     * 递归保存目录树
     */
    private void saveCatalogRecursive(Long wikiId, UpdateWikiCatalogItemDTO catalog, Long parentId,
                                       int level, Map<Long, WikiCatalog> oldCatalogMap) {
        WikiCatalog wikiCatalog = WikiCatalog.builder()
                .id(catalog.getId())
                .wikiId(wikiId)
                .title(catalog.getTitle())
                .level(level)
                .sort(catalog.getSort())
                .documentId(catalog.getDocumentId())
                .isEmbedding(catalog.getIsEmbedding())
                .parentId(parentId)
                .deleted(Boolean.FALSE)
                .build();
        if (catalog.getId() != null && oldCatalogMap.containsKey(catalog.getId())) {
            // 自定义 SQL 不会触发 MyBatis-Plus 自动填充，需手动设置 updater 和 updateTime
            wikiCatalog.setUpdater(String.valueOf(getLoginUserId()));
            wikiCatalog.setUpdateTime(LocalDateTime.now());
            wikiCatalogMapper.updateWikiCatalog(wikiCatalog);
        } else {
            wikiCatalog.setId(null);
            wikiCatalogMapper.insert(wikiCatalog);
        }
        // 有文档ID的目录节点，更新文档类型为 WIKI
        if (catalog.getDocumentId() != null) {
            documentService.updateTypeByIds(DocumentTypeEnum.WIKI.getValue(), List.of(catalog.getDocumentId()));
        }

        // 递归保存子目录
        List<UpdateWikiCatalogItemDTO> children = catalog.getChildren();
        if (!CollectionUtils.isEmpty(children)) {
            for (int i = 0; i < children.size(); i++) {
                UpdateWikiCatalogItemDTO child = children.get(i);
                child.setSort(i + 1);
                saveCatalogRecursive(wikiId, child, wikiCatalog.getId(), level + 1, oldCatalogMap);
            }
        }
    }

    @Override
    public List<WikiCatalog> findWikiDocumentById(Long wikiId) {
        return wikiCatalogMapper.selectByWikiDocumentId(wikiId);
    }

    @Override
    public List<WikiCatalogueTransformer> findWikiDetailByIds(List<Long> wikiIds) {
        if (wikiIds == null || wikiIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Wiki> wikis = wikiMapper.selectWikiByIds(wikiIds);
        return getWikiDetailVOS(wikis);
    }

    @Override
    public void setEmbeddingStatus(List<Long> ids, Boolean isEmbedding) {
        wikiCatalogMapper.updateIsEmbedding(ids, isEmbedding);
    }

    @Override
    public WikiCatalog findWikiCatalogByDocumentId(Long documentId) {
        return wikiCatalogMapper.selectWikiCatalogByDocumentId(documentId);
    }

    @Override
    public FindWikiDocumentDetailVO findWikiDetail(FindDocumentDetailDTO findDocumentDetailDTO) {
        Long documentId = findDocumentDetailDTO.getDocumentId();
        Long wikiId = findDocumentDetailDTO.getWikiId();

        Document documentDO = TenantUtils.executeIgnore(() -> documentService.selectDocumentById(documentId));

        // 判断文档是否存在
        if (Objects.isNull(documentDO)) {
            log.warn("==> 该文档不存在, documentId: {}", documentId);
            throw exception(DOCUMENT_NOT_FOUND);
        }

        // 前端未传知识库 ID 时，反查文档所属知识库，保证知识库回退判定可用
        if (Objects.isNull(wikiId)) {
            WikiCatalog catalog = TenantUtils.executeIgnore(() ->
                    wikiCatalogMapper.selectWikiCatalogByDocumentId(documentId));
            wikiId = Objects.nonNull(catalog) ? catalog.getWikiId() : null;
        }

        if (!knowledgeService.hasWikiDocumentAccess(wikiId, documentId, Permission.READ)) {
            throw exception(NO_READ_PERMISSION);
        }
        // 上下篇只在当前知识库范围内计算，并仅包含当前用户可读的文档
        Long finalWikiId = wikiId;
        List<Long> wikiDocumentIds = TenantUtils.executeIgnore(() ->
                        wikiCatalogMapper.selectByWikiDocumentId(finalWikiId))
                .stream()
                .map(WikiCatalog::getDocumentId)
                .filter(Objects::nonNull)
                .toList();
        List<Long> permissionIds = new ArrayList<>(
                knowledgeService.accessibleWikiDocumentIds(wikiId, wikiDocumentIds, Permission.READ));

        // 查询正文
        DocumentContent documentContentDO = TenantUtils.executeIgnore(() ->
                documentService.selectByDocumentId(documentId));
        String content = documentContentDO.getContent();

        // 计算 md 正文字数
        int totalWords = MarkdownStatsUtil.calculateWordCount(content);

        // DO 转 VO
        FindWikiDocumentDetailVO vo = FindWikiDocumentDetailVO.builder()
                .title(documentDO.getTitle())
                .createTime(documentDO.getCreateTime())
                .content(UmoDocConverter.resolveContentFileUrls(content, fileId -> fileService.presignGetUrl(fileId, 3600))).summary(documentDO.getSummary())
                .readNum(documentDO.getReadNum())
                .totalWords(totalWords).fileId(documentDO.getFileId())
                .readTime(MarkdownStatsUtil.calculateReadingTime(totalWords))
                .updateTime(documentDO.getUpdateTime())
                .build();
        vo.setPermissions(new ArrayList<>(
                aclDecisionEngine.userPermissions(ResourceType.DOCUMENT, documentId)));
        // 查询所属分类
        DictValue dictValue = TenantUtils.executeIgnore(() ->
                documentService.selectCategoryByDocumentId(documentId));
        if (Objects.nonNull(dictValue)) {
            vo.setCategoryId(dictValue.getId());
            vo.setCategoryName(dictValue.getValue());
        }

        // 查询标签
        List<DictValue> dictValues = TenantUtils.executeIgnore(() ->
                documentService.selectTagsByDocumentId(documentId));
        List<Tag> tags = new ArrayList<>();
        dictValues.forEach(value -> tags.add(Tag.builder()
                .id(value.getId()).name(value.getValue()).build()));
        vo.setTags(tags);

        // 上一篇
        Document preDocumentDO = TenantUtils.executeIgnore(() ->
                documentService.selectPreDocumentFilter(documentId, permissionIds));
        if (Objects.nonNull(preDocumentDO)) {
            FindPreNextArticleVO preArticleVO = FindPreNextArticleVO.builder()
                    .documentId(preDocumentDO.getId())
                    .articleTitle(preDocumentDO.getTitle())
                    .build();
            vo.setPreArticle(preArticleVO);
        }

        // 下一篇
        Document nextDocumentDO = TenantUtils.executeIgnore(() ->
                documentService.selectNextDocumentFilter(documentId, permissionIds));
        if (Objects.nonNull(nextDocumentDO)) {
            FindPreNextArticleVO nextArticleVO = FindPreNextArticleVO.builder()
                    .documentId(nextDocumentDO.getId())
                    .articleTitle(nextDocumentDO.getTitle())
                    .build();
            vo.setNextArticle(nextArticleVO);
        }

        // 发布文档阅读事件
        eventPublisher.publishEvent(new ReadKnowledgeEvent(this, wikiId, documentId));

        return vo;
    }

    @Override
    public void passageEmbedding(Long wikiId) {
        if (!hasWikiAccess(wikiId, Permission.MANAGE)) {
            throw exception(NO_MANAGE_PERMISSION);
        }
        Wiki wiki = wikiMapper.findWikiById(wikiId);
        if (Objects.nonNull(wiki)) {
            eventPublisher.publishEvent(new WikiEmbeddingEvent(
                    this, wiki));
        }

    }

    @Override
    public PageResult<FindUserWikiPageListVO> findWikiPermissionPageList(FindAccessibleWikiPageListDTO dto) {
        Page<Wiki> wikiPage = wikiMapper.pagePublishQuery(new Page<>(dto.getPage(), dto.getPageSize()), dto);

        return getPageResult(wikiPage.getTotal(), wikiPage.getRecords());
    }

    @Override
    public List<HotWikiVO> getHotWiki(Integer limit) {
        List<WikiHot> hotWikis = knowledgeService.selectHotWikiByPermission(limit);
        List<HotWikiVO> hotWikiVOs = new ArrayList<>();
        hotWikis.forEach(item -> {
            Long wikiId = item.getId();
            HotWikiVO build = HotWikiVO.builder().id(wikiId).title(item.getTitle())
                    .viewCount(item.getViewCount()).build();
            hotWikiVOs.add(build);
            WikiCatalog wikiCatalogDO = wikiCatalogMapper.selectFirstDocumentId(wikiId);
            build.setImgUrl(fileService.presignGetUrl(item.getCover(), 600));
            build.setFirstArticleId(Objects.nonNull(wikiCatalogDO) ? wikiCatalogDO.getDocumentId() : null);
        });
        return hotWikiVOs;
    }

    @Override
    public List<WikiDetailTransformer> findWikiDetailByPermission() {
        List<Wiki> wikis = knowledgeService.getAccessibleEmbeddedWikis(Permission.READ);
        return wikis.stream().map(wiki -> WikiDetailTransformer.builder()
                .wikiId(wiki.getId()).title(wiki.getTitle())
                .summary(wiki.getSummary()).build()).toList();
    }

    @NotNull
    private List<WikiCatalogueTransformer> getWikiDetailVOS(List<Wiki> wikis) {
        List<WikiCatalogueTransformer> wikiDetailVOS = Lists.newArrayList();
        wikis.forEach(wiki -> {
            WikiCatalogueTransformer build = WikiCatalogueTransformer.builder().wikiId(wiki.getId()).title(wiki.getTitle())
                    .summary(wiki.getSummary()).catalogue(new ArrayList<>()).build();
            wikiDetailVOS.add(build);
            List<FindWikiCatalogListVO> wikiCatalogList = this.findWikiCatalogListByPermission(
                    FindWikiCatalogListDTO.builder().wikiId(wiki.getId()).build());
            wikiCatalogList.forEach(wikiCatalog -> {
                List<WikiDetailCatalog> catalogue = build.getCatalogue();
                catalogue.add(convertToWikiDetailCatalog(wikiCatalog));
            });
        });
        return wikiDetailVOS;
    }

    /**
     * 递归将 FindWikiCatalogListVO 转换为 WikiDetailCatalog，支持多级目录
     */
    private WikiDetailCatalog convertToWikiDetailCatalog(FindWikiCatalogListVO vo) {
        WikiDetailCatalog catalog = WikiDetailCatalog.builder()
                .catalogId(vo.getId())
                .title(vo.getTitle())
                .build();
        List<FindWikiCatalogListVO> children = vo.getChildren();
        if (Objects.nonNull(children) && !children.isEmpty()) {
            catalog.setChildren(children.stream()
                    .map(this::convertToWikiDetailCatalog)
                    .toList());
        }
        return catalog;
    }

    @NotNull
    private PageResult<FindUserWikiPageListVO> getPageResult(long total, List<Wiki> records) {
        List<FindUserWikiPageListVO> list = new ArrayList<>();
        records.forEach(record -> {
            Long wikiId = record.getId();
            FindUserWikiPageListVO build = FindUserWikiPageListVO.builder().id(record.getId())
                    .title(record.getTitle()).summary(record.getSummary()).type(record.getType())
                    .updateTime(record.getUpdateTime()).isTop(record.getWeight() > 0).build();

            EmbeddingCount embeddingCounts = wikiCatalogMapper.countEmbedding(wikiId);
            Long embeddingCount = embeddingCounts.getCount();
            Long embeddingTotal = embeddingCounts.getTotal();
            build.setTaskStatus(TaskStatusEnum.fromCounts(embeddingCount, embeddingTotal));
            // 当前知识库内，用户不可读的文档（文档级 ACL 优先，无文档 ACL 时回退知识库授权）
            List<Long> wikiDocumentIds = TenantUtils.executeIgnore(() -> wikiCatalogMapper.selectByWikiDocumentId(wikiId))
                    .stream().map(WikiCatalog::getDocumentId).filter(Objects::nonNull).toList();
            List<Long> restrictedDocumentIds = knowledgeService
                    .restrictedWikiDocumentIds(wikiId, wikiDocumentIds, Permission.READ).stream().toList();
            WikiCatalog wikiCatalogDO = wikiCatalogMapper.selectFirstFilterDocumentId(wikiId, restrictedDocumentIds);
            build.setImgUrl(fileService.presignGetUrl(record.getCover(), 600));
            build.setFirstArticleId(Objects.nonNull(wikiCatalogDO) ? wikiCatalogDO.getDocumentId() : null);
            build.setPermissions(new ArrayList<>(aclDecisionEngine.userPermissions(ResourceType.WIKI, wikiId)));
            list.add(build);
        });
        return new PageResult<>(list, total);
    }

    @Override
    public Boolean hasWikiAccess(Long wikiId, Permission permission) {
        return aclDecisionEngine.canAccess(ResourceType.WIKI, wikiId, permission);
    }

    @Override
    public Set<Long> hasWikisAccess(List<Long> wikiIds, Permission permission) {
        if (wikiIds == null || wikiIds.isEmpty()) {
            return Set.of();
        }
        return aclDecisionEngine.accessibleIds(ResourceType.WIKI, permission, wikiIds);
    }
}
