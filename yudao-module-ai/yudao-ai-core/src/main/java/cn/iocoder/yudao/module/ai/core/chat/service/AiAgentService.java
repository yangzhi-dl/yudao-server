package cn.iocoder.yudao.module.ai.core.chat.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.AiAgentDO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.AgentPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.CreateAgentDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.UpdateAgentDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.AgentDetailVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.AgentVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.SelectAgentVO;

import java.util.List;

public interface AiAgentService {

    List<SelectAgentVO> selectAgentList();

    AiAgentDO selectAccessibleAgentById(Long id);

    Boolean increaseUsageCount(Long id);

    AgentDetailVO selectAgentById(Long id);

    PageResult<AgentVO> pageQuery(AgentPageQueryDTO dto);

    Boolean updateData(UpdateAgentDTO dto);

    Boolean delete(List<Long> ids);

    Boolean insert(CreateAgentDTO dto);
}
