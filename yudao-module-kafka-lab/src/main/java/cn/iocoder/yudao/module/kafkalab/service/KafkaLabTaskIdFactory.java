package cn.iocoder.yudao.module.kafkalab.service;

import java.util.Objects;

/**
 * 让同一条 IM 消息在任务表中始终对应同一个业务任务 ID。
 */
public final class KafkaLabTaskIdFactory {

    private KafkaLabTaskIdFactory() {
    }

    public static String fromMessageUid(Long messageUid) {
        return "im-" + Objects.requireNonNull(messageUid, "messageUid must not be null");
    }

}
