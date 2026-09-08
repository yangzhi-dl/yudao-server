package cn.iocoder.yudao.module.ai.knowledge.document.dal.mysql;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.common.model.entity.DeletedStatus;
import cn.iocoder.yudao.module.ai.common.model.entity.TypeStatus;
import cn.iocoder.yudao.module.ai.knowledge.document.dal.dataobject.Document;
import cn.iocoder.yudao.module.ai.knowledge.document.enums.DocumentTypeEnum;
import cn.iocoder.yudao.module.ai.knowledge.document.model.vo.FindDocumentPageListVO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
public interface DocumentMapper extends BaseMapperX<Document> {

    // ========== 基础 CRUD（XML - JSON 类型处理器） ==========

    /** 更新文档（需要 JacksonTypeHandler） */
    int updateDocument(Document document);

    // ========== 复杂查询（XML） ==========

    /** 分页查询 */
    default PageResult<FindDocumentPageListVO> pageQuery(PageParam pageParam, String title,
                                           LocalDateTime startDate, LocalDateTime endDate,
                                           Integer type, Long categoryId,
                                           List<Long> tagIds) {
        LambdaQueryWrapperX<Document> wrapper = new LambdaQueryWrapperX<Document>()
                .eq(Document::getDeleted, 0)
                .likeIfPresent(Document::getTitle, title)
                .eqIfPresent(Document::getType, type)
                .eqIfPresent(Document::getCategoryId, categoryId)
                .geIfPresent(Document::getCreateTime, startDate)
                .leIfPresent(Document::getCreateTime, endDate);

        if (CollUtil.isNotEmpty(tagIds)) {
            wrapper.and(w -> {
                for (Long tagId : tagIds) {
                    w.or().apply("JSON_CONTAINS(tag_ids, CONCAT('', {0}, ''))", tagId);
                }
            });
        }

        PageResult<Document> result = selectPage(pageParam, wrapper);

        List<FindDocumentPageListVO> voList = result.getList().stream()
                .map(doc -> FindDocumentPageListVO.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .cover(doc.getCover())
                .summary(doc.getSummary())
                .type(doc.getType())
                .categoryId(doc.getCategoryId())
                .tagIds(doc.getTagIds())
                .weight(doc.getWeight())
                .createTime(doc.getCreateTime())
                .updateTime(doc.getUpdateTime())
                .build()).toList();

        return new PageResult<>(voList, result.getTotal());
    }

    /** 查询未归档的文档（type=NORMAL），支持标题搜索 + 分类 + 标签过滤 + 分页 */
    default PageResult<Document> selectUnarchivedDocuments(PageParam pageParam, String title,
                                                           Long categoryId, List<Long> tagIds) {
        LambdaQueryWrapperX<Document> wrapper = new LambdaQueryWrapperX<Document>()
                .eq(Document::getDeleted, 0)
                .eq(Document::getType, DocumentTypeEnum.NORMAL.getValue())
                .likeIfPresent(Document::getTitle, title)
                .eqIfPresent(Document::getCategoryId, categoryId);

        if (CollUtil.isNotEmpty(tagIds)) {
            wrapper.and(w -> {
                for (Long tagId : tagIds) {
                    w.or().apply("JSON_CONTAINS(tag_ids, CONCAT('', {0}, ''))", tagId);
                }
            });
        }

        wrapper.orderByDesc(Document::getCreateTime);
        return selectPage(pageParam, wrapper);
    }

    /** 查询下一篇文档（权限过滤） */
    Document selectNextDocumentFilter(Long documentId, List<Long> permissionIds);

    /** 查询上一篇文档（权限过滤） */
    Document selectPreDocumentFilter(Long documentId, List<Long> permissionIds);

    /** 分页查询文档 ID 列表 */
    default PageResult<Document> selectDocumentIdsPage(PageParam pageParam, List<Long> ids) {
        LambdaQueryWrapperX<Document> wrapper = new LambdaQueryWrapperX<Document>()
                .eq(Document::getDeleted, 0)
                .inIfPresent(Document::getId, ids)
                .orderByDesc(Document::getReadNum);
        return selectPage(pageParam, wrapper);
    }

