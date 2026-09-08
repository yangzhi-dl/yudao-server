package cn.iocoder.yudao.module.kafkalab.service;

import cn.iocoder.yudao.module.kafkalab.domain.KafkaLabMessage;
import cn.iocoder.yudao.module.kafkalab.domain.KafkaLabScenario;
import cn.iocoder.yudao.module.kafkalab.exception.KafkaLabPoisonMessageException;
import cn.iocoder.yudao.module.kafkalab.exception.KafkaLabRetryableException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class KafkaLabFailureSimulator {

    private final ConcurrentHashMap<Long, AtomicInteger> attempts = new ConcurrentHashMap<>();

    public void raiseIfRequired(KafkaLabMessage message) {
        KafkaLabScenario scenario = message.getScenario();
        if (scenario == KafkaLabScenario.POISON) {
            throw new KafkaLabPoisonMessageException("poison message " + message.getMessageUid());
        }
        if (scenario != KafkaLabScenario.RETRYABLE && scenario != KafkaLabScenario.PARTIAL_FAILURE) {
            return;
        }
        int attempt = attempts.computeIfAbsent(message.getMessageUid(), ignored -> new AtomicInteger()).incrementAndGet();
        if (attempt < 3) {
            throw new KafkaLabRetryableException("retryable message " + message.getMessageUid() + ", attempt=" + attempt);
        }
    }
}
