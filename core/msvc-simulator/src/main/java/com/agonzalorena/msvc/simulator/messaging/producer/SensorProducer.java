package com.agonzalorena.msvc.simulator.messaging.producer;

import com.agonzalorena.msvc.simulator.presentation.dto.SensorDTO;
import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.agonzalorena.msvc.protobuf.SensorProto.SensorEvent;

@Slf4j(topic = "SensorProducer")
@Component
public class SensorProducer {
    private final static String TOPIC = "topic-telemetry";

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


        kafkaTemplate.send(TOPIC, wellId, protoSensor.toByteArray())
                     .whenComplete((result, exception) -> {
                         if(exception != null){
                             log.error("Error sending message: {}", exception.getMessage());
                         } else {
                             log.info("Message sent to partition: {} with offset: {}", result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                         }
                     });
        log.info("[{}] Enviando datos: {}", wellId, sensorData);
    }
}
