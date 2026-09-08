package cn.iocoder.yudao.module.kafkalab.exception;

public class KafkaLabRetryableException extends RuntimeException {
    public KafkaLabRetryableException(String message) {
        super(message);
    }
}
