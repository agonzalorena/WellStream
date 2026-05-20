package com.agonzalorena.msvc.analyzer.service;

import com.agonzalorena.msvc.analyzer.presentation.dto.SensorDTO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TrendAnalyzerService {

    private final Map<String, LinkedList<SensorDTO>> tempHistory = new ConcurrentHashMap<>();
    private static final int WINDOW_SIZE = 5;

    @Async
    public void processIncomingTelemetry(SensorDTO sensorDTO) {
        // computeIfAbsent se asegura de que solo se cree una nueva LinkedList para cada pozo si no existe ya una, evitando condiciones de carrera.
        LinkedList<SensorDTO> readings = tempHistory.computeIfAbsent(
                sensorDTO.wellId(),
                key -> new LinkedList<>()
        );
        synchronized (readings) {
            readings.addLast(sensorDTO);

            if (readings.size() > WINDOW_SIZE) {
                readings.removeFirst();
            }

            if (readings.size() == WINDOW_SIZE) {
                double sumTemp = 0;
                double sumPres = 0;
                double sumFlow = 0;
                for (SensorDTO i : readings) {
                    sumTemp += i.temperatureC();
                    sumPres += i.pressurePsi();
                    sumFlow += i.flowRateBpd();
                }

                double avgTemp = Math.round((sumTemp / WINDOW_SIZE) * 100.0) / 100.0;
                double avgPres = Math.round((sumPres / WINDOW_SIZE) * 100.0) / 100.0;
                double avgFlow = Math.round((sumFlow / WINDOW_SIZE) * 100.0) / 100.0;

                // Guardar con tiempo de procesamiento
                // Instant processingTime = Instant.now();
                System.out.println("Guardando promedio de las ultimas 5 lecturas, pozo: " + sensorDTO.wellId() + ", promedio: " + avgTemp + "°C " + avgPres + "PSI " + avgFlow + "BPD");

                readings.clear();
            }
        }

    }
}
