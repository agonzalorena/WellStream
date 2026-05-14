package com.agonzalorena.msvc.simulator.service;

import com.agonzalorena.msvc.simulator.messaging.producer.SensorProducer;
import com.agonzalorena.msvc.simulator.presentation.dto.SensorDTO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SensorService {
    final private SensorProducer sensorProducer;

    public SensorService(SensorProducer sensorProducer) {
        this.sensorProducer = sensorProducer;
    }

    @Scheduled(fixedRate = 4000)
    public void sendSensorData() {
        SensorDTO sensorData = new SensorDTO(
                "Sensor-1",
                Instant.now(),
                3450.2,
                60.5,
               1013.2
        );
        sensorProducer.sendMessage(sensorData.wellId(),sensorData);
        System.out.println("Sent sensor data: " + sensorData);
    }
}
