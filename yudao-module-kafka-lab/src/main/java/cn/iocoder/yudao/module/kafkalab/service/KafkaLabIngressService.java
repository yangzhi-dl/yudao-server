package cn.iocoder.yudao.module.kafkalab.service;

import cn.iocoder.yudao.module.kafkalab.dal.dataobject.ImbotGroupChatLogDO;
import cn.iocoder.yudao.module.kafkalab.dal.dataobject.ImbotGroupDO;
import cn.iocoder.yudao.module.kafkalab.dal.mysql.ImbotGroupChatLogMapper;
import cn.iocoder.yudao.module.kafkalab.dal.mysql.ImbotGroupMapper;
import cn.iocoder.yudao.module.kafkalab.domain.KafkaLabMessage;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class KafkaLabIngressService {

    private final ImbotGroupChatLogMapper messageMapper;
    private final ImbotGroupMapper groupMapper;
    private final KafkaLabFailureSimulator failureSimulator = new KafkaLabFailureSimulator();

    public KafkaLabMessage parse(String payload) {
        try {
            KafkaLabMessage message = JsonUtils.parseObject(payload, KafkaLabMessage.class);
            if (message == null) {
                throw new IllegalArgumentException("empty Kafka lab payload");
            }
            return message;
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("invalid Kafka lab payload", exception);
        }
    }

    /** 复刻旧版：慢处理和异常都发生在消费线程，调用方会吞掉异常。 */
    public void processLegacy(KafkaLabMessage message, KafkaLabSlowTaskService slowTaskService) {
        persistMessageAndGroup(message);
        failureSimulator.raiseIfRequired(message);
        if (message.getScenario() == cn.iocoder.yudao.module.kafkalab.domain.KafkaLabScenario.SLOW_TASK) {
            slowTaskService.process(message);
        }
    }

    /** 治理版：DB 单元成功后才允许 Listener ACK。 */
    @Transactional(rollbackFor = Exception.class)
    public void processGovernedFastPath(KafkaLabMessage message) {
        persistMessageAndGroup(message);
        failureSimulator.raiseIfRequired(message);
    }

    private void persistMessageAndGroup(KafkaLabMessage message) {
        long timestamp = message.getSentAt() == null ? System.currentTimeMillis() : message.getSentAt();
        ImbotGroupChatLogDO log = new ImbotGroupChatLogDO();
        log.setMessageUid(message.getMessageUid());
        log.setGroupId(message.getGroupId());
        log.setSenderId(message.getSenderId());
        log.setContentText(message.getContentText());
        log.setSourcePlatform("wildfire");
        log.setMsgType(1);
        log.setMsgTimestamp(timestamp);
        log.setSummarizeStatus(0);
        try {
            messageMapper.insert(log);
        } catch (DuplicateKeyException ignored) {
            // 原表 uk_message_uid 是消息日志的幂等边界。
        }

        ImbotGroupDO group = new ImbotGroupDO();
        group.setPlatform("wildfire");
        group.setGroupId(message.getGroupId());
        group.setGroupName(message.getGroupName());
        group.setLastMessageAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()));
        groupMapper.upsert(group);
    }
}
