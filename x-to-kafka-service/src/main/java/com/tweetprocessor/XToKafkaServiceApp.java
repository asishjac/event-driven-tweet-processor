package com.tweetprocessor;

import com.tweetprocessor.init.StreamInitializer;
import com.tweetprocessor.runner.StreamRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = "com.tweetprocessor")
public class XToKafkaServiceApp implements CommandLineRunner {

    private final StreamInitializer streamInitializer;
    private final StreamRunner streamRunner;

    public XToKafkaServiceApp(StreamInitializer streamInitializer, StreamRunner streamRunner) {
        this.streamInitializer = streamInitializer;
        this.streamRunner = streamRunner;
    }

    public static void main(String[] args) {
        SpringApplication.run(XToKafkaServiceApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Application running ...");
        streamInitializer.init();
        streamRunner.start();
    }
}
