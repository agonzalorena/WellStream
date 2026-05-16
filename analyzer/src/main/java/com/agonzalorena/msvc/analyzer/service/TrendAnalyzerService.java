package com.agonzalorena.msvc.analyzer.service;

import com.agonzalorena.msvc.analyzer.presentation.dto.SensorDTO;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TrendAnalyzerService {

    private final Map<String, LinkedList<Double>> tempHistory = new ConcurrentHashMap<>();

    private static final int WINDOW_SIZE = 5;
    private static final double CRITICAL_TEMP_LIMIT = 85.0;

    public void processIncomingTelemetry(SensorDTO sensorDTO) {
        LinkedList<Double> readings = tempHistory.get(sensorDTO.wellId());
        if (readings == null) {
            readings = new LinkedList<>();
            tempHistory.put(sensorDTO.wellId(), readings);
        }
        readings.addLast(sensorDTO.temperatureC());

        if (readings.size() > WINDOW_SIZE) {
            readings.removeFirst();
        }

        if (readings.size() == WINDOW_SIZE) {
            double sum = 0;
            for (Double temp : readings) {
                sum += temp;
            }
            double avgTemp = Math.round((sum / WINDOW_SIZE) * 100.0) / 100.0;

            // Guardar con tiempo de procesamiento
            // Instant processingTime = Instant.now();
            System.out.println("Guardando promedio de las ultimas 5 lecturas, pozo: " + sensorDTO.wellId() + ", promedio: " + avgTemp + " °C");

            readings.clear();
        }

    }
}
