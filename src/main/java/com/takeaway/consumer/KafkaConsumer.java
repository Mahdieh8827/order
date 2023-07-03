package com.takeaway.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.takeaway.event.EmployeeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class KafkaConsumer {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "${spring.kafka.topic.employee-events}"  , groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String jsonMessage) {
        try {
            var employeeEvent = objectMapper.readValue(jsonMessage, EmployeeEvent.class);
            log.info(String.format("EmployeeEmail: %s", employeeEvent.getEmployee().getEmail()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
