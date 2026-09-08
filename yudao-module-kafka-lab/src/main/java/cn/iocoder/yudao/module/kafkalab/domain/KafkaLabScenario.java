package cn.iocoder.yudao.module.kafkalab.domain;

public enum KafkaLabScenario {
    FAST,
    SLOW_TASK,
    RETRYABLE,
    POISON,
    PARTIAL_FAILURE,
    DUPLICATE
}
