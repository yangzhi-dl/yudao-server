package cn.iocoder.yudao.module.ai.knowledge.common.factory.embedding;

import cn.iocoder.yudao.module.ai.core.chat.enums.EmbeddingTypeEnum;
import cn.iocoder.yudao.module.ai.core.rag.factory.EmbeddingTypeService;
import cn.iocoder.yudao.module.ai.knowledge.document.dal.dataobject.Document;
import cn.iocoder.yudao.module.ai.knowledge.document.service.DocumentService;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DocumentEmbeddingTypeFactory implements EmbeddingTypeService {

    private final DocumentService documentService;

    public DocumentEmbeddingTypeFactory(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Override
    public EmbeddingTypeEnum getType() {
        return EmbeddingTypeEnum.DOCUMENT;
    }

    @Override
    public String getName(Long id) {
        Document document = documentService.selectById(id);
        return Objects.isNull(document) ? "" : document.getTitle();
    }

}
