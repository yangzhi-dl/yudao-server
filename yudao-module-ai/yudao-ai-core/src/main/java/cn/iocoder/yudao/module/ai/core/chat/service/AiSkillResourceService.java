package cn.iocoder.yudao.module.ai.core.chat.service;

import cn.iocoder.yudao.module.ai.core.chat.model.dto.CreateSkillResourceDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.SkillResourceVO;

import java.util.List;

public interface AiSkillResourceService {

    List<SkillResourceVO> selectListBySkillId(Long skillId);

    Boolean insert(CreateSkillResourceDTO dto);

    Boolean delete(List<Long> ids);

}