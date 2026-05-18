package com.agonzalorena.msvc.analyzer.messaging.consumer;

import com.agonzalorena.msvc.analyzer.presentation.dto.SensorDTO;
import com.agonzalorena.msvc.analyzer.service.AlertAnalyzerService;
import com.agonzalorena.msvc.analyzer.service.TrendAnalyzerService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TelemetryConsumer {

    private final TrendAnalyzerService trendAnalyzerService;
    private final AlertAnalyzerService alertAnalyzerService;

    public TelemetryConsumer(TrendAnalyzerService trendAnalyzerService, AlertAnalyzerService alertAnalyzerService) {
        this.trendAnalyzerService = trendAnalyzerService;
        this.alertAnalyzerService = alertAnalyzerService;
    }

    @KafkaListener(topics = "topic-telemetry", groupId = "telemetry-analyzer_group")
    public void consume(SensorDTO dto) {
        System.out.println("Received telemetry data: " + dto);
        trendAnalyzerService.processIncomingTelemetry(dto);
        alertAnalyzerService.checkCriticalValues(dto);

    }
}
