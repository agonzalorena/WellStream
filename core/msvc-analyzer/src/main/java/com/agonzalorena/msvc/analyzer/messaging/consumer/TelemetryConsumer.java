package com.agonzalorena.msvc.analyzer.messaging.consumer;

import com.agonzalorena.msvc.analyzer.presentation.dto.SensorDTO;
import com.agonzalorena.msvc.analyzer.service.AlertAnalyzerService;
import com.agonzalorena.msvc.analyzer.messaging.buffer.TelemetryBufferManager;
import com.google.protobuf.InvalidProtocolBufferException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.agonzalorena.msvc.protobuf.SensorProto;

@Component
public class TelemetryConsumer {
    private final TelemetryBufferManager telemetryBufferManager;
    private final AlertAnalyzerService alertAnalyzerService;

    public TelemetryConsumer(TelemetryBufferManager telemetryBufferManager, AlertAnalyzerService alertAnalyzerService) {
        this.telemetryBufferManager = telemetryBufferManager;
        this.alertAnalyzerService = alertAnalyzerService;
    }

    @KafkaListener(topics = "topic-telemetry", groupId = "telemetry-analyzer_group")
    public void consume(byte[] payload) {
        try{
            SensorProto.SensorDTO sensorData = SensorProto.SensorDTO.parseFrom(payload);
            SensorDTO dto = new SensorDTO(
                    sensorData.getWellId(),
                    java.time.Instant.ofEpochMilli(sensorData.getTimestamp()),
                    sensorData.getPressurePsi(),
                    sensorData.getTemperatureC(),
                    sensorData.getFlowRateBpd()
            );
            System.out.println("Received telemetry data: " + dto);
            telemetryBufferManager.addSensorData(dto);
            alertAnalyzerService.checkCriticalValues(dto);
        }catch (InvalidProtocolBufferException e){
            System.err.println("Failed to parse protobuf message: " + e.getMessage());
        }



    }
}
