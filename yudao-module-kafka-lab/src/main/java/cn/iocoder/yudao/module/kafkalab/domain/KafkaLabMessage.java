package cn.iocoder.yudao.module.kafkalab.domain;

import lombok.Data;

@Data
public class KafkaLabMessage {
    private Long messageUid;
    private String groupId;
    private String groupName;
    private String senderId;
    private String contentText;
    private KafkaLabScenario scenario;
    private Long sentAt;
}
