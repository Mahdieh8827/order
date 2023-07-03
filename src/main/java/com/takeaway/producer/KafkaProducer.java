package com.takeaway.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducer<T> {
    @Value("${spring.kafka.topic.employee-events}")
    private String topicName;

    private final KafkaTemplate<String, T> kafkaTemplate;

    public void send(T message){
        kafkaTemplate.send(topicName, message);
    }
}
