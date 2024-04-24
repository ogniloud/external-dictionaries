package com.madr.external_dictionaries.wordmaster.kafka.properties;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.kafka", ignoreUnknownFields = false)
@Getter
@Setter
public class KafkaConfiguration {
    private String bootstrapServers;
    private boolean useQueue;
    private Topics topics;

    public record Topics(@NestedConfigurationProperty TopicConfiguration wiktionaryRequests,
                         @NestedConfigurationProperty TopicConfiguration bakedWords,
                         @NestedConfigurationProperty TopicConfiguration dlq) {
    }

    public record TopicConfiguration(@NotEmpty String name, Integer replicas, Integer partitions) {
    }
}
