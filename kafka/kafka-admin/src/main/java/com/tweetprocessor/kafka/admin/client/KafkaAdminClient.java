package com.tweetprocessor.kafka.admin.client;

import com.tweetprocessor.config.KafkaConfigData;
import com.tweetprocessor.config.RetryConfigData;
import com.tweetprocessor.kafka.admin.exception.KafkaClientException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@Slf4j
@Component
public class KafkaAdminClient {

    private final KafkaConfigData kafkaConfigData;
    private final RetryConfigData retryConfigData;
    private final AdminClient adminClient;
    private final RetryTemplate retryTemplate;
    private final WebClient webClient;

    public KafkaAdminClient(KafkaConfigData kafkaConfigData, RetryConfigData retryConfigData, AdminClient adminClient, RetryTemplate retryTemplate, WebClient webClient) {
        this.kafkaConfigData = kafkaConfigData;
        this.retryConfigData = retryConfigData;
        this.adminClient = adminClient;
        this.retryTemplate = retryTemplate;
        this.webClient = webClient;
    }

    public void createTopics() {
        try {
            retryTemplate.execute(this::doCreateTopics);
        } catch (Throwable e) {
            log.error("Reached max number of retry attempts {} to create kafka topics", retryConfigData.getMaxAttempts());
            throw new KafkaClientException("Reached max number of retry attempts to create kafka topics", e);
        }
    }

    public void checkTopicsCreated() {
        var topics = getTopics();
        var retryCount = 1;
        var maxRetry = retryConfigData.getMaxAttempts();
        var multiplier = retryConfigData.getMultiplier().intValue();
        var sleepTimeMs = retryConfigData.getSleepTimeMs();
        for (var topic : kafkaConfigData.getTopicNamesToCreate()) {
            while (!isTopicCreated(topic, topics)) {
                checkMaxRetryAttempted(retryCount++, maxRetry);
                sleep(sleepTimeMs);
                sleepTimeMs = sleepTimeMs * multiplier;
                topics = getTopics();
            }
        }
    }

    public void checkSchemaRegistry() {
        var retryCount = 1;
        var maxRetry = retryConfigData.getMaxAttempts();
        var multiplier = retryConfigData.getMultiplier().intValue();
        var sleepTimeMs = retryConfigData.getSleepTimeMs();
        while (!getSchemaRegistryStatus().is2xxSuccessful()) {
            checkMaxRetryAttempted(retryCount++, maxRetry);
            sleep(sleepTimeMs);
            sleepTimeMs = sleepTimeMs * multiplier;
        }
    }

    private HttpStatusCode getSchemaRegistryStatus() {
        try {
            return webClient
                    .get()
                    .uri(kafkaConfigData.getSchemaRegistryUrl())
                    .retrieve()
                    .toBodilessEntity()
                    .map(response -> response.getStatusCode())
                    .block();
        } catch (Exception e) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
    }

    private void sleep(Long sleepTimeMs) {
        try {
            log.info("Sleeping for {} ms before next retry to check if topics are created", sleepTimeMs);
            Thread.sleep(sleepTimeMs);
        } catch (InterruptedException e) {
            throw new KafkaClientException("Error while sleeping to wait for topics to be created", e);
        }
    }

    private void checkMaxRetryAttempted(int retryCount, Integer maxRetry) {
        if (retryCount > maxRetry) {
            log.error("Reached max number of retry attempts {} to verify kafka topics are created", maxRetry);
            throw new KafkaClientException("Reached max number of retry attempts to verify kafka topics are created");
        }
    }

    private boolean isTopicCreated(String topicName, Collection<TopicListing> topics) {
        if (topics == null) {
            return false;
        }
        return topics.stream().anyMatch(topic -> topic.name().equals(topicName));
    }

    private CreateTopicsResult doCreateTopics(RetryContext retryContext) {
        var topicNames = kafkaConfigData.getTopicNamesToCreate();
        log.info("Creating kafka topics {}, attempt number {}",
                topicNames,
                retryContext.getRetryCount());
        var kafkaTopics = topicNames.stream().map(topic -> new NewTopic(
                topic.trim(),
                kafkaConfigData.getNumOfPartitions(),
                kafkaConfigData.getReplicationFactor()
        )).collect(Collectors.toList());
        return adminClient.createTopics(kafkaTopics);
    }

    private Collection<TopicListing> getTopics() {
        try {
            return retryTemplate.execute(this::doGetTopics);
        } catch (Throwable t) {
            log.error("Reached max number of retry attempts {} to get topic listings from kafka", retryConfigData.getMaxAttempts());
            throw new KafkaClientException("Reached max number of retry attempts to get kafka topics", t);
        }
    }

    private Collection<TopicListing> doGetTopics(RetryContext retryContext) throws ExecutionException, InterruptedException {
        log.info("Getting topic listings from kafka, attempt number {}",
                retryContext.getRetryCount());
        var topicList = adminClient.listTopics().listings().get();
        if (topicList != null) {
            topicList.forEach(topic -> log.debug("Topic with name {} found in kafka cluster", topic.name()));
        }
        return topicList;
    }
}
