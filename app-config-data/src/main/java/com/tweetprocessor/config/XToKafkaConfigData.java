package com.tweetprocessor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "x-to-kafka-service")
@Data
public class XToKafkaConfigData {
    private List<String> tweetKeywords;
    private String welcomeMessage;
    private String enableMockTweets;
    private Long mockSleepMs;
    private Integer mockMinTweetLength;
    private Integer mockMaxTweetLength;
}
