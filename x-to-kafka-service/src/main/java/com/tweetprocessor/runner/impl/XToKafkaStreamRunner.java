package com.tweetprocessor.runner.impl;

import com.tweetprocessor.config.XToKafkaConfigData;
import com.tweetprocessor.listener.XToKafkaStatusListener;
import com.tweetprocessor.runner.StreamRunner;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import twitter4j.FilterQuery;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;

@Component
@Slf4j
@ConditionalOnProperty(
        name = "x-to-kafka-service.enable-mock-tweets",
        havingValue = "false",
        matchIfMissing = true
)
public class XToKafkaStreamRunner implements StreamRunner {

    private final XToKafkaConfigData xToKafkaConfigData;
    private final XToKafkaStatusListener xToKafkaStatusListener;

    private TwitterStream xStream;

    public XToKafkaStreamRunner(XToKafkaConfigData xToKafkaConfigData,
                                XToKafkaStatusListener xToKafkaStatusListener) {
        this.xToKafkaConfigData = xToKafkaConfigData;
        this.xToKafkaStatusListener = xToKafkaStatusListener;
    }

    @Override
    public void start() throws TwitterException {
        xStream = new twitter4j.TwitterStreamFactory().getInstance();
        xStream.addListener(xToKafkaStatusListener);
        addFilter();
    }

    @PreDestroy
    public void shutdown() {
        if (xStream != null) {
            log.info("Shutting down X stream...");
            xStream.shutdown();
        }
    }

    private void addFilter() {
        var keywords = xToKafkaConfigData.getTweetKeywords().toArray(new String[0]);
        var filterQuery = new FilterQuery(keywords);
        xStream.filter(filterQuery);
        log.info("Started filtering X stream for keywords: {}", keywords);
    }
}
