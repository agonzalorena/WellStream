package com.agonzalorena.msvc.analyzer.messaging.consumer;

import com.agonzalorena.msvc.analyzer.messaging.buffer.TelemetryBufferManager;
import com.agonzalorena.msvc.analyzer.messaging.consumer.parser.SensorEventParser;
import com.agonzalorena.msvc.analyzer.presentation.dto.SensorDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Slf4j(topic = "TelemetryConsumer")
@Component
public class TelemetryConsumer {
    private static final String TOPIC = "topic-telemetry";

    private final TelemetryBufferManager telemetryBufferManager;
    private final SensorEventParser sensorEventParser;

    public TelemetryConsumer(TelemetryBufferManager telemetryBufferManager, SensorEventParser sensorEventParser) {
        this.telemetryBufferManager = telemetryBufferManager;
        this.sensorEventParser = sensorEventParser;
    }

    @KafkaListener(topics = TOPIC, groupId = "telemetry-analyzer-group")
    public void consume(byte[] payload) {
        SensorDTO dto = sensorEventParser.parse(payload);
        if(dto != null) {
            log.info("Received telemetry data for buffering: {}", dto);
            telemetryBufferManager.addSensorData(dto);
        }
    }

}
