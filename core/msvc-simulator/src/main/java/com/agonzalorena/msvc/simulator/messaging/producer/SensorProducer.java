package com.agonzalorena.msvc.simulator.messaging.producer;

import com.agonzalorena.msvc.simulator.presentation.dto.SensorDTO;
import com.google.protobuf.Timestamp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.agonzalorena.msvc.protobuf.SensorProto.SensorEvent;

@Component
public class SensorProducer {
    @Value("${spring.kafka.topics.telemetry}")
    private String telemetryTopic;
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    public SensorProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String wellId, SensorDTO sensorData){
        // Convertimos Instant a google.protobuf.Timestamp
        Timestamp protoTimestamp = Timestamp.newBuilder()
                .setSeconds(sensorData.timestamp().getEpochSecond())
                .setNanos(sensorData.timestamp().getNano())
                .build();

        SensorEvent protoSensor = SensorEvent.newBuilder()
                .setWellId(sensorData.wellId())
                .setTimestamp(protoTimestamp)
                .setPressurePsi(sensorData.pressurePsi())
                .setTemperatureC(sensorData.temperatureC())
                .setFlowRateBpd(sensorData.flowRateBpd())
                .build();


        kafkaTemplate.send(telemetryTopic, wellId, protoSensor.toByteArray())
                     .whenComplete((result, exception) -> {
                         if(exception != null){
                             System.err.println("Error sending message: " + exception.getMessage());
                         } else {
                             System.out.println("Message sent to partition: " + result.getRecordMetadata().partition() + " with offset: " + result.getRecordMetadata().offset());
                         }
                     });
    }
}
