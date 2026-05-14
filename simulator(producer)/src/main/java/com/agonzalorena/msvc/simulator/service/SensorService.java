package com.agonzalorena.msvc.simulator.service;

import com.agonzalorena.msvc.simulator.messaging.producer.SensorProducer;
import com.agonzalorena.msvc.simulator.presentation.dto.SensorDTO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SensorService {
    final private SensorProducer sensorProducer;

    private double currentFlow = 1013.2;
    private double currentPressure = 3450.0;
    private double currentTemp = 60.0;

    public SensorService(SensorProducer sensorProducer) {
        this.sensorProducer = sensorProducer;
    }

    @Scheduled(fixedRate = 4000)
    public void sendSensorData() {
        SensorDTO sensorDto = generateRandomSensorData();
        sensorProducer.sendMessage(sensorDto.wellId(), sensorDto);
        System.out.println("Sent sensor data: " + sensorDto);
    }

    private SensorDTO generateRandomSensorData() {
        this.currentFlow += (Math.random() * 20 - 10);
        this.currentPressure += (Math.random() * 10 - 5);
        this.currentTemp += (Math.random() * 2 - 1);


       return new SensorDTO(
                "Cerro-Dragon-1",
                Instant.now(),
                round(this.currentPressure),
                round(this.currentTemp),
                round(this.currentFlow)
        );
    }

    // Metodo auxiliar para redondear a 2 decimales
    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
