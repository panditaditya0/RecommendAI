package com.RecommendAI.RecommendAI.Services.impl;

import com.RecommendAI.RecommendAI.Services.EventStreamingPlatformService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaServiceImpl implements EventStreamingPlatformService {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void sendMessage(String topicName, String message) {
        Message<String> message1 = MessageBuilder
                .withPayload(message)
                .setHeader(KafkaHeaders.TOPIC, topicName)
                .build();
        kafkaTemplate.send(message1);
    }
}
