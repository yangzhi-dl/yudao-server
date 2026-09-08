package cn.iocoder.yudao.module.ai.core.rag.handle;

import cn.iocoder.yudao.module.ai.core.chat.model.entity.ModelUseInfo;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;

public interface PromptHandler {
    ModelUseInfo handle(FileDO file);
}