package cn.iocoder.yudao.module.kafkalab.consumer;

import cn.iocoder.yudao.module.kafkalab.domain.KafkaLabMessage;
import cn.iocoder.yudao.module.kafkalab.domain.KafkaLabScenario;
import cn.iocoder.yudao.module.kafkalab.producer.KafkaLabProducer;
import cn.iocoder.yudao.module.kafkalab.service.KafkaLabIngressService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "yudao.kafka-lab", name = "enabled", havingValue = "true")
public class KafkaLabGovernedConsumer {

    private final KafkaLabIngressService ingressService;
    private final KafkaLabProducer producer;

    @KafkaListener(topics = "${yudao.kafka-lab.raw-topic:kafka-lab-im-raw}",
            groupId = "${yudao.kafka-lab.governed-group:kafka-lab-im-governed}",
            containerFactory = "kafkaLabManualListenerContainerFactory",
            autoStartup = "${yudao.kafka-lab.governed-enabled:true}")
    public void consume(String payload, Acknowledgment acknowledgment) {
        KafkaLabMessage message = ingressService.parse(payload);
        ingressService.processGovernedFastPath(message);
        if (message.getScenario() == KafkaLabScenario.SLOW_TASK) {
            producer.sendSlow(message);
        }
        acknowledgment.acknowledge();
    }
}
