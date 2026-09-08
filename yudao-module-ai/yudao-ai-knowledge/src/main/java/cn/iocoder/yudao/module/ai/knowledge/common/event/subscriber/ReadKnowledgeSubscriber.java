package cn.iocoder.yudao.module.ai.knowledge.common.event.subscriber;

import cn.iocoder.yudao.module.ai.common.enums.UsageRecordType;
import cn.iocoder.yudao.module.ai.common.model.entity.UsageRecord;
import cn.iocoder.yudao.module.ai.common.service.UsageRecordService;
import cn.iocoder.yudao.module.ai.knowledge.common.event.ReadKnowledgeEvent;
import cn.iocoder.yudao.module.ai.knowledge.document.dal.mysql.DocumentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 文档阅读事件订阅者，增加阅读量，并通过 Redis 限制同一用户一天内只增加一次
 *
 */
@Slf4j
@Component
public class ReadKnowledgeSubscriber implements ApplicationListener<ReadKnowledgeEvent> {

    private final DocumentMapper documentMapper;
    private final UsageRecordService usageRecordService;
    private final StringRedisTemplate redisTemplate;

    public ReadKnowledgeSubscriber(DocumentMapper documentMapper, UsageRecordService usageRecordService, StringRedisTemplate redisTemplate) {
        this.documentMapper = documentMapper;
        this.usageRecordService = usageRecordService;
        this.redisTemplate = redisTemplate;
    }

    @Async
    @Override
    public void onApplicationEvent(ReadKnowledgeEvent event) {
        Long documentId = event.getDocumentId();
        Long wikiId = event.getWikiId();
        if (Objects.nonNull(wikiId)) {
            usageRecordService.refreshUsageRecord(UsageRecord.builder()
                    .type(UsageRecordType.WIKI).objectId(wikiId).build());
        }
        usageRecordService.refreshUsageRecord(UsageRecord.builder()
                .type(UsageRecordType.DOCUMENT).objectId(documentId).build());
        String threadName = Thread.currentThread().getName();
        Long loginUserId = getLoginUserId();
        log.debug("==> 文档阅读事件: {}, documentId: {}, userId: {}", threadName, documentId, loginUserId);

        String dateStr = LocalDate.now().toString().replace("-", "");
        String key = "read:documentId:" + documentId + ":user:" + loginUserId + ":" + dateStr;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = LocalDateTime.of(LocalDate.now()
                .plusDays(1), LocalTime.MIDNIGHT);
        long secondsUntilMidnight = Duration.between(now, midnight).getSeconds();

        try {
            Boolean success = redisTemplate.opsForValue()
                    .setIfAbsent(key, "1", Duration.ofSeconds(secondsUntilMidnight));

            if (Boolean.TRUE.equals(success)) {
                // 首次阅读，增加文档阅读量
                documentMapper.increaseReadNum(documentId);
                log.info("==> 文档阅读事件消费成功，阅读量 +1，documentId: {}, userId: {}", documentId, loginUserId);
            } else {
                log.debug("==> 用户 {} 今天已阅读过文档 {}，不再增加阅读量", loginUserId, documentId);
            }
        } catch (Exception e) {
            log.error("==> Redis 操作异常，执行降级增加操作", e);
            documentMapper.increaseReadNum(documentId);
            log.info("==> 文档阅读事件消费成功（降级），阅读量 +1，documentId: {}, userId: {}", documentId, loginUserId);
        }
    }
}