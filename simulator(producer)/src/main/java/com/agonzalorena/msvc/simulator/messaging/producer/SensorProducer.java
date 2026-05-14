package com.agonzalorena.msvc.simulator.messaging.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SensorProducer {

    private KafkaTemplate<String, Object> kafkaTemplate;

    public SensorProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String wellId, Object sensorData){
        kafkaTemplate.send("topic_telemetry", wellId, sensorData)
                     .whenComplete((result, exception) -> {
                         if(exception != null){
                             System.err.println("Error sending message: " + exception.getMessage());
                         } else {
                             System.out.println("Message sent to partition: " + result.getRecordMetadata().partition() + " with offset: " + result.getRecordMetadata().offset());
                         }
                     });
    }
}
