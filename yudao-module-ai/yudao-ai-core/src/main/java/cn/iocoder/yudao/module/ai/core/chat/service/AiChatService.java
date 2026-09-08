package cn.iocoder.yudao.module.ai.core.chat.service;

import cn.iocoder.yudao.module.ai.core.chat.model.dto.ExportChatHistoryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.RegenerateMessageDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.SendMessageDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.dto.ToolPageQueryDTO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.ExportChatRecordVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.RegenerateVersionVO;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.SelectEndpointVO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.core.chat.model.vo.SelectToolVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface AiChatService {

    SseEmitter conversation(Long uuid, SendMessageDTO messageDto);

    /**
     * 在上层业务已经完成智能体执行授权校验后发送消息。
     *
     * <p>该方法仅供服务端产品模块调用，不能把“已授权”状态作为前端请求参数透传。
     * 普通 AI 工作台仍应调用 {@link #conversation(Long, SendMessageDTO)}，并执行原有 ACL 校验。</p>
     *
     * @param uuid 会话唯一标识
     * @param messageDto 消息参数
     * @return SSE 发射器
     */
    SseEmitter conversationWithValidatedAgentPermission(Long uuid, SendMessageDTO messageDto);

    /**
     * 重新生成回答：以 lastId（用户消息 ID）为锚点，原地重新生成其 assistant 回答，
     * 被替换的旧回答快照进版本表；dto 携带端点配置（未持久化在对话表中）
     */
    SseEmitter regenerate(Long uuid, Long lastId, RegenerateMessageDTO dto);

    /**
     * 查询指定 assistant 消息的版本列表（第 1 个为当前生效版本）
     */
    List<RegenerateVersionVO> regenerateList(Long dialogueId);

    /**
     * 查询指定话题下所有消息的版本列表（前端按 dialogueId 分组）
     */
    List<RegenerateVersionVO> regenerateListByTopic(Long topicId);

    /**
     * 切换版本：互换主行与目标版本行内容，返回切换后的完整版本列表
     */
    List<RegenerateVersionVO> switchRegenerateVersion(Long dialogueId, Long versionId);

    Boolean stopConversation(Long uuid);

    List<SelectEndpointVO> selectEndpointList();

    PageResult<SelectToolVO> selectToolList(ToolPageQueryDTO dto);

    Boolean clearHistory();

    ExportChatRecordVO exportHistory(ExportChatHistoryDTO dto);

    ExportChatRecordVO exportChatRecord();

}
