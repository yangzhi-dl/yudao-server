package cn.iocoder.yudao.module.ai.common.sequence;

import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SequenceManager {

    // 已完成页面计数（消费者使用）
    private final ConcurrentMap<Long, AtomicInteger> completedCounts = new ConcurrentHashMap<>();

    // 文档总页数
    private final ConcurrentMap<Long, Integer> documentTotalPages = new ConcurrentHashMap<>();

    // 是否是重新处理文档
    private final ConcurrentMap<Long, Boolean> reloadDocument = new ConcurrentHashMap<>();

    // 重新处理文档页码
    private final ConcurrentMap<Long, List<Integer>> reloadDocumentPageNumber = new ConcurrentHashMap<>();

    private final ConcurrentMap<Long, ApplicationEvent> documentApplicationEvent = new ConcurrentHashMap<>();

    //文档完成标记（确保回调只触发一次）
    private final ConcurrentMap<Long, AtomicBoolean> documentCompletedFlags = new ConcurrentHashMap<>();

    /**
     * 注册文档的总页数（生产者调用）
     */
    public void registerDocument(Long documentId, int totalPages) {
        documentTotalPages.put(documentId, totalPages);
        completedCounts.put(documentId, new AtomicInteger(0));
        documentCompletedFlags.put(documentId, new AtomicBoolean(false));
    }

    /**
     * 【消费者调用】标记一个页面已完成
     */
    public void markPageCompleted(Long documentId) {
        AtomicInteger count = completedCounts.get(documentId);
        if (count != null) {
            count.incrementAndGet();
        }
    }

    /**
     * 原子性检查并标记文档完成
     * @return 仅当首次检测到完成时返回 true，后续调用返回 false
     */
    public boolean tryMarkDocumentCompleted(Long documentId) {
        Integer total = documentTotalPages.get(documentId);
        AtomicInteger completed = completedCounts.get(documentId);
        AtomicBoolean flag = documentCompletedFlags.get(documentId);

        // 快速失败：文档未注册或标记已存在
        if (total == null || completed == null || flag == null) {
            return false;
        }

        // 1. 先检查计数是否达标（非阻塞读）
        if (completed.get() >= total) {
            // 2. 尝试原子设置标记（CAS操作，只有一个线程能成功）
            return flag.compareAndSet(false, true);
        }
        return false;
    }

    /**
     * 清理文档信息
     */
    public void cleanup(Long documentId) {
        documentTotalPages.remove(documentId);
        completedCounts.remove(documentId);
        reloadDocument.remove(documentId);
        reloadDocumentPageNumber.remove(documentId);
        documentCompletedFlags.remove(documentId);
        documentApplicationEvent.remove(documentId);
    }

    /**
     * 仅移除完成标记（不清理其他信息），用于失败路径中释放 documentCompletedFlags
     * 防止后续 tryMarkDocumentCompleted 永远返回 false
     */
    public void resetCompletedFlag(Long documentId) {
        documentCompletedFlags.remove(documentId);
    }

    public void setDocumentApplicationEvent(Long documentId, ApplicationEvent event) {
        documentApplicationEvent.put(documentId, event);
    }

    public ApplicationEvent getDocumentApplicationEvent(Long documentId) {
        return documentApplicationEvent.get(documentId);
    }

    public void setReloadDocument(Long documentId, Boolean isReload) {
        reloadDocument.put(documentId, isReload);
    }

    public void setReloadDocumentPageNumber(Long documentId, List<Integer> pageNumber) {
        reloadDocumentPageNumber.put(documentId, pageNumber);
    }

    public List<Integer> getReloadDocumentPageNumber(Long documentId) {
        return reloadDocumentPageNumber.get(documentId);
    }

    public Boolean isReloadDocument(Long documentId) {
        return reloadDocument.get(documentId);
    }

    public int getTotalPages(Long documentId) {
        return documentTotalPages.getOrDefault(documentId, 0);
    }
}