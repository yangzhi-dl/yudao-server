package cn.iocoder.yudao.module.kafkalab.framework.kafka;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yudao.kafka-lab")
@Data
public class KafkaLabProperties {

    private boolean enabled;
    private boolean legacyEnabled;
    private boolean governedEnabled = true;
    private String rawTopic = "kafka-lab-im-raw";
    private String slowTopic = "kafka-lab-im-slow";
    private String legacyGroup = "kafka-lab-im-legacy";
    private String governedGroup = "kafka-lab-im-governed";
    private String slowGroup = "kafka-lab-im-slow";
    private String dltGroup = "kafka-lab-im-dlt";
    private int partitions = 6;
    private int governedConcurrency = 6;
}
