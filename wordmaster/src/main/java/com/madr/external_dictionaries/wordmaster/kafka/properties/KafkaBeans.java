package com.madr.external_dictionaries.wordmaster.kafka.properties;

import com.madr.external_dictionaries.mongomodel.protobuf.Requests;
import com.madr.external_dictionaries.mongomodel.protobuf.Responses;
import com.madr.external_dictionaries.wordmaster.kafka.serdes.WiktionaryRequestDeserializer;
import com.madr.external_dictionaries.wordmaster.kafka.serdes.WiktionaryResponseSerializer;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaBeans {
    @Value(value = "#{@kafkaConfiguration.bootstrapServers}")
    private String bootstrapAddress;
    @Value(value = "#{@kafkaConfiguration.topics.wiktionaryRequests()}")
    private KafkaConfiguration.TopicConfiguration wiktionaryRequests;
    @Value(value = "#{@kafkaConfiguration.topics.bakedWords()}")
    private KafkaConfiguration.TopicConfiguration bakedWords;
    @Value("#{kafkaConfiguration.topics.dlq()}")
    private KafkaConfiguration.TopicConfiguration dlq;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic topicWiktionaryRequests() {
        return TopicBuilder.name(wiktionaryRequests.name())
            .partitions(wiktionaryRequests.partitions())
            .replicas(wiktionaryRequests.replicas())
            .build();
    }

    @Bean
    public ConsumerFactory<String, Requests.WiktionaryRequest> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress,
            ConsumerConfig.GROUP_ID_CONFIG, wiktionaryRequests.name(),
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
            ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, WiktionaryRequestDeserializer.class
        ));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Requests.WiktionaryRequest>
    kafkaListenerContainerFactory(
        ConsumerFactory<String, Requests.WiktionaryRequest> consumerFactory,
        KafkaTemplate<String, Responses.WiktionaryResponse> kafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Requests.WiktionaryRequest> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        var errorHandler = new DefaultErrorHandler(new DeadLetterPublishingRecoverer(
            kafkaTemplate,
            (ignored1, ignored2) -> new TopicPartition(dlq.name(), -1)
        ));
        errorHandler.addNotRetryableExceptions(Exception.class);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    @Bean
    public NewTopic topicDlq() {
        return TopicBuilder.name("updates_dlq")
            .partitions(dlq.partitions())
            .replicas(dlq.replicas())
            .build();
    }

    @Bean
    public NewTopic topicBakedWords() {
        return TopicBuilder.name(bakedWords.name())
            .partitions(bakedWords.partitions())
            .replicas(bakedWords.replicas())
            .build();
    }

    public ProducerFactory<String, Responses.WiktionaryResponse> producerFactory() {
        return new DefaultKafkaProducerFactory<>(Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, WiktionaryResponseSerializer.class
        ));
    }

    @Bean
    public KafkaTemplate<String, Responses.WiktionaryResponse> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
