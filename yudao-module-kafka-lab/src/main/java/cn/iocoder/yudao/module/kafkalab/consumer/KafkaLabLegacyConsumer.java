package cn.iocoder.yudao.module.kafkalab.consumer;

import cn.iocoder.yudao.module.kafkalab.service.KafkaLabIngressService;
import cn.iocoder.yudao.module.kafkalab.service.KafkaLabSlowTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "yudao.kafka-lab", name = "enabled", havingValue = "true")
public class KafkaLabLegacyConsumer {

    private final KafkaLabIngressService ingressService;
    private final KafkaLabSlowTaskService slowTaskService;

    @KafkaListener(topics = "${yudao.kafka-lab.raw-topic:kafka-lab-im-raw}",
            groupId = "${yudao.kafka-lab.legacy-group:kafka-lab-im-legacy}",
            containerFactory = "kafkaLabLegacyListenerContainerFactory",
            autoStartup = "${yudao.kafka-lab.legacy-enabled:false}")
    public void consume(String payload) {
        try {
            ingressService.processLegacy(ingressService.parse(payload), slowTaskService);
        } catch (Exception exception) {
            // 刻意复刻旧源码：捕获后正常返回，错误处理器和 DLT 都收不到异常。
            log.error("legacy Kafka lab message failed and was swallowed, payload={}", payload, exception);
        }
    }
}
