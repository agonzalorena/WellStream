package com.agonzalorena.msvc.notification.messaging.consumer;

import com.agonzalorena.msvc.notification.presentation.dto.SensorDTO;
import com.agonzalorena.msvc.notification.presentation.service.SseService;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.agonzalorena.msvc.protobuf.SensorProto.SensorEvent;

import java.time.Instant;

@Slf4j
@Component
public class SensorConsumer{
    private static final String TOPIC = "topic-telemetry";

    private final SseService sseService;

    public SensorConsumer(SseService sseService) {
        this.sseService = sseService;
    }

    @KafkaListener(topics = TOPIC, groupId = "notification-telemetry-group")
    public void consume(byte[] payload) {
        try {
            SensorEvent sensorData = SensorEvent.parseFrom(payload);
            //convertir timestamp de google.protobuf.Timestamp a java.time.Instant
            Timestamp protoTime = sensorData.getTimestamp();
            Instant timestamp = Instant.ofEpochSecond(protoTime.getSeconds(), protoTime.getNanos());

            sseService.broadcast("metric", new SensorDTO(
                    sensorData.getWellId(),
                    timestamp,
                    sensorData.getPressurePsi(),
                    sensorData.getTemperatureC(),
                    sensorData.getFlowRateBpd()
            ));
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to parse protobuf message: {}", e.getMessage());
        }
    }
}