    /** 分页查询使用记录文档 */
    default PageResult<Document> selectUsageRecordDocumentPage(PageParam pageParam, List<Long> ids) {
        LambdaQueryWrapperX<Document> wrapper = new LambdaQueryWrapperX<Document>()
                .eq(Document::getDeleted, 0)
                .in(Document::getId, ids);
        String fieldOrder = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        wrapper.last("ORDER BY FIELD(id, " + fieldOrder + ")");
        return selectPage(pageParam, wrapper);
    }

    // ========== 批量更新（XML） ==========

    /** 批量更新 type */
    void updateTypeByIds(TypeStatus typeStatus);

    /** 批量更新 deleted */
    void updateDocumentDeleted(DeletedStatus deletedStatus);

    // ========== 简单查询（默认方法） ==========

    /** 查询最大权重 */
    default int selectMaxWeight() {
        List<Document> list = selectList(new LambdaQueryWrapperX<Document>()
                .orderByDesc(Document::getWeight).last("limit 1"));
        if (list.isEmpty()) return 0;
        return list.getFirst().getWeight() != null ? list.getFirst().getWeight() : 0;
    }

    /** 增加阅读数 */
    default void increaseReadNum(Long documentId) {
        LambdaUpdateWrapper<Document> wrapper = new LambdaUpdateWrapper<Document>()
                .eq(Document::getId, documentId)
                .setSql("read_num = read_num + 1");
        update(null, wrapper);
    }

    /** 查询所有文档 ID（未删除） */
    default List<Long> selectAllIds() {
        return selectList(new LambdaQueryWrapperX<Document>()
                .select(Document::getId)
                .orderByAsc(Document::getId))
                .stream()
                .map(Document::getId)
                .toList();
    }

    /** 按创建者查询文档 ID 列表 */
    default List<Long> selectIdsByCreator(String creator) {
        return selectList(new LambdaQueryWrapperX<Document>()
                .select(Document::getId)
                .eq(Document::getCreator, creator))
                .stream()
                .map(Document::getId)
                .toList();
    }

    /** 查询当前租户下的文档 ID 列表 */
    default List<Long> selectIdsByTenant() {
        return selectList(new LambdaQueryWrapperX<Document>()
                .select(Document::getId)
                .eq(Document::getDeleted, 0))
                .stream()
                .map(Document::getId)
                .toList();
    }

    /**
     * 批量查询文档 ID 对应的归属租户
     * <p>
     * 注意：跨租户使用，调用方需通过 {@link cn.iocoder.yudao.framework.tenant.core.util.TenantUtils#executeIgnore}
     * 绕过租户插件，否则会被自动追加 tenant_id 过滤。
     */
    default Map<Long, Long> selectTenantIdMap(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return selectList(new LambdaQueryWrapperX<Document>()
                .select(Document::getId, Document::getTenantId)
                .in(Document::getId, ids)
                .eq(Document::getDeleted, 0))
                .stream().collect(Collectors.toMap(Document::getId, Document::getTenantId));
    }

    @DataPermission(enable = false)
    List<Document> selectDocumentByIds(List<Long> documentIds);

    /** 按 ID 查询文档 */
    @DataPermission(enable = false)
    Document selectDocumentById(Long documentId);

    /**
     * 按文件 ID 查询文档（忽略数据权限，用于文件访问的 ACL 判断）。
     * 匹配 {@code file_id}（封面/主文件）或 {@code resource_ids}（正文引用的资源文件）
     */
    @DataPermission(enable = false)
    default List<Document> selectByFileId(Long fileId) {
        return selectList(new LambdaQueryWrapperX<Document>()
                .eq(Document::getDeleted, 0)
                .and(wrapper -> wrapper
                        .eq(Document::getFileId, fileId)
                        .or()
                        .apply("JSON_CONTAINS(resource_ids, CAST({0} AS JSON))", fileId)));
    }
}
