package cn.iocoder.yudao.module.kafkalab.controller.admin;

import cn.iocoder.yudao.module.kafkalab.dal.mysql.ImbotGroupChatLogMapper;
import cn.iocoder.yudao.module.kafkalab.dal.mysql.ImbotGroupMapper;
import cn.iocoder.yudao.module.kafkalab.dal.mysql.ImbotTaskMapper;
import cn.iocoder.yudao.module.kafkalab.domain.KafkaLabMessage;
import cn.iocoder.yudao.module.kafkalab.domain.KafkaLabScenario;
import cn.iocoder.yudao.module.kafkalab.producer.KafkaLabProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/kafka-lab")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "yudao.kafka-lab", name = "enabled", havingValue = "true")
public class KafkaLabController {

    private final KafkaLabProducer producer;
    private final ImbotGroupChatLogMapper messageMapper;
    private final ImbotGroupMapper groupMapper;
    private final ImbotTaskMapper taskMapper;
    private final AtomicLong messageSequence = new AtomicLong(System.currentTimeMillis() * 1000L);

    @PostMapping("/publish")
    public Map<String, Object> publish(@RequestParam(defaultValue = "FAST") KafkaLabScenario scenario,
                                       @RequestParam(defaultValue = "1") int count,
                                       @RequestParam(defaultValue = "group") String groupPrefix,
                                       @RequestParam(defaultValue = "60") int groupCount,
                                       @RequestParam(required = false) Long messageUid) {
        int safeCount = Math.max(1, Math.min(count, 10_000));
        int safeGroupCount = Math.max(1, Math.min(groupCount, 1_000));
        long duplicateUid = messageSequence.incrementAndGet();
        for (int index = 0; index < safeCount; index++) {
            String groupId = groupPrefix + "-" + (scenario == KafkaLabScenario.DUPLICATE
                    ? 0 : index % safeGroupCount);
            KafkaLabMessage message = new KafkaLabMessage();
            message.setMessageUid(messageUid != null ? messageUid
                    : scenario == KafkaLabScenario.DUPLICATE ? duplicateUid : messageSequence.incrementAndGet());
            message.setGroupId(groupId);
            message.setGroupName("Kafka 复现群");
            message.setSenderId("user-001");
            message.setContentText("Kafka lab " + scenario + " message " + index);
            message.setScenario(scenario);
            message.setSentAt(System.currentTimeMillis());
            producer.sendRaw(message);
        }
        return Map.of("published", safeCount, "scenario", scenario,
                "groupPrefix", groupPrefix, "groupCount", safeGroupCount);
    }

    @GetMapping("/statistics")
    public Map<String, Long> statistics() {
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("messages", messageMapper.count());
        result.put("groups", groupMapper.count());
        result.put("tasks", taskMapper.count());
        return result;
    }
}
