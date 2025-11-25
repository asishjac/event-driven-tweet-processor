package com.tweetprocessor.listener;

import com.tweetprocessor.config.KafkaConfigData;
import com.tweetprocessor.kafka.avro.model.XAvroModel;
import com.tweetprocessor.kafka.producer.service.KafkaProducer;
import com.tweetprocessor.transformer.XStatusToAvroTransformer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import twitter4j.Status;
import twitter4j.StatusAdapter;

@Slf4j
@Component
public class XToKafkaStatusListener extends StatusAdapter {

    private final KafkaConfigData kafkaConfigData;
    private final KafkaProducer<Long, XAvroModel> kafkaProducer;
    private final XStatusToAvroTransformer xStatusToAvroTransformer;

    public XToKafkaStatusListener(KafkaConfigData kafkaConfigData, KafkaProducer<Long, XAvroModel> kafkaProducer, XStatusToAvroTransformer xStatusToAvroTransformer) {
        this.kafkaConfigData = kafkaConfigData;
        this.kafkaProducer = kafkaProducer;
        this.xStatusToAvroTransformer = xStatusToAvroTransformer;
    }

    @Override
    public void onStatus(Status status) {
        log.info("Received status text {} sending to kafka topic {}", status.getText(), kafkaConfigData.getTopicName());
        XAvroModel xAvroModel = xStatusToAvroTransformer.getXAvroModelFromStatus(status);
        kafkaProducer.send(kafkaConfigData.getTopicName(), xAvroModel.getUserId(), xAvroModel);
    }
}
