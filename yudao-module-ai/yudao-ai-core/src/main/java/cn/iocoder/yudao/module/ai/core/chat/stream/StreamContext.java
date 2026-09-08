package cn.iocoder.yudao.module.ai.core.chat.stream;

import cn.iocoder.yudao.module.ai.core.chat.model.entity.FileInfo;
import cn.iocoder.yudao.module.ai.core.chat.model.entity.MessageData;
import lombok.Builder;
import lombok.Data;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * 流式处理上下文 —— 策略模式的参数对象
 * <p>
 * 包含流式处理所需的所有公共参数，各策略实现按需取用。
 *
 */
@Data
@Builder
public class StreamContext {

    /** 会话唯一标识 */
    private Long uuid;
    /** 会话唯一标识（字符串形式） */
    private String _uuid;
    /** 创建者用户ID */
    private String creator;
    /** 端点ID（模型/智能体ID） */
    private Long endpointId;
    /** 知识库ID列表 */
    private List<Long> wikiIds;
    /** 对话消息列表 */
    private List<Message> messages;
    /** 消息数据 */
    private MessageData messageData;
    /** SSE 发射器 */
    private SseEmitter emitter;
    /** 消息映射表 */
    private Map<Long, MessageData> msgMap;
    /** 文件信息列表 */
    private List<FileInfo> fileInfos;
    /** 文件上下文（文本内容） */
    private List<String> fileContext;
    /** 用户原始问题 */
    private String question;
    /** 上层服务是否已经完成智能体执行授权校验 */
    private boolean agentPermissionValidated;
}
