package com.agonzalorena.msvc.analyzer.messaging.consumer;

import com.agonzalorena.msvc.analyzer.presentation.dto.SensorDTO;
import com.agonzalorena.msvc.analyzer.service.AlertAnalyzerService;
import com.agonzalorena.msvc.analyzer.messaging.buffer.TelemetryBufferManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TelemetryConsumer {
    private final TelemetryBufferManager telemetryBufferManager;
    private final AlertAnalyzerService alertAnalyzerService;

    public TelemetryConsumer(TelemetryBufferManager telemetryBufferManager, AlertAnalyzerService alertAnalyzerService) {
        this.telemetryBufferManager = telemetryBufferManager;
        this.alertAnalyzerService = alertAnalyzerService;
    }

    @KafkaListener(topics = "topic-telemetry", groupId = "telemetry-analyzer_group")
    public void consume(SensorDTO dto) {
        System.out.println("Received telemetry data: " + dto);
        telemetryBufferManager.addSensorData(dto);
        alertAnalyzerService.checkCriticalValues(dto);

    }
}
