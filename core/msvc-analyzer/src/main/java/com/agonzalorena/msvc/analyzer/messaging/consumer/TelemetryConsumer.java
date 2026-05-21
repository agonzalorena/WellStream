package com.agonzalorena.msvc.analyzer.messaging.consumer;

import com.agonzalorena.msvc.analyzer.messaging.buffer.TelemetryBufferManager;
import com.agonzalorena.msvc.analyzer.messaging.consumer.parser.SensorEventParser;
import com.agonzalorena.msvc.analyzer.presentation.dto.SensorDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
public class TelemetryConsumer {
    private final TelemetryBufferManager telemetryBufferManager;
    private final SensorEventParser sensorEventParser;

    public TelemetryConsumer(TelemetryBufferManager telemetryBufferManager, SensorEventParser sensorEventParser) {
        this.telemetryBufferManager = telemetryBufferManager;
        this.sensorEventParser = sensorEventParser;
    }

    @KafkaListener(topics = "topic-telemetry", groupId = "telemetry-analyzer_group")
    public void consume(byte[] payload) {
        SensorDTO dto = sensorEventParser.parse(payload);
        if(dto != null) {
            System.out.println("Received telemetry data for buffering: " + dto);
            telemetryBufferManager.addSensorData(dto);
        }
    }

}
