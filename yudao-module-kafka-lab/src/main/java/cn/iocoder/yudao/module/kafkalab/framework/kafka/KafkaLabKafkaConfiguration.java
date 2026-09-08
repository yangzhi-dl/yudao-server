package cn.iocoder.yudao.module.kafkalab.framework.kafka;

import cn.iocoder.yudao.module.kafkalab.exception.KafkaLabPoisonMessageException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(KafkaLabProperties.class)
@ConditionalOnProperty(prefix = "yudao.kafka-lab", name = "enabled", havingValue = "true")
public class KafkaLabKafkaConfiguration {

    @Bean("kafkaLabKafkaTemplate")
    KafkaTemplate<String, String> kafkaLabKafkaTemplate(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.RETRIES_CONFIG, 3);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(properties));
    }

    @Bean("kafkaLabLegacyListenerContainerFactory")
    ConcurrentKafkaListenerContainerFactory<String, String> legacyFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(consumerProperties(bootstrapServers, true)));
        factory.setConcurrency(1);
        return factory;
    }

    @Bean("kafkaLabManualListenerContainerFactory")
    ConcurrentKafkaListenerContainerFactory<String, String> manualFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            KafkaLabProperties properties,
            DefaultErrorHandler kafkaLabErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(consumerProperties(bootstrapServers, false)));
        factory.setConcurrency(properties.getGovernedConcurrency());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(kafkaLabErrorHandler);
        return factory;
    }

    @Bean
    DefaultErrorHandler kafkaLabErrorHandler(
            @Qualifier("kafkaLabKafkaTemplate") KafkaTemplate<String, String> kafkaLabKafkaTemplate) {
        DefaultErrorHandler handler = new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(kafkaLabKafkaTemplate), new FixedBackOff(1000L, 2L));
        handler.addNotRetryableExceptions(KafkaLabPoisonMessageException.class, IllegalArgumentException.class);
        return handler;
    }

    @Bean
    NewTopic kafkaLabRawTopic(KafkaLabProperties properties) {
        return TopicBuilder.name(properties.getRawTopic()).partitions(properties.getPartitions()).replicas(1).build();
    }

    @Bean
    NewTopic kafkaLabSlowTopic(KafkaLabProperties properties) {
        return TopicBuilder.name(properties.getSlowTopic()).partitions(properties.getPartitions()).replicas(1).build();
    }

    @Bean
    NewTopic kafkaLabRawDltTopic(KafkaLabProperties properties) {
        return TopicBuilder.name(properties.getRawTopic() + ".DLT").partitions(properties.getPartitions()).replicas(1).build();
    }

    @Bean
    NewTopic kafkaLabSlowDltTopic(KafkaLabProperties properties) {
        return TopicBuilder.name(properties.getSlowTopic() + ".DLT").partitions(properties.getPartitions()).replicas(1).build();
    }

    private Map<String, Object> consumerProperties(String bootstrapServers, boolean autoCommit) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, autoCommit);
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        return properties;
    }
}
