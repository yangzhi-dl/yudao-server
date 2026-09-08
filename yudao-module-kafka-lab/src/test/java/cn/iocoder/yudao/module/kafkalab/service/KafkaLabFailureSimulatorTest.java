package cn.iocoder.yudao.module.kafkalab.service;

import cn.iocoder.yudao.module.kafkalab.domain.KafkaLabMessage;
import cn.iocoder.yudao.module.kafkalab.domain.KafkaLabScenario;
import cn.iocoder.yudao.module.kafkalab.exception.KafkaLabRetryableException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KafkaLabFailureSimulatorTest {

    @Test
    void shouldFailTwiceThenSucceedForRetryableMessage() {
        KafkaLabMessage message = new KafkaLabMessage();
        message.setMessageUid(10002L);
        message.setScenario(KafkaLabScenario.RETRYABLE);
        KafkaLabFailureSimulator simulator = new KafkaLabFailureSimulator();

        assertThrows(KafkaLabRetryableException.class, () -> simulator.raiseIfRequired(message));
        assertThrows(KafkaLabRetryableException.class, () -> simulator.raiseIfRequired(message));
        assertDoesNotThrow(() -> simulator.raiseIfRequired(message));
    }
}
