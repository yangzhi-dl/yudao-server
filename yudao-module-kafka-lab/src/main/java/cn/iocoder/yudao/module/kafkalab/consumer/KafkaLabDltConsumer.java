package cn.iocoder.yudao.module.kafkalab.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(prefix = "yudao.kafka-lab", name = "enabled", havingValue = "true")
public class KafkaLabDltConsumer {

    @KafkaListener(topics = {"${yudao.kafka-lab.raw-topic:kafka-lab-im-raw}.DLT",
            "${yudao.kafka-lab.slow-topic:kafka-lab-im-slow}.DLT"},
            groupId = "${yudao.kafka-lab.dlt-group:kafka-lab-im-dlt}",
            containerFactory = "kafkaLabLegacyListenerContainerFactory",
            autoStartup = "${yudao.kafka-lab.governed-enabled:true}")
    public void consume(String payload) {
        log.error("Kafka lab dead-letter message requires compensation, payload={}", payload);
    }
}
