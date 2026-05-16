package com.agonzalorena.msvc.analyzer.messaging.consumer;

import com.agonzalorena.msvc.analyzer.presentation.dto.SensorDTO;
import com.agonzalorena.msvc.analyzer.service.TrendAnalyzerService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TelemetryConsumer {

    private static final double MAX_PRESSURE_PSI = 3820.0;
    private static final double MAX_TEMPERATURE_C = 87.0;
    private static final double MIN_FLOW_RATE_BPD = 600.0;

    private final TrendAnalyzerService trendAnalyzerService;

    public TelemetryConsumer(TrendAnalyzerService trendAnalyzerService) {
        this.trendAnalyzerService = trendAnalyzerService;
    }

    @KafkaListener(topics = "topic-telemetry", groupId = "telemetry-analyzer_group")
    public void consume(SensorDTO dto) {
        System.out.println("Received telemetry data: " + dto);
        trendAnalyzerService.processIncomingTelemetry(dto);


        if (dto.pressurePsi() > MAX_PRESSURE_PSI) {
            System.out.println("ALERT: Pressure exceeds threshold! Current: " + dto.pressurePsi() + " PSI");
        }
        if (dto.temperatureC() > MAX_TEMPERATURE_C) {
            System.out.println("ALERT: Temperature exceeds threshold! Current: " + dto.temperatureC() + " °C");
        }
        if (dto.flowRateBpd() < MIN_FLOW_RATE_BPD) {
            System.out.println("ALERT: Flow rate below threshold! Current: " + dto.flowRateBpd() + " BPD");
        }
    }
}
