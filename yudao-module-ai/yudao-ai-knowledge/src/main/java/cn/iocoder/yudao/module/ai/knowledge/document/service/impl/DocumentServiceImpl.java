package cn.iocoder.yudao.module.ai.knowledge.document.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.system.enums.acl.Permission;
import cn.iocoder.yudao.module.system.enums.acl.ResourceType;
import cn.iocoder.yudao.module.system.service.acl.engine.AclDecisionEngine;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.ai.common.enums.CoreDictType;
import cn.iocoder.yudao.module.ai.common.enums.TaskPriority;
import cn.iocoder.yudao.module.ai.common.enums.UsageRecordType;
import cn.iocoder.yudao.module.ai.common.event.KBConversionEvent;
import cn.iocoder.yudao.module.ai.common.model.dto.BasePageListDTO;
import cn.iocoder.yudao.module.ai.common.model.entity.*;
import cn.iocoder.yudao.module.ai.common.service.DocumentConversionService;
import cn.iocoder.yudao.module.ai.common.service.UsageRecordService;
import cn.iocoder.yudao.module.ai.common.task.SmartTaskScheduler;
import cn.iocoder.yudao.module.ai.common.utils.MarkdownStatsUtil;
import cn.iocoder.yudao.module.ai.knowledge.common.utils.UmoDocConverter;
import cn.iocoder.yudao.module.ai.core.chat.service.AiModelToolService;
import cn.iocoder.yudao.module.ai.core.rag.service.MilvusStoreService;
import cn.iocoder.yudao.module.ai.core.rag.utils.MarkdownSplitter;
import cn.iocoder.yudao.module.ai.knowledge.common.event.ReadKnowledgeEvent;
import cn.iocoder.yudao.module.ai.knowledge.common.event.WriteWikiDocEvent;
import cn.iocoder.yudao.module.ai.knowledge.common.service.KnowledgeService;
import cn.iocoder.yudao.module.ai.knowledge.document.dal.dataobject.Document;
import cn.iocoder.yudao.module.ai.knowledge.document.model.dto.*;
import cn.iocoder.yudao.module.ai.knowledge.document.dal.dataobject.DocumentContent;
import cn.iocoder.yudao.module.ai.knowledge.document.model.entity.*;
import cn.iocoder.yudao.module.ai.knowledge.document.model.vo.*;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.entity.WikiCatalogIdSet;
import cn.iocoder.yudao.module.ai.search.enums.IndexNameEnum;
import cn.iocoder.yudao.module.ai.search.repository.GenericElasticsearchRepository;
import cn.iocoder.yudao.module.ai.knowledge.document.dal.mysql.DocumentContentMapper;
import cn.iocoder.yudao.module.ai.knowledge.document.dal.mysql.DocumentMapper;
import cn.iocoder.yudao.module.ai.knowledge.document.service.DocumentService;
import cn.iocoder.yudao.module.ai.knowledge.grap.service.WikiGraphService;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import cn.iocoder.yudao.module.system.dal.dataobject.dict.DictDataDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.dict.DictDataService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.ai.common.constans.ErrorCodeConstants.DOCUMENT_NOT_FOUND;
import static cn.iocoder.yudao.module.ai.common.constans.ErrorCodeConstants.NO_READ_PERMISSION;

