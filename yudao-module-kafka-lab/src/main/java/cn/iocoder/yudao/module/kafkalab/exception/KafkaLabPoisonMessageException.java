package cn.iocoder.yudao.module.kafkalab.exception;

public class KafkaLabPoisonMessageException extends RuntimeException {
    public KafkaLabPoisonMessageException(String message) {
        super(message);
    }
}
