package cn.iocoder.yudao.module.ai.data.governance.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.ai.common.utils.MarkdownStatsUtil;
import cn.iocoder.yudao.module.ai.core.chat.service.AiModelToolService;
import cn.iocoder.yudao.module.ai.data.config.AiDataProperties;
import cn.iocoder.yudao.module.ai.data.governance.dal.dataobject.DataAsset;
import cn.iocoder.yudao.module.ai.data.governance.dal.dataobject.DataAssetContent;
import cn.iocoder.yudao.module.ai.data.governance.dal.dataobject.GovernanceRecord;
import cn.iocoder.yudao.module.ai.data.governance.dal.mysql.DataAssetContentMapper;
import cn.iocoder.yudao.module.ai.data.governance.dal.mysql.DataAssetMapper;
import cn.iocoder.yudao.module.ai.data.governance.dal.mysql.GovernanceRecordMapper;
import cn.iocoder.yudao.module.ai.data.governance.enums.AssetStatus;
import cn.iocoder.yudao.module.ai.data.governance.enums.GovernanceAction;
import cn.iocoder.yudao.module.ai.data.governance.event.AssetConvertedEvent;
import cn.iocoder.yudao.module.ai.data.governance.service.DataAssetService;
import cn.iocoder.yudao.module.ai.knowledge.common.utils.UmoDocConverter;
import cn.iocoder.yudao.module.ai.knowledge.document.model.dto.PublishDocumentDTO;
import cn.iocoder.yudao.module.ai.knowledge.document.service.DocumentService;
import cn.iocoder.yudao.module.ai.knowledge.wiki.dal.dataobject.WikiCatalog;
import cn.iocoder.yudao.module.ai.knowledge.wiki.model.dto.ArchiveWikiDocumentDTO;
import cn.iocoder.yudao.module.ai.knowledge.wiki.service.WikiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.data.constans.DataErrorCodeConstants.ASSET_ALREADY_ARCHIVED;
import static cn.iocoder.yudao.module.ai.data.constans.DataErrorCodeConstants.ASSET_ALREADY_DISCARDED;
import static cn.iocoder.yudao.module.ai.data.constans.DataErrorCodeConstants.ASSET_NOT_FOUND;
import static cn.iocoder.yudao.module.ai.data.constans.DataErrorCodeConstants.ASSET_QUALITY_BELOW_THRESHOLD;

/**
 * 数据资产与治理 Service 实现。
 */
@Slf4j
@Service
public class DataAssetServiceImpl implements DataAssetService {

    private final DataAssetMapper dataAssetMapper;
    private final DataAssetContentMapper dataAssetContentMapper;
    private final GovernanceRecordMapper governanceRecordMapper;
    private final AiDataProperties properties;
    private final AiModelToolService aiModelToolService;
    private final DocumentService documentService;
    private final WikiService wikiService;
    private final ApplicationEventPublisher eventPublisher;

    public DataAssetServiceImpl(DataAssetMapper dataAssetMapper,
                                DataAssetContentMapper dataAssetContentMapper,
                                GovernanceRecordMapper governanceRecordMapper,
                                AiDataProperties properties,
                                AiModelToolService aiModelToolService,
                                DocumentService documentService,
                                WikiService wikiService,
                                ApplicationEventPublisher eventPublisher) {
        this.dataAssetMapper = dataAssetMapper;
        this.dataAssetContentMapper = dataAssetContentMapper;
        this.governanceRecordMapper = governanceRecordMapper;
        this.properties = properties;
        this.aiModelToolService = aiModelToolService;
        this.documentService = documentService;
        this.wikiService = wikiService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAsset(Long sourceConfigId, Long collectTaskId, Long collectResultId,
                            Long warehouseFileId, String title, String markdown) {
        DataAsset asset = DataAsset.builder()
                .sourceConfigId(sourceConfigId)
                .collectTaskId(collectTaskId)
                .collectResultId(collectResultId)
                .fileId(warehouseFileId)
                .title(title)
                .qualityScore(score(markdown))
                .status(AssetStatus.PENDING_GOVERNANCE.getValue())
                .build();
        dataAssetMapper.insert(asset);

        DataAssetContent content = DataAssetContent.builder()
                .assetId(asset.getId())
                .content(markdown)
                .build();
        dataAssetContentMapper.insert(content);

        saveRecord(asset.getId(), GovernanceAction.CREATE, null, markdown, null, null);
        eventPublisher.publishEvent(new AssetConvertedEvent(this, asset.getId()));
        return asset.getId();
    }

    @Override
    public DataAsset getAsset(Long id) {
        return validateAsset(id);
    }

    @Override
    public PageResult<DataAsset> getAssetPage(PageParam pageParam, Integer status, String title) {
        return dataAssetMapper.selectPage(pageParam, status, title);
    }

    @Override
    public String getAssetContent(Long assetId) {
        validateAsset(assetId);
        DataAssetContent content = dataAssetContentMapper.selectByAssetId(assetId);
        return content == null ? "" : content.getContent();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editAsset(Long assetId, String title, String summary, String content) {
        DataAsset asset = validateAsset(assetId);
        String before = getAssetContent(assetId);

        if (title != null && !title.isBlank()) {
            asset.setTitle(title);
        }
        if (summary != null) {
            asset.setSummary(summary);
        }
        asset.setStatus(AssetStatus.GOVERNED.getValue());
        dataAssetMapper.updateById(asset);

        saveOrUpdateContent(assetId, content, null);
        saveRecord(assetId, GovernanceAction.EDIT, before, content, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void aiCleanAsset(Long assetId, String prompt) {
        DataAsset asset = validateAsset(assetId);
        String before = getAssetContent(assetId);
        String effectivePrompt = (prompt == null || prompt.isBlank())
                ? properties.getDefaultCleanPrompt() : prompt;

        String cleaned = aiModelToolService.optimizeMarkdownContent(before, effectivePrompt);

        asset.setStatus(AssetStatus.GOVERNED.getValue());
        dataAssetMapper.updateById(asset);

        saveOrUpdateContent(assetId, cleaned, effectivePrompt);
        saveRecord(assetId, GovernanceAction.AI_CLEAN, before, cleaned, effectivePrompt, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void discardAsset(Long assetId) {
        DataAsset asset = validateAsset(assetId);
        if (Objects.equals(asset.getStatus(), AssetStatus.ARCHIVED.getValue())) {
            throw exception(ASSET_ALREADY_ARCHIVED);
        }
        asset.setStatus(AssetStatus.DISCARDED.getValue());
        dataAssetMapper.updateById(asset);
        saveRecord(assetId, GovernanceAction.DISCARD, getAssetContent(assetId), null, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveAsset(Long assetId, Long wikiId, Long parentId, Long categoryId,
                             List<Long> tagIds, String title, String summary, Long cover, Boolean isTop) {
        DataAsset asset = validateAsset(assetId);
        if (Objects.equals(asset.getStatus(), AssetStatus.ARCHIVED.getValue())) {
            throw exception(ASSET_ALREADY_ARCHIVED);
        }
        if (Objects.equals(asset.getStatus(), AssetStatus.DISCARDED.getValue())) {
            throw exception(ASSET_ALREADY_DISCARDED);
        }

        // 归档门槛：已治理，或质量评分达到阈值
        boolean governed = Objects.equals(asset.getStatus(), AssetStatus.GOVERNED.getValue());
        boolean qualityOk = asset.getQualityScore() != null
                && asset.getQualityScore() >= properties.getQualityThreshold();
        if (!governed && !qualityOk) {
            throw exception(ASSET_QUALITY_BELOW_THRESHOLD);
        }

        String content = getAssetContent(assetId);
        // 资产内容为 Markdown，归档为知识库文档时需转换为 UMO Doc 格式 HTML 存储
        String umoContent = UmoDocConverter.convert(content);
        String documentTitle = (title != null && !title.isBlank()) ? title : asset.getTitle();
        if (documentTitle == null || documentTitle.isBlank()) {
            documentTitle = "未命名资产";
        }

        PublishDocumentDTO publishDocumentDTO = PublishDocumentDTO.builder()
                .title(documentTitle)
                .content(umoContent)
                .summary(summary != null ? summary : asset.getSummary())
                .cover(cover != null ? cover : asset.getCover())
                .categoryId(categoryId != null ? categoryId : asset.getCategoryId())
                .tagIds(tagIds != null ? tagIds : asset.getTagIds())
                .isTop(isTop != null ? isTop : false)
                .build();

        Long documentId = documentService.createDocumentReturnId(publishDocumentDTO);

        wikiService.archiveDocuments(ArchiveWikiDocumentDTO.builder()
                .wikiId(wikiId)
                .parentId(parentId)
                .documentIds(List.of(documentId))
                .build());

        WikiCatalog catalog = wikiService.findWikiCatalogByDocumentId(documentId);
        asset.setStatus(AssetStatus.ARCHIVED.getValue());
        asset.setArchivedDocumentId(documentId);
        asset.setArchivedWikiId(wikiId);
        asset.setArchivedCatalogId(catalog != null ? catalog.getId() : null);
        dataAssetMapper.updateById(asset);

        saveRecord(assetId, GovernanceAction.ARCHIVE, content, null, null, null);
    }

    @Override
    public List<GovernanceRecord> getGovernanceRecords(Long assetId) {
        validateAsset(assetId);
        return governanceRecordMapper.selectByAssetId(assetId);
    }

    private DataAsset validateAsset(Long id) {
        DataAsset asset = dataAssetMapper.selectById(id);
        if (asset == null) {
            throw exception(ASSET_NOT_FOUND);
        }
        return asset;
    }

    private void saveOrUpdateContent(Long assetId, String content, String prompt) {
        DataAssetContent existing = dataAssetContentMapper.selectByAssetId(assetId);
        if (existing == null) {
            dataAssetContentMapper.insert(DataAssetContent.builder()
                    .assetId(assetId)
                    .content(content)
                    .prompt(prompt)
                    .build());
        } else {
            existing.setContent(content);
            if (prompt != null) {
                existing.setPrompt(prompt);
            }
            dataAssetContentMapper.updateByAssetId(existing);
        }
    }

    private void saveRecord(Long assetId, GovernanceAction action, String contentBefore,
                            String contentAfter, String prompt, String remark) {
        governanceRecordMapper.insert(GovernanceRecord.builder()
                .assetId(assetId)
                .action(action.getValue())
                .contentBefore(contentBefore)
                .contentAfter(contentAfter)
                .prompt(prompt)
                .operatorId(SecurityFrameworkUtils.getLoginUserId())
                .remark(remark)
                .build());
    }

    /**
     * 简单质量评分：结合字数与 Markdown 结构特征，输出 0-100。
     */
    private int score(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return 0;
        }
        int wordCount = MarkdownStatsUtil.calculateWordCount(markdown);
        int score = 0;
        if (wordCount >= 200) {
            score += 40;
        } else if (wordCount >= 50) {
            score += 25;
        } else {
            score += 10;
        }
        if (markdown.contains("#")) {
            score += 20;
        }
        if (markdown.contains("```")) {
            score += 10;
        }
        if (markdown.contains("|")) {
            score += 10;
        }
        if (markdown.contains("](")) {
            score += 10;
        }
        if (markdown.contains("![")) {
            score += 10;
        }
        return Math.min(score, 100);
    }

}