@Slf4j
@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentMapper documentMapper;

    private final AdminUserService adminUserService;

    private final DocumentContentMapper documentContentMapper;

    private final KnowledgeService knowledgeService;

    private final DictDataService dictDataService;

    private final FileService fileService;

    private final ApplicationEventPublisher eventPublisher;

    private final AiModelToolService aiModelToolService;

    private final UsageRecordService usageRecordService;

    private final MilvusStoreService milvusStoreService;

    private final DocumentConversionService documentConversionService;

    private final WikiGraphService wikiGraphService;

    private final GenericElasticsearchRepository<DocumentSearch> searchRepository;

    private final SmartTaskScheduler smartTaskScheduler;

    private final AclDecisionEngine aclDecisionEngine;

    public DocumentServiceImpl(DocumentMapper documentMapper, AdminUserService adminUserService, DocumentContentMapper documentContentMapper, KnowledgeService knowledgeService, DictDataService dictDataService, FileService fileService, ApplicationEventPublisher eventPublisher, AiModelToolService aiModelToolService, UsageRecordService usageRecordService, MilvusStoreService milvusStoreService, DocumentConversionService documentConversionService, WikiGraphService wikiGraphService, GenericElasticsearchRepository<DocumentSearch> searchRepository, SmartTaskScheduler smartTaskScheduler, AclDecisionEngine aclDecisionEngine) {
        this.documentMapper = documentMapper;
        this.adminUserService = adminUserService;
        this.documentContentMapper = documentContentMapper;
        this.knowledgeService = knowledgeService;
        this.dictDataService = dictDataService;
        this.fileService = fileService;
        this.eventPublisher = eventPublisher;
        this.aiModelToolService = aiModelToolService;
        this.usageRecordService = usageRecordService;
        this.milvusStoreService = milvusStoreService;
        this.documentConversionService = documentConversionService;
        this.wikiGraphService = wikiGraphService;
        this.searchRepository = searchRepository;
        this.smartTaskScheduler = smartTaskScheduler;
        this.aclDecisionEngine = aclDecisionEngine;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDocument(PublishDocumentDTO publishDocumentDto) {
        createDocumentReturnId(publishDocumentDto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDocumentReturnId(PublishDocumentDTO publishDocumentDto) {

        Long cover = publishDocumentDto.getCover();
        Boolean isTop = publishDocumentDto.getIsTop();
        Document document = Document.builder()
                .title(publishDocumentDto.getTitle())
                .cover(cover)
                .summary(publishDocumentDto.getSummary())
                .weight(isTop ? documentMapper.selectMaxWeight() + 1 : 0)
                .categoryId(publishDocumentDto.getCategoryId())
                .tagIds(publishDocumentDto.getTagIds())
                .build();
        documentMapper.insert(document);
        Long documentId = document.getId();
        loadingDocumentIndex(document, documentId);

        DocumentContent documentContent = DocumentContent.builder()
                .documentId(documentId)
                .content(publishDocumentDto.getContent())
                .build();
        documentContentMapper.insert(documentContent);
        return documentId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocument(List<Long> ids) {
        if (!ids.isEmpty()) {
            DeletedStatus deletedStatus = DeletedStatus.builder()
                    .deleted(true).ids(ids).build();

            documentMapper.updateDocumentDeleted(deletedStatus);
            this.clearDocumentChunkKeysByIds(ids);

            List<WikiCatalogIdSet> wikiCatalogIdSets = knowledgeService.selectWikiCatalogIdSetByDocumentIds(ids);
            List<Long> documentIds = wikiCatalogIdSets.stream()
                    .map(WikiCatalogIdSet::getDocumentId).toList();
            knowledgeService.deleteWikiCatalogsByDocumentIds(documentIds);

            smartTaskScheduler.submit(() -> {
                milvusStoreService.delDocumentByDocIds(ids);
                wikiCatalogIdSets.forEach(wiki ->
                        wikiGraphService.deleteWikiDocument(wiki.getWikiId(), wiki.getDocumentId()));
                try {
                    searchRepository.bulkDelete(IndexNameEnum.DOCUMENT, ids.stream()
                            .map(String::valueOf)
                            .collect(Collectors.toList()));
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }, TaskPriority.NORMAL, "cleanup");
        }
    }

    @Override
    public void updateChunkKeys(Long id, List<String> chunkKeys) {
        documentContentMapper.updateByDocumentId(DocumentContent.builder()
                .documentId(id).chunkKeys(chunkKeys).build());
    }

    @Override
    public PageResult<FindDocumentPageListVO> findDocumentPageList(FindDocumentPageListDTO findDocumentPageListDto) {
        String title = findDocumentPageListDto.getTitle();
        LocalDateTime startDate = findDocumentPageListDto.getStartDate();
        LocalDateTime endDate = findDocumentPageListDto.getEndDate();
        Integer type = findDocumentPageListDto.getType();
        Long categoryId = findDocumentPageListDto.getCategoryId();
        List<Long> tagIds = findDocumentPageListDto.getTagIds();
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(findDocumentPageListDto.getPage());
        pageParam.setPageSize(findDocumentPageListDto.getPageSize());
        PageResult<FindDocumentPageListVO> result = TenantUtils.executeIgnore(() ->documentMapper.pageQuery(
                pageParam,
                title, startDate, endDate, type, categoryId, tagIds));
        List<FindDocumentPageListVO> records = getFindDocumentPageListVOS(result);

        return new PageResult<>(records, result.getTotal());
    }

    private @NonNull List<FindDocumentPageListVO> getFindDocumentPageListVOS(PageResult<FindDocumentPageListVO> result) {
        List<FindDocumentPageListVO> records = result.getList();
        records.forEach(item -> {
            Long cover = item.getCover();
            if (Objects.nonNull(cover)) {
                try {
                    FileDO file = fileService.getFile(cover);
                    item.setImageUrl(fileService.presignGetUrl(file.getPath(), 600));
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
            item.setIsTop(item.getWeight() > 0);
            // 设置 ACL 权限列表
            item.setPermissions(new ArrayList<>(aclDecisionEngine.userPermissions(ResourceType.DOCUMENT, item.getId())));
            // 标记是否为授权过来的内容
            item.setGranted(!aclDecisionEngine.isCreator(ResourceType.DOCUMENT, item.getId()));
        });
        return records;
    }

    @Override
    public FindDocumentDetailVO findDocumentDetail(Long documentId) {

        Document document = TenantUtils.executeIgnore(() -> documentMapper.selectDocumentById(documentId));

        if (Objects.isNull(document)) {
            log.warn("==> 查询的文档不存在，documentId: {}", documentId);
            throw exception(DOCUMENT_NOT_FOUND);
        }

        if (!knowledgeService.isPermissionByWikiDocumentId(documentId, Permission.READ,
                Objects.equals(document.getCreator(), String.valueOf(getLoginUserId())))) {
            throw exception(NO_READ_PERMISSION);
        }

        DocumentContent documentContent = TenantUtils.executeIgnore(() -> documentContentMapper.selectByDocumentId(documentId));
        Long cover = document.getCover();
        FindDocumentDetailVO detailVo = FindDocumentDetailVO.builder()
                .id(document.getId()).title(document.getTitle())
                .categoryId(document.getCategoryId())
                .cover(cover).isTop(document.getWeight() > 0)
                .content(UmoDocConverter.resolveContentFileUrls(
                        documentContent.getContent(), fileId -> fileService.presignGetUrl(fileId, 3600)))
                .chunkKeys(documentContent.getChunkKeys())
                .summary(document.getSummary()).build();
        if (Objects.nonNull(cover)) {
            detailVo.setImageUrl(fileService.presignGetUrl(cover, 600));
        }
        detailVo.setTagIds(document.getTagIds());
        return detailVo;
    }

    @Override
    public Document selectById(Long documentId) {
        return documentMapper.selectById(documentId);
    }

    @Override
    public Document selectDocumentById(Long documentId) {
        return documentMapper.selectDocumentById(documentId);
    }

    @Override
    public List<Document> selectDocumentByIds(List<Long> documentIds) {
        return documentMapper.selectDocumentByIds(documentIds);
    }

    @Override
    public DocumentContent selectByDocumentId(Long documentId) {
        return documentContentMapper.selectByDocumentId(documentId);
    }

    @Override
    public DictValue selectCategoryByDocumentId(Long documentId) {
        Document document = documentMapper.selectDocumentById(documentId);
        DictDataDO dictData = dictDataService.getDictData(document.getCategoryId());
        if (Objects.isNull(dictData)) {
            return DictValue.builder().build();
        }
        return DictValue.builder().id(dictData.getId()).remark(dictData.getRemark())
                .value(dictData.getLabel()).dictType(dictData.getDictType()).build();
    }

    @Override
    public List<DictValue> selectTagsByDocumentId(Long documentId) {
        Document document = documentMapper.selectDocumentById(documentId);
        List<DictDataDO> dictDataByIds = dictDataService.getDictDataByIds(document.getTagIds());
        List<DictValue> dictValues = new ArrayList<>();
        dictDataByIds.forEach(dictData -> dictValues.add(DictValue.builder().id(dictData.getId()).remark(dictData.getRemark())
                .value(dictData.getLabel()).dictType(dictData.getDictType()).build()));
        return dictValues;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDocument(UpdateDocumentDTO updateDocumentDto) {
        Long documentId = updateDocumentDto.getId();

        // VO 转 Article, 并更新
        Boolean isTop = updateDocumentDto.getIsTop();
        Document document = Document.builder()
                .id(documentId)
                .title(updateDocumentDto.getTitle())
                .cover(updateDocumentDto.getCover())
                .summary(updateDocumentDto.getSummary())
                .weight(isTop ? documentMapper.selectMaxWeight() + 1 : 0)
                .categoryId(updateDocumentDto.getCategoryId())
                .tagIds(updateDocumentDto.getTagIds())
                .build();
        int count = documentMapper.updateDocument(document);
        // 根据更新是否成功，来判断该文档是否存在
        if (count == 0) {
           log.warn("==> 更新文档时，该文档不存在, documentId: {}", documentId);
           throw exception(DOCUMENT_NOT_FOUND);
        }
        // 查询更新后的完整文档记录，确保 createTime/updateTime 等字段正确
        Document updatedDocument = documentMapper.selectById(documentId);
        loadingDocumentIndex(updatedDocument, documentId);

        // VO 转 ArticleContent，并更新
        String content = updateDocumentDto.getContent();
        if (Objects.nonNull(content)) {
            DocumentContent documentContent = DocumentContent.builder()
                    .documentId(documentId)
                    .content(content)
                    .build();
            documentContentMapper.updateByDocumentId(documentContent);
            eventPublisher.publishEvent(new WriteWikiDocEvent(this, documentId));
        }
    }

    @Override
    public void updateDocumentToConversion(ConversionUpdateDocument document, Boolean isLoadingIndex) {
        Long documentId = document.getId();
        Document wikiDocument = Document.builder()
                .id(documentId)
                .title(document.getTitle())
                .cover(document.getCover())
                .summary(document.getSummary())
                .fileId(document.getFileId())
                .categoryId(document.getCategoryId())
                .tagIds(document.getTagIds())
                .build();
        int count = documentMapper.updateById(wikiDocument);

        // 根据更新是否成功，来判断该文档是否存在
        if (count == 0) {
            log.warn("==> 更新文档信息时，该文档不存在, documentId: {}", documentId);
            throw exception(DOCUMENT_NOT_FOUND);
        }

        // VO 转 ArticleContent，并更新
        String content = document.getContent();
        if (Objects.nonNull(content)) {
            DocumentContent documentContent = DocumentContent.builder()
                    .documentId(documentId)
                    .content(content)
                    .build();
            documentContentMapper.updateByDocumentId(documentContent);
            eventPublisher.publishEvent(new WriteWikiDocEvent(this, documentId));
        }

        if (isLoadingIndex) {
            loadingDocumentIndex(wikiDocument, documentId);
        }
    }

    @Override
    public void clearDocumentChunkKeysByIds(List<Long> documentIds) {
        documentContentMapper.clearDocumentChunkKeysByIds(documentIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDocumentIsTop(Boolean isTop, Long id) {
        // 默认权重为 0
        int weight = 0;
        // 若设置为置顶
        if (isTop) {
            // 查询出表中最大的权重值, 最大权重值加一
            weight = documentMapper.selectMaxWeight() + 1;
        }

        // 更新该篇文档的权重值
        documentMapper.updateById(Document.builder()
                .id(id)
                .weight(weight)
                .build());

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTypeByIds(Integer type, List<Long> articleIds) {
        if (!articleIds.isEmpty()) {
            TypeStatus builder = TypeStatus.builder()
                    .type(type).ids(articleIds.toArray(new Long[0])).build();
            documentMapper.updateTypeByIds(builder);
        }
    }

    @Override
    public Document selectNextDocumentFilter(Long documentId, List<Long> permissionIds) {
        return documentMapper.selectNextDocumentFilter(documentId, permissionIds);
    }

    @Override
    public Document selectPreDocumentFilter(Long documentId, List<Long> permissionIds) {
        return documentMapper.selectPreDocumentFilter(documentId, permissionIds);
    }

    @Override
    public FindDocumentInfoDetailVO findDocumentInfoDetail(FindDocumentDetailDTO dto, Boolean openPermission) {
        Long documentId = dto.getDocumentId();

        Document documentDO = documentMapper.selectById(documentId);


        // 判断文档是否存在
        if (Objects.isNull(documentDO)) {
            log.warn("进行文档查询 ==> 该文档不存在, documentId: {}", documentId);
            throw exception(DOCUMENT_NOT_FOUND);
        }
        String loginUserId = String.valueOf(getLoginUserId());
        boolean isCreator = Objects.equals(documentDO.getCreator(), loginUserId);
        List<Long> permissionIds = new ArrayList<>();
        if (openPermission) {
            if (!knowledgeService.isPermissionByWikiDocumentId(documentId, Permission.READ, isCreator)) {
                throw exception(NO_READ_PERMISSION);
            }
            permissionIds = knowledgeService.selectWikiDocumentIdByPermission(Permission.READ);
        }

        // 查询正文
        DocumentContent documentContentDO = documentContentMapper.selectByDocumentId(documentId);
        String content = documentContentDO.getContent();

        // 计算 md 正文字数
        int totalWords = MarkdownStatsUtil.calculateWordCount(content);
        // DO 转 VO
        Integer type = documentDO.getType();
        FindDocumentInfoDetailVO vo = FindDocumentInfoDetailVO.builder()
                .title(documentDO.getTitle())
                .summary(documentDO.getSummary()).content(UmoDocConverter.resolveContentFileUrls(content, fileId -> fileService.presignGetUrl(fileId, 3600)))
                .readNum(documentDO.getReadNum())
                .totalWords(totalWords).type(type).fileId(documentDO.getFileId())
                .readTime(MarkdownStatsUtil.calculateReadingTime(totalWords))
                .build();

        vo.setPermissions(new ArrayList<>(
                aclDecisionEngine.userPermissions(ResourceType.DOCUMENT, documentId)));
        if (type == 2) {
            WikiCatalogIdSet wikiCatalogIdSet = knowledgeService.selectWikiCatalogIdSetByDocumentId(documentId);
            vo.setWikiId(wikiCatalogIdSet.getWikiId());
        }

        vo.setCreateTime(documentDO.getCreateTime());
        vo.setUpdateTime(documentDO.getUpdateTime());

        // 查询所属分类
        DictValue dictValue = this.selectCategoryByDocumentId(documentId);
        if (Objects.nonNull(dictValue)) {
            vo.setCategoryId(dictValue.getId());
            vo.setCategoryName(dictValue.getValue());
        }

        // 查询标签
        List<DictValue> dictValues = this.selectTagsByDocumentId(documentId);
        List<Tag> tags = new ArrayList<>();
        dictValues.forEach(value -> tags.add(Tag.builder()
                .id(value.getId()).name(value.getValue()).build()));
        vo.setTags(tags);

        // 上一篇
        Document preDocumentDO = this.selectPreDocumentFilter(documentId, permissionIds);
        if (Objects.nonNull(preDocumentDO)) {
            FindPreNextArticleVO preArticleVO = FindPreNextArticleVO.builder()
                    .documentId(preDocumentDO.getId())
                    .articleTitle(preDocumentDO.getTitle())
                    .build();
            vo.setPreArticle(preArticleVO);
        }

        // 下一篇
        Document nextDocumentDO = this.selectNextDocumentFilter(documentId, permissionIds);
        if (Objects.nonNull(nextDocumentDO)) {
            FindPreNextArticleVO nextArticleVO = FindPreNextArticleVO.builder()
                    .documentId(nextDocumentDO.getId())
                    .articleTitle(nextDocumentDO.getTitle())
                    .build();
            vo.setNextArticle(nextArticleVO);
        }

        // 发布文档阅读事件
        eventPublisher.publishEvent(new ReadKnowledgeEvent(this, null, documentId));

        return vo;
    }

    @Override
    public List<DocumentContent> selectDocumentContentByDocumentIds(List<Long> documentIds) {
        return documentContentMapper.selectByDocumentIds(documentIds);
    }

    @Override
    public PageResult<HotDocumentVO> getHotDocumentPage(BasePageListDTO dto) {
        List<Long> ids = knowledgeService.selectWikiDocumentIdByPermission(Permission.READ);
        if (Objects.isNull(ids)) {
            return new PageResult<>(List.of(), 0L);
        }
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(dto.getPage());
        pageParam.setPageSize(dto.getPageSize());
        PageResult<Document> result = documentMapper.selectDocumentIdsPage(pageParam, ids);
        List<Document> records = result.getList();
        List<HotDocumentVO> vos = new ArrayList<>();
        records.forEach(document -> {
            Long documentId = document.getId();
            HotDocumentVO build = HotDocumentVO.builder().id(documentId).title(document.getTitle())
                    .summary(document.getSummary()).createTime(document.getCreateTime())
                    .imageUrl(fileService.presignGetUrl(document.getCover(), 600)).build();
            // 查询所属分类
            DictDataDO dictValue = dictDataService.getDictData(document.getCategoryId());
            if (Objects.nonNull(dictValue)) {
                build.setCategoryName(dictValue.getLabel());
            }
            List<Long> tagIds = document.getTagIds();
            if (Objects.nonNull(tagIds)) {
                List<String> tagsName = new ArrayList<>();
                List<DictDataDO> dictDataByIds = dictDataService.getDictDataByIds(tagIds);
                dictDataByIds.forEach(dictDataDO -> {
                    tagsName.add(dictDataDO.getLabel());
                });
                build.setTagNames(tagsName);
            }
            AdminUserDO user = adminUserService.getUser(Long.valueOf(document.getCreator()));
            build.setUserInfo(BaseUserInfo.builder().id(user.getId()).name(user.getUsername()).build());
            DocumentContent documentContent = this.selectByDocumentId(documentId);
            int totalWords = MarkdownStatsUtil.calculateWordCount(documentContent.getContent());
            build.setWordCount((long) totalWords);
            vos.add(build);
        });
        return new PageResult<>(vos, result.getTotal());
    }

    @Override
    public List<DocumentDetailTransformer> findDocumentDetailByIds(List<Long> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            return List.of();
        }

        List<Long> publishDocIds = knowledgeService.selectWikiDocumentIdByPermission(Permission.READ);
        if (publishDocIds == null || publishDocIds.isEmpty()) {
            return List.of();
        }

        Set<Long> publishIdSet = new HashSet<>(publishDocIds);

        List<Long> validIds = documentIds.stream()
                .filter(publishIdSet::contains)
                .distinct()
                .toList();

        if (validIds.isEmpty()) {
            return List.of();
        }
        List<Document> documents = TenantUtils.executeIgnore(() -> documentMapper.selectDocumentByIds(validIds));
        List<DocumentDetailTransformer> vos = new ArrayList<>();
        documents.forEach(document -> {
            DocumentDetailTransformer build = DocumentDetailTransformer.builder()
                    .documentId(document.getId()).title(document.getTitle())
                    .summary(document.getSummary()).build();
            DictDataDO dictValue = dictDataService.getDictData(document.getCategoryId());
            if (Objects.nonNull(dictValue)) {
                build.setCategoryName(dictValue.getLabel());
            }
            List<Long> tagIds = document.getTagIds();
            if (Objects.nonNull(tagIds)) {
                List<String> tagsName = new ArrayList<>();
                List<DictDataDO> dictDataByIds = dictDataService.getDictDataByIds(tagIds);
                dictDataByIds.forEach(dictDataDO -> {
                    tagsName.add(dictDataDO.getLabel());
                });
                build.setTagNames(tagsName);
            }
            vos.add(build);
        });
        return vos;
    }

    @Override
    public String generateSummaryByModel(Long id) {
        FindDocumentDetailVO articleDetail = this.findDocumentDetail(id);
        List<org.springframework.ai.document.Document> documents = MarkdownSplitter.splitByHeadersWithHierarchy(
                articleDetail.getContent(), new HashMap<>());
        return aiModelToolService.generateSummaryByModel(documents);
    }

    @Override
    public String generateSummaryByContent(String content) {
        List<org.springframework.ai.document.Document> documents = MarkdownSplitter.splitByHeadersWithHierarchy(
                content, new HashMap<>());
        return aiModelToolService.generateSummaryByModel(documents);
    }

    @Override
    public String generateTitleBySummary(String summary) {
        return aiModelToolService.generateTitleByModel(summary);
    }

    @Override
    public String formattingDocument(String content) {
        return aiModelToolService.formattingDocument(content);
    }

    @Override
    public DocumentTaxonomy generateTaxonomy(String summary) {
        List<DictDataDO> categories = dictDataService.getDictDataListByDictType(CoreDictType.DOCUMENT_CATEGORY.getValue());
        List<DictDataDO> tags = dictDataService.getDictDataListByDictType(CoreDictType.DOCUMENT_TAG.getValue());
        List<TaxonomyValue> categoriesList = categories.stream().map(value -> TaxonomyValue.builder().id(value.getId())
                .value(value.getLabel()).remark(value.getRemark()).build()).toList();
        List<TaxonomyValue> tagsList = tags.stream().map(value -> TaxonomyValue.builder().id(value.getId())
                .value(value.getLabel()).remark(value.getRemark()).build()).toList();
        DocumentCitationTaxonomy build = DocumentCitationTaxonomy.builder()
                .categories(categoriesList).tags(tagsList).build();
        return aiModelToolService.generateTaxonomy(build, DocumentTaxonomy.class, summary);
    }

    @Override
    public PageResult<UsageRecordDocumentVO> getUsageRecordDocumentPage(BasePageListDTO dto) {
        List<UsageRecord> usageRecords = usageRecordService.selectUserListByType(UsageRecordType.DOCUMENT);
        if (Objects.isNull(usageRecords) || usageRecords.isEmpty()) {
            return new PageResult<>(List.of(), 0L);
        }
        Map<Long, LocalDateTime> usageRecordsHash = usageRecords.stream()
                .collect(Collectors.toMap(
                        UsageRecord::getObjectId,
                        UsageRecord::getUpdateTime,
                        (oldTime, newTime) -> newTime.isAfter(oldTime) ? newTime : oldTime
                ));
        List<Long> ids = usageRecordsHash.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(Map.Entry::getKey)
                .toList();

        List<Long> filteredDocumentIds = knowledgeService.filterWikiDocumentIdsByPermission(ids, Permission.READ);
        if (filteredDocumentIds.isEmpty()) {
            return new PageResult<>(List.of(), 0L);
        }
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(dto.getPage());
        pageParam.setPageSize(dto.getPageSize());
        PageResult<Document> pageResult = documentMapper.selectUsageRecordDocumentPage(
                pageParam, filteredDocumentIds);
        List<UsageRecordDocumentVO> vos = pageResult.getList().stream()
                .map(document -> UsageRecordDocumentVO.builder()
                        .id(document.getId())
                        .usageTime(usageRecordsHash.get(document.getId()))
                        .title(document.getTitle())
                        .summary(document.getSummary())
                        .imageUrl(fileService.presignGetUrl(document.getCover(), 600))
                        .build())
                .toList();
        return new PageResult<>(vos, pageResult.getTotal());
    }

    @Override
    public Boolean saveDocumentInfo(SaveDocumentInfoDTO dto) {
        Long documentId = dto.getId();
        Boolean isTop = dto.getIsTop();

        // 新增：id 为空时创建新文档
        if (Objects.isNull(documentId)) {
            Document document = Document.builder()
                    .title(dto.getTitle())
                    .cover(dto.getCover())
                    .summary(dto.getSummary())
                    .weight(isTop ? documentMapper.selectMaxWeight() + 1 : 0)
                    .categoryId(dto.getCategoryId())
                    .tagIds(dto.getTagIds())
                    .build();
            documentMapper.insert(document);
            DocumentContent documentContent = DocumentContent.builder()
                    .documentId(document.getId())
                    .content("")
                    .build();
            return documentContentMapper.insert(documentContent) > 0;
        }

        // 更新
        Document document = Document.builder()
                .id(documentId)
                .title(dto.getTitle())
                .cover(dto.getCover())
                .summary(dto.getSummary())
                .weight(isTop ? documentMapper.selectMaxWeight() + 1 : 0)
                .categoryId(dto.getCategoryId())
                .tagIds(dto.getTagIds())
                .build();

        boolean isSuccess = documentMapper.updateById(document) > 0;
        if (!isSuccess) {
            log.warn("==> 保存文档信息时，该文档不存在, documentId: {}", documentId);
            throw exception(DOCUMENT_NOT_FOUND);
        }

        Document updatedDocument = documentMapper.selectById(documentId);
        loadingDocumentIndex(updatedDocument, documentId);
        return true;
    }

    @Override
    public Boolean saveDocumentContent(SaveDocumentContentDTO dto) {
        Long documentId = dto.getId();
        String content = dto.getContent();
        if (Objects.nonNull(content)) {
            DocumentContent documentContent = DocumentContent.builder()
                    .documentId(documentId)
                    .content(content)
                    .build();
            documentContentMapper.updateByDocumentId(documentContent);
            // 解析正文中引用的资源文件 ID（图片/视频/音频/附件），更新文档 resource_ids，
            // 供文件访问权限判定（DocumentFileAccessChecker 据此将资源文件关联到文档）
            List<Long> resourceIds = UmoDocConverter.extractResourceIds(content);
            Document document = Document.builder()
                    .id(documentId)
                    .resourceIds(resourceIds.isEmpty() ? null : resourceIds)
                    .build();
            documentMapper.updateById(document);
            eventPublisher.publishEvent(new WriteWikiDocEvent(this, documentId));
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void documentConversion(List<Long> ids) {
        for (Long id : ids) {
            FileDO file = fileService.getFile(id);
            String fileName = file.getName();
            Document document = Document.builder()
                    .title(fileName).build();
            documentMapper.insert(document);
            Long documentId = document.getId();
            DocumentContent documentContent = DocumentContent.builder()
                    .documentId(documentId)
                    .content("")
                    .build();
            documentContentMapper.insert(documentContent);
            try {
                byte[] fileContent = fileService.getFileContent(file.getConfigId(), file.getPath());
                KBConversionEvent kbEvent = new KBConversionEvent(this);
                kbEvent.setTenantId(TenantContextHolder.getTenantId());
                kbEvent.setUserId(SecurityFrameworkUtils.getLoginUserId());
                DocumentTask task = DocumentTask.builder()
                        .event(kbEvent).fileId(id)
                        .inputStream(new ByteArrayInputStream(fileContent))
                        .pageNumber(1).priority(1).documentId(documentId)
                        .fileName(fileName).retryCount(new AtomicInteger(0))
                        .createTime(LocalDateTime.now())
                        .build();
                documentConversionService.convertDocumentFileAsync(task);

                loadingDocumentIndex(document, documentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void reloadDocumentIndex() {
        smartTaskScheduler.submit(() -> {
            try {
                // 确保 ES 索引存在
                searchRepository.ensureIndexExists(IndexNameEnum.DOCUMENT);

                // 获取数据库中所有未删除的文档 ID
                List<Long> dbIds = documentMapper.selectAllIds();
                Set<Long> dbIdSet = new HashSet<>(dbIds);
                log.info("DB 文档数量: {}", dbIdSet.size());

                // 获取 ES 所有文档 ID
                List<String> esIdList = searchRepository.fetchAllIds(IndexNameEnum.DOCUMENT);
                log.info("ES 文档数量: {}", esIdList.size());

                // 计算 ES 中有但 DB 中没有的 ID（需删除）
                List<String> idsToDelete = esIdList.stream()
                        .filter(esId -> {
                            try {
                                long id = Long.parseLong(esId);
                                return !dbIdSet.contains(id);
                            } catch (NumberFormatException e) {
                                return true;
                            }
                        })
                        .collect(Collectors.toList());

                if (!idsToDelete.isEmpty()) {
                    searchRepository.bulkDelete(IndexNameEnum.DOCUMENT, idsToDelete);
                    log.info("从 ES 删除不存在的文档: {} 条", idsToDelete.size());
                }

                // 分页从 DB 查询文档，逐批写入 ES，避免一次性加载全部到内存
                int pageSize = 2000;
                int totalWritten = 0;
                for (int offset = 0; offset < dbIds.size(); offset += pageSize) {
                    int end = Math.min(offset + pageSize, dbIds.size());
                    List<Long> pageIds = dbIds.subList(offset, end);
                    List<Document> pageDocuments = documentMapper.selectDocumentByIds(pageIds);

                    List<DocumentSearch> batch = new ArrayList<>(pageDocuments.size());
                    for (Document doc : pageDocuments) {
                        DocumentSearch build = DocumentSearch.builder()
                                .title(doc.getTitle())
                                .summary(doc.getSummary()).build();
                        build.setId(doc.getId());
                        build.setCreateTime(doc.getCreateTime());
                        build.setUpdateTime(doc.getUpdateTime());

                        DictDataDO dictValue = dictDataService.getDictData(doc.getCategoryId());
                        if (Objects.nonNull(dictValue)) {
                            build.setCategory(dictValue.getLabel());
                        }
                        List<Long> tagIds = doc.getTagIds();
                        if (Objects.nonNull(tagIds)) {
                            List<String> tagsName = new ArrayList<>();
                            List<DictDataDO> dictDataByIds = dictDataService.getDictDataByIds(tagIds);
                            dictDataByIds.forEach(dictTag -> {
                                tagsName.add(dictTag.getLabel());
                            });
                            build.setTags(tagsName);
                        }

                        batch.add(build);
                    }

                    if (!batch.isEmpty()) {
                        searchRepository.bulkSave(IndexNameEnum.DOCUMENT, batch,
                                doc -> String.valueOf(doc.getId()));
                        totalWritten += batch.size();
                        log.info("文档索引进度: {}/{}", totalWritten, dbIds.size());
                    }
                }

                log.info("文档索引重载完成，共写入 {} 条", totalWritten);
            } catch (IOException e) {
                log.error("重载文档索引失败", e);
            }
        }, TaskPriority.BACKGROUND, "index");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reloadDocumentEmbedding(Long id) {
        smartTaskScheduler.submit(() -> {
            Boolean isSuccess = milvusStoreService.reloadDocumentByWiki(id);
            log.info("文档 “{}” 重新向量化状态：{}", id, isSuccess);
        }, TaskPriority.NORMAL, "reload-document-embbeding");
    }

    private void loadingDocumentIndex(Document document, Long documentId) {
        smartTaskScheduler.submit(() -> {
            try {
                DocumentSearch build = DocumentSearch.builder()
                        .title(document.getTitle())
                        .summary(document.getSummary()).build();
                DictDataDO dictValue = dictDataService.getDictData(document.getCategoryId());
                if (Objects.nonNull(dictValue)) {
                    build.setCategory(dictValue.getLabel());
                }
                List<Long> tagIds = document.getTagIds();
                if (Objects.nonNull(tagIds)) {
                    List<String> tagsName = new ArrayList<>();
                    List<DictDataDO> dictDataByIds = dictDataService.getDictDataByIds(tagIds);
                    dictDataByIds.forEach(dictTag -> {
                        tagsName.add(dictTag.getLabel());
                    });
                    build.setTags(tagsName);
                }
                build.setId(documentId);
                build.setCreateTime(document.getCreateTime());
                build.setUpdateTime(document.getUpdateTime());
                searchRepository.save(IndexNameEnum.DOCUMENT,
                        String.valueOf(documentId), build);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }, TaskPriority.NORMAL, "index");
    }
}
