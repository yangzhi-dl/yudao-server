package cn.iocoder.yudao.module.ai.core.chat.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.CreateSkillDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.SkillPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.UpdateSkillDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.SkillDetailVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.SkillVO;

import java.util.List;

public interface AiSkillService {

    PageResult<SkillVO> pageQuery(SkillPageQueryDTO dto);

    Boolean insert(CreateSkillDTO dto);

    Boolean updateData(UpdateSkillDTO dto);

    Boolean delete(List<Long> ids);

    SkillDetailVO selectSkillById(Long id);

}