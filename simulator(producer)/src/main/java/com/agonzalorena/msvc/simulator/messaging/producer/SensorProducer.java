package com.agonzalorena.msvc.simulator.messaging.producer;

import com.agonzalorena.msvc.simulator.presentation.dto.SensorDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SensorProducer {
    @Value("${spring.kafka.topics.telemetry}")
    private String telemetryTopic;
    private KafkaTemplate<String, SensorDTO> kafkaTemplate;

    public SensorProducer(KafkaTemplate<String, SensorDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String wellId, SensorDTO sensorData){
        kafkaTemplate.send(telemetryTopic, wellId, sensorData)
                     .whenComplete((result, exception) -> {
                         if(exception != null){
                             System.err.println("Error sending message: " + exception.getMessage());
                         } else {
                             System.out.println("Message sent to partition: " + result.getRecordMetadata().partition() + " with offset: " + result.getRecordMetadata().offset());
                         }
                     });
    }
}
