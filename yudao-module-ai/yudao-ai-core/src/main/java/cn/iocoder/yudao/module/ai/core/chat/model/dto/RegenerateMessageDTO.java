package cn.iocoder.yudao.module.ai.core.chat.model.dto;

import cn.iocoder.yudao.module.ai.core.chat.enums.AiApiType;
import cn.iocoder.yudao.module.ai.core.chat.enums.AiModelType;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 重新生成回答请求 DTO
 * <p>
 * 端点信息（endpointId/apiType/modelType 等）未持久化在 ai_chat_dialogue 表中，
 * 由前端携带当前会话的配置传入，用于选择对应的流式处理策略与模型。
 *
 * @author yudao
 */
@Data
public class RegenerateMessageDTO implements Serializable {

    /**
     * 端点 ID（模型/智能体 ID）
     */
    private Long endpointId;

    /**
     * API 类型
     */
    private AiApiType apiType;

    /**
     * 模型类型
     */
    private AiModelType modelType;

    /**
     * 知识库 ID 列表
     */
    private List<Long> wikiIds;

    /**
     * 选中的工具 ID 列表
     */
    private List<Long> selectedTools;

}
