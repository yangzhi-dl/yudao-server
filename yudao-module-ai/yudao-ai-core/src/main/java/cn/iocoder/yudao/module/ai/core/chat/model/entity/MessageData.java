package cn.iocoder.yudao.module.ai.core.chat.model.entity;

import cn.iocoder.yudao.module.ai.core.chat.enums.AiApiType;
import cn.iocoder.yudao.module.ai.core.chat.enums.AiModelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageData implements Serializable {

    private Long topicId;

    private Long lastId;

    private List<Long> fileIds;

    private Long endpointId;

    private Long tenantId;

    /**
     * 用户编号，用于异步线程中恢复用户上下文（如 creator/updater 自动填充）
     */
    private String userId;

    private AiApiType apiType;

    private AiModelType modelType;

    private List<Long> wikiIds;

    private String question;

    private SseEmitter sse;

    private List<AiContent> aiContent;

    private AiAgentMessage aiAgentMessage;

    private List<Document> documents;

    private List<ChatTool> tools;

    private List<Long> selectedTools;

    /**
     * 模型返回的真实 Token 用量
     */
    private Long totalTokens;

    /**
     * 输入(prompt) token 用量
     */
    private Long promptTokens;

    /**
     * 输出(completion) token 用量
     */
    private Long completionTokens;

    /**
     * 重新生成目标：被重生成的 assistant 对话 ID（null = 正常会话；非 null = 重生成会话）。
     * <p>
     * 重生成完成时不会新增 dialogue 行，而是把新回答原地写回该行，
     * 并将被替换的旧回答快照进 ai_chat_regenerate 版本表。
     */
    private Long regenerateDialogueId;

    /**
     * 主流的 Disposable，用于在用户主动停止时取消 AI 模型流式调用
     */
    private transient Disposable streamDisposable;

    /**
     * 多智能体场景下子智能体的 Disposable 列表，用于在用户主动停止时取消所有子智能体流
     */
    private transient List<Disposable> subAgentDisposables;

    /**
     * 添加子智能体 Disposable（线程安全）
     */
    public void addSubAgentDisposable(Disposable disposable) {
        if (this.subAgentDisposables == null) {
            this.subAgentDisposables = new ArrayList<>();
        }
        this.subAgentDisposables.add(disposable);
    }

    /**
     * 取消所有流（主流 + 子智能体流）
     */
    public void cancelAllStreams() {
        if (this.streamDisposable != null && !this.streamDisposable.isDisposed()) {
            this.streamDisposable.dispose();
        }
        if (this.subAgentDisposables != null) {
            this.subAgentDisposables.forEach(d -> {
                if (!d.isDisposed()) {
                    d.dispose();
                }
            });
            this.subAgentDisposables.clear();
        }
    }

}
