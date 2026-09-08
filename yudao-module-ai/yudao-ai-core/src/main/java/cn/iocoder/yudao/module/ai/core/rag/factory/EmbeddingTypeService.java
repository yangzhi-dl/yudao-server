package cn.iocoder.yudao.module.ai.core.rag.factory;

import cn.iocoder.yudao.module.ai.core.chat.enums.EmbeddingTypeEnum;

public interface EmbeddingTypeService {

    EmbeddingTypeEnum getType();
    String getName(Long id);

}
