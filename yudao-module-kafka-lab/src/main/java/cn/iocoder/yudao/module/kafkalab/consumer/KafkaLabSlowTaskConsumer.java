package cn.iocoder.yudao.module.kafkalab.consumer;

import cn.iocoder.yudao.module.kafkalab.service.KafkaLabIngressService;
import cn.iocoder.yudao.module.kafkalab.service.KafkaLabSlowTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "yudao.kafka-lab", name = "enabled", havingValue = "true")
public class KafkaLabSlowTaskConsumer {

    private final KafkaLabIngressService ingressService;
    private final KafkaLabSlowTaskService slowTaskService;

    @KafkaListener(topics = "${yudao.kafka-lab.slow-topic:kafka-lab-im-slow}",
            groupId = "${yudao.kafka-lab.slow-group:kafka-lab-im-slow}",
            containerFactory = "kafkaLabManualListenerContainerFactory",
            autoStartup = "${yudao.kafka-lab.governed-enabled:true}")
    public void consume(String payload, Acknowledgment acknowledgment) {
        slowTaskService.process(ingressService.parse(payload));
        acknowledgment.acknowledge();
    }
}
