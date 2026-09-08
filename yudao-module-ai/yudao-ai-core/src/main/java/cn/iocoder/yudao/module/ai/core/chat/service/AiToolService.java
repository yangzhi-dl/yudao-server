package cn.iocoder.yudao.module.ai.core.chat.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.core.chat.dal.dataobject.ModelToolDO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.CreateToolDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ToolPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.UpdateToolDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.McpConfig;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.ToolFunction;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.SelectToolVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.ToolDetailVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.ToolPageVO;
import io.modelcontextprotocol.client.McpSyncClient;

import java.util.List;

public interface AiToolService {

    PageResult<ToolPageVO> pageQuery(ToolPageQueryDTO dto);

    Boolean createTool(CreateToolDTO dto);

    List<ToolPageVO> getAllSystemTools();

    Boolean pingTool(Long id);

    List<ToolFunction> getMcpServiceToolsByConfig(McpConfig config);

    void updateOnlineStatus(Long id, Boolean isOnline);

    Boolean updateTool(UpdateToolDTO dto);

    ToolDetailVO detail(Long id);

    Boolean delete(List<Long> ids);

    PageResult<SelectToolVO> selectPageQuery(ToolPageQueryDTO dto);

    List<ModelToolDO> getOnlineToolsByIds(List<Long> enabledToolIds);

    List<McpSyncClient> loadingMcpSyncClients(List<ModelToolDO> modelTools);

    Integer getToolSize();

}
