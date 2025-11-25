package com.tweetprocessor.kafka.producer.service.impl;

import com.tweetprocessor.kafka.avro.model.XAvroModel;
import com.tweetprocessor.kafka.producer.service.KafkaProducer;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;


import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class XKafkaProducer implements KafkaProducer<Long, XAvroModel> {

    private KafkaTemplate<Long, XAvroModel> kafkaTemplate;

    public XKafkaProducer(KafkaTemplate<Long, XAvroModel> template) {
        this.kafkaTemplate = template;
    }

    @Override
    public void send(String topicName, Long key, XAvroModel message) {
        log.info("Sending message='{}' to topic='{}'", message, topicName);
        CompletableFuture<SendResult<Long, XAvroModel>> kafkaResultFuture =
                kafkaTemplate.send(topicName, key, message);
        addCallback(topicName, message, kafkaResultFuture);
    }

    private void addCallback(String topicName, XAvroModel message, CompletableFuture<SendResult<Long, XAvroModel>> kafkaResultFuture) {
        log.info("Inside callback");
        kafkaResultFuture.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Error while sending message {} to topic {}", message.toString(), topicName, ex);
            } else if (result != null && result.getRecordMetadata() != null){
                RecordMetadata metadata = result.getRecordMetadata();
                log.debug("Received new metadata. Topic: {}; Partition {}; Offset {}; Timestamp {}, at time {}",
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset(),
                        metadata.timestamp(),
                        System.nanoTime());
            } else {
                log.warn("Message sent to topic {} returned null result or metadata", topicName);
            }
        });
    }

    @PreDestroy
    public void close() {
        if (kafkaTemplate != null) {
            log.info("Closing kafka producer!");
            kafkaTemplate.destroy();
        }
    }

}
