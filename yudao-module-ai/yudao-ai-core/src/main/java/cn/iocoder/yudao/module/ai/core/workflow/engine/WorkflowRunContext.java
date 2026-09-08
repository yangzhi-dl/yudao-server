package cn.iocoder.yudao.module.ai.core.workflow.engine;

import cn.iocoder.yudao.module.ai.core.workflow.dal.dataobject.AiWorkflowDO;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 工作流运行上下文（Tinyflow 版）
 * <p>
 * 贯穿一次工作流执行的全过程：携带运行记录、SSE 发射器、节点执行日志等，
 * 执行器通过 {@link #sendEvent(String, Object)} 向客户端推送事件。
 */
@Slf4j
@Data
public class WorkflowRunContext {

    /** 运行记录 ID */
    private Long runId;

    /** 工作流（graph 已选：已发布则用发布快照，否则当前画布） */
    private AiWorkflowDO workflow;

    /** 用户输入（开始节点注入） */
    private Map<String, Object> inputs = new LinkedHashMap<>();

    /** SSE 发射器（可为 null，表示后台静默运行） */
    private SseEmitter emitter;

    /**
     * 节点执行日志：nodeId -> {status, inputs, outputs, elapsedMs, ...}（线程安全，且按执行轨迹顺序排列）
     * <p>
     * 使用 {@link LinkedHashMap} 保持插入顺序（即节点实际执行顺序），避免序列化时因哈希乱序打乱运行轨迹。
     */
    private final Map<String, Object> nodeLogs = Collections.synchronizedMap(new LinkedHashMap<>());

    /** 节点执行序号（递增，标记每个节点在整个运行轨迹中的先后位置） */
    private final AtomicInteger logSeq = new AtomicInteger();

    /** 运行开始时间 */
    private final long startTime = System.currentTimeMillis();

    /** 停止标志（前端点停止按钮） */
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    /** 错误信息（线程安全） */
    private final AtomicReference<String> error = new AtomicReference<>();

    /**
     * 当前正在执行的流式订阅（LLM/Agent 节点）。停止时必须 dispose 掉在途的 AI 流，
     * 才能真正中断模型的流式输出，否则执行线程被 {@code blockLast()} 卡住，模型会一直回复到结束。
     */
    private final AtomicReference<reactor.core.Disposable> activeStream = new AtomicReference<>();

    /** 最终产物（presentFiles 工具生成的文件列表，跨节点累积，最终写入运行记录） */
    private final List<Object> presentedFiles = Collections.synchronizedList(new ArrayList<>());

    /**
     * 累积最终产物文件信息（按 id 去重）
     */
    public void addPresentedFiles(List<?> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        presentedFiles.addAll(files);
    }

    public List<Object> getPresentedFiles() {
        return new ArrayList<>(presentedFiles);
    }

    public void requestStop() {
        stopped.set(true);
        // 中断在途的 AI 流式订阅，让模型立即停止输出
        reactor.core.Disposable disposable = activeStream.getAndSet(null);
        if (disposable != null) {
            disposable.dispose();
        }
    }

    /**
     * 登记当前正在执行的流式订阅，供 {@link #requestStop()} 中断。
     * 若在登记前已被停止，则立即取消，避免竞态导致无效订阅常驻。
     */
    public void trackStream(reactor.core.Disposable disposable) {
        activeStream.set(disposable);
        if (stopped.get() && disposable != null) {
            disposable.dispose();
        }
    }

    /**
     * 流结束后注销订阅（仅当仍指向当前订阅时，避免误清后续订阅）
     */
    public void untrackStream(reactor.core.Disposable disposable) {
        activeStream.compareAndSet(disposable, null);
    }

    /**
     * 发送 SSE 事件
     */
    public void sendEvent(String event, Object data) {
        if (emitter == null) {
            return;
        }
        try {
            // 调试用：打印每次推送的 SSE 事件名与 JSON 内容，便于定位前端接收与渲染问题
            log.debug("[workflow-sse][runId({})] event={} data={}",
                    runId, event, data == null ? "null" : JSON.toJSONString(data));
            if (Objects.nonNull(data)) {
                emitter.send(SseEmitter.event().name(event).id(String.valueOf(runId)).data(data));
            }
        } catch (IOException e) {
            log.warn("[workflow][runId({})] SSE 发送事件 {} 失败", runId, event);
        } catch (IllegalStateException e) {
            // 连接已关闭，忽略
            log.debug("[workflow][runId({})] SSE 连接已关闭", runId);
        }
    }

    /**
     * 记录节点日志（自增执行序号，标记运行轨迹顺序）
     */
    public void logNode(String nodeId, Map<String, Object> nodeLog) {
        nodeLog.put("seq", logSeq.incrementAndGet());
        nodeLogs.put(nodeId, nodeLog);
    }

    /**
     * 当前已耗时（毫秒）
     */
    public long elapsedMs() {
        return System.currentTimeMillis() - startTime;
    }
}
