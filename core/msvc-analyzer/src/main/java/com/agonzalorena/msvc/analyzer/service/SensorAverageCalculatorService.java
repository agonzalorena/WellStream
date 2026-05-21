package com.agonzalorena.msvc.analyzer.service;

import com.agonzalorena.msvc.analyzer.messaging.buffer.TelemetryBufferManager;
import com.agonzalorena.msvc.analyzer.persistence.entity.SensorAverage;
import com.agonzalorena.msvc.analyzer.persistence.repository.SensorAverageRepository;
import com.agonzalorena.msvc.analyzer.presentation.dto.SensorDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SensorAverageCalculatorService {

    private final TelemetryBufferManager telemetryBufferManager;
    private final SensorAverageRepository sensorAverageRepository;

    public SensorAverageCalculatorService(TelemetryBufferManager telemetryBufferManager, SensorAverageRepository sensorAverageRepository) {
        this.telemetryBufferManager = telemetryBufferManager;
        this.sensorAverageRepository = sensorAverageRepository;
    }


    @Scheduled(fixedDelay = 30000) // Ejecuta cada 30 segundos
    public void processIncomingTelemetry() {
        Map<String, List<SensorDTO>> dataToProcess = telemetryBufferManager.flush();
        if(dataToProcess.isEmpty()){
            log.info("No telemetry data to process at {}", Instant.now());
            return;
        }
        Instant processingTime = Instant.now();

        dataToProcess.forEach((wellId, sensorDataList) -> {
            double sumTemp=0.0;
            double sumPres=0.0;
            double sumFlow=0.0;
            for (SensorDTO dto : sensorDataList) {
                sumTemp += dto.temperatureC();
                sumPres += dto.pressurePsi();
                sumFlow += dto.flowRateBpd();
            }
            int count = sensorDataList.size();

            double averageTemp = (sumTemp / count);
            double averagePres = sumPres / count;
            double averageFlow = sumFlow / count;

            SensorAverage average = new SensorAverage();
            average.setWellId(wellId);
            average.setAvgTemperatureC(round(averageTemp));
            average.setAvgPressurePsi(round(averagePres));
            average.setAvgFlowRateBpd(round(averageFlow));
            average.setTimestamp(processingTime);
            average.setReadingsCount(sensorDataList.size());

            try {
                sensorAverageRepository.save(average);
                log.info("Saved average for wellId: {}, avgTemp: {}, avgPres: {}, avgFlow: {}, count: {}",
                        wellId, averageTemp, averagePres, averageFlow, count);
            }catch (Exception e){
                log.error("Error saving average for wellId: {}, error: {}. Lost data", wellId, e.getMessage());
            }
        });

    }
    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
