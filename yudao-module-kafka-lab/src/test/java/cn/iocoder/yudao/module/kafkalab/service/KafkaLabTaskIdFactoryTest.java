package cn.iocoder.yudao.module.kafkalab.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KafkaLabTaskIdFactoryTest {

    @Test
    void shouldBuildStableTaskIdFromMessageUid() {
        assertEquals("im-10001", KafkaLabTaskIdFactory.fromMessageUid(10001L));
    }

}
