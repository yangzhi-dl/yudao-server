package cn.iocoder.yudao.module.ai.core.chat.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.CreateModelDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ModelPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.UpdateModelDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ChatApi;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.ModelVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.SelectModelVO;

import java.util.List;

public interface AiModelService {

    ChatApi selectChatModelApiById(Long id);

    SelectModelVO selectModelById(Long modelId);

    PageResult<ModelVO> pageQuery(ModelPageQueryDTO modelPageQueryDTO);

    List<SelectModelVO> selectModelList(Boolean filterEmbedding);

    boolean deleteModelById(List<Long> ids);

    boolean updateModel(UpdateModelDTO model);

    boolean insertModel(CreateModelDTO model);

}
