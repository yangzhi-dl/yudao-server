package cn.iocoder.yudao.module.ai.knowledge.document.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.common.model.dto.BasePageListDTO;
import cn.iocoder.yudao.module.ai.common.model.entity.DictValue;
import cn.iocoder.yudao.module.ai.knowledge.document.dal.dataobject.Document;
import cn.iocoder.yudao.module.ai.knowledge.document.model.dto.*;
import cn.iocoder.yudao.module.ai.knowledge.document.model.vo.*;
import cn.iocoder.yudao.module.ai.knowledge.document.dal.dataobject.DocumentContent;
import cn.iocoder.yudao.module.ai.knowledge.document.model.entity.ConversionUpdateDocument;
import cn.iocoder.yudao.module.ai.knowledge.document.model.entity.DocumentTaxonomy;

import java.util.List;

public interface DocumentService {
    /**
     * 添加文档
     */
    void createDocument(PublishDocumentDTO publishDocumentDto);

    /**
     * 添加文档，并返回新文档 ID（供数据治理归档等需要回填 ID 的场景使用）
     */
    Long createDocumentReturnId(PublishDocumentDTO publishDocumentDto);

    /**
     * 删除文档
     */
    void deleteDocument(List<Long> ids);

    void updateChunkKeys(Long id, List<String> chunkKeys);

    /**
     * 查询文档分页数据
     */
    PageResult<FindDocumentPageListVO> findDocumentPageList(FindDocumentPageListDTO findDocumentPageListDto);

    /**
     * 查询文档详情
     */
    FindDocumentDetailVO findDocumentDetail(Long documentId);

    Document selectById(Long documentId);

    /**
     * 按 ID 查询文档
     */
    Document selectDocumentById(Long documentId);

    List<Document> selectDocumentByIds(List<Long> documentIds);

    DocumentContent selectByDocumentId(Long documentId);

    List<DictValue> selectTagsByDocumentId(Long documentId);

    DictValue selectCategoryByDocumentId(Long documentId);

    /**
     * 更新文档
     */
    void updateDocument(UpdateDocumentDTO updateDocumentDto);

    void updateDocumentToConversion(ConversionUpdateDocument document, Boolean isLoadingIndex);

    void clearDocumentChunkKeysByIds(List<Long> documentIds);

    /**
     * 更新文档是否置顶
     */
    void updateDocumentIsTop(Boolean isTop, Long id);

    void updateTypeByIds(Integer type, List<Long> documentIds);

    Document selectNextDocumentFilter(Long documentId, List<Long> permissionIds);

    Document selectPreDocumentFilter(Long documentId, List<Long> permissionIds);

    default FindDocumentInfoDetailVO findDocumentInfoDetailByPermission(FindDocumentDetailDTO dto) {
        return this.findDocumentInfoDetail(dto, true);
    }

    default FindDocumentInfoDetailVO findDocumentInfoDetail(FindDocumentDetailDTO dto) {
        return this.findDocumentInfoDetail(dto, false);
    }

    FindDocumentInfoDetailVO findDocumentInfoDetail(FindDocumentDetailDTO dto, Boolean openPermission);

    List<DocumentContent> selectDocumentContentByDocumentIds(List<Long> documentIds);

    PageResult<HotDocumentVO> getHotDocumentPage(BasePageListDTO dto);

    List<DocumentDetailTransformer> findDocumentDetailByIds(List<Long> documentIds);

    String generateSummaryByModel(Long id);

    String generateSummaryByContent(String content);

    String generateTitleBySummary(String summary);

    String formattingDocument(String content);

    DocumentTaxonomy generateTaxonomy(String summary);

    PageResult<UsageRecordDocumentVO> getUsageRecordDocumentPage(BasePageListDTO dto);

    Boolean saveDocumentInfo(SaveDocumentInfoDTO dto);

    Boolean saveDocumentContent(SaveDocumentContentDTO dto);

    void documentConversion(List<Long> ids);

    void reloadDocumentIndex();

    void reloadDocumentEmbedding(Long id);
}
