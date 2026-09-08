package cn.iocoder.yudao.module.kafkalab.producer;

import cn.iocoder.yudao.module.kafkalab.domain.KafkaLabMessage;
import cn.iocoder.yudao.module.kafkalab.framework.kafka.KafkaLabProperties;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(prefix = "yudao.kafka-lab", name = "enabled", havingValue = "true")
public class KafkaLabProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaLabProperties properties;

    public KafkaLabProducer(@Qualifier("kafkaLabKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate,
                            KafkaLabProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    public void sendRaw(KafkaLabMessage message) {
        send(properties.getRawTopic(), message);
    }

    public void sendSlow(KafkaLabMessage message) {
        send(properties.getSlowTopic(), message);
    }

    private void send(String topic, KafkaLabMessage message) {
        String payload;
        try {
            payload = JsonUtils.toJsonString(message);
        } catch (Exception exception) {
            throw new IllegalArgumentException("cannot serialize Kafka lab message", exception);
        }
        try {
            kafkaTemplate.send(topic, message.getGroupId(), payload).get(10, TimeUnit.SECONDS);
        } catch (Exception exception) {
            throw new IllegalStateException("cannot publish Kafka lab message", exception);
        }
    }
}
