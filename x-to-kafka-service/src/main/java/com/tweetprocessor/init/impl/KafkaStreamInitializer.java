package com.tweetprocessor.init.impl;

import com.tweetprocessor.config.KafkaConfigData;
import com.tweetprocessor.init.StreamInitializer;
import com.tweetprocessor.kafka.admin.client.KafkaAdminClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaStreamInitializer implements StreamInitializer {

    private final KafkaAdminClient kafkaAdminClient;
    private final KafkaConfigData kafkaConfigData;

    public KafkaStreamInitializer(KafkaAdminClient kafkaAdminClient, KafkaConfigData kafkaConfigData) {
        this.kafkaAdminClient = kafkaAdminClient;
        this.kafkaConfigData = kafkaConfigData;
    }

    @Override
    public void init() {
        kafkaAdminClient.createTopics();
        kafkaAdminClient.checkSchemaRegistry();
        log.info("Topics with names {} are ready for operations!", kafkaConfigData.getTopicNamesToCreate().toArray());
    }
}
