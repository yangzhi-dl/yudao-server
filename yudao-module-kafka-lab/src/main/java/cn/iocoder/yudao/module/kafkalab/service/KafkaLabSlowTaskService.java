package cn.iocoder.yudao.module.kafkalab.service;

import cn.iocoder.yudao.module.kafkalab.dal.dataobject.ImbotTaskDO;
import cn.iocoder.yudao.module.kafkalab.dal.mysql.ImbotTaskMapper;
import cn.iocoder.yudao.module.kafkalab.domain.KafkaLabMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class KafkaLabSlowTaskService {

    private final ImbotTaskMapper taskMapper;

    @Transactional(rollbackFor = Exception.class)
    public void process(KafkaLabMessage message) {
        sleepForLlmSimulation();
        ImbotTaskDO task = new ImbotTaskDO();
        task.setGroupId(message.getGroupId());
        task.setGroupName(message.getGroupName());
        task.setBizDate(LocalDate.ofInstant(Instant.ofEpochMilli(message.getSentAt()), ZoneId.systemDefault()));
        task.setTaskId(KafkaLabTaskIdFactory.fromMessageUid(message.getMessageUid()));
        task.setTopic(message.getContentText());
        task.setStatus("DONE");
        task.setIsClaimed(0);
        task.setEvidence("[" + message.getMessageUid() + "]");
        taskMapper.upsert(task);
    }

    private void sleepForLlmSimulation() {
        try {
            Thread.sleep(800L);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("slow task interrupted", exception);
        }
    }
}
