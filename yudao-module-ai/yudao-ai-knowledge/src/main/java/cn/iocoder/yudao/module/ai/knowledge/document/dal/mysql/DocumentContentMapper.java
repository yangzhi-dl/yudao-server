package cn.iocoder.yudao.module.ai.knowledge.document.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.ai.knowledge.document.dal.dataobject.DocumentContent;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DocumentContentMapper extends BaseMapperX<DocumentContent> {

    /**
     * 清空文档的 chunkKeys（XML - 需要 JacksonTypeHandler）
     */
    void clearDocumentChunkKeysByIds(List<Long> documentIds);

    /**
     * 按 documentId 查询
     */
    default DocumentContent selectByDocumentId(Long documentId) {
        return selectOne(DocumentContent::getDocumentId, documentId);
    }

    /**
     * 按 documentId 列表查询
     */
    default List<DocumentContent> selectByDocumentIds(List<Long> documentIds) {
        return selectList(DocumentContent::getDocumentId, documentIds);
    }

    /**
     * 按 documentId 更新
     */
    default void updateByDocumentId(DocumentContent content) {
        LambdaUpdateWrapper<DocumentContent> wrapper = new LambdaUpdateWrapper<DocumentContent>()
                .eq(DocumentContent::getDocumentId, content.getDocumentId());
        update(content, wrapper);
    }
}
