package com.tweetprocessor.transformer;

import com.tweetprocessor.kafka.avro.model.XAvroModel;
import org.springframework.stereotype.Component;
import twitter4j.Status;

@Component
public class XStatusToAvroTransformer {

    public XAvroModel getXAvroModelFromStatus(Status status) {
        return XAvroModel
                .newBuilder()
                .setId(status.getId())
                .setUserId(status.getUser().getId())
                .setText(status.getText())
                .setCreatedAt(status.getCreatedAt().toInstant())
                .build();
    }
}
