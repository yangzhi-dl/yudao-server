package cn.iocoder.yudao.module.ai.core.rag.factory;

import cn.iocoder.yudao.module.ai.core.rag.enums.EmbeddingWorkEnum;

public interface EmbeddingWorkService {
    EmbeddingWorkEnum getType();
    Boolean process(Long objectId);
}