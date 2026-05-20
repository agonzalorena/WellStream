package com.agonzalorena.msvc.simulator.service;

import com.agonzalorena.msvc.simulator.messaging.producer.SensorProducer;
import com.agonzalorena.msvc.simulator.presentation.dto.SensorDTO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SensorService {
    final private SensorProducer sensorProducer;

    private double currentFlow = 1213.2;
    private double currentPressure = 3250.0;
    private double currentTemp = 60.0;

    // Multiplicadores para simular anomalías
    private double pressureMultiplier = 1.0;
    private double temperatureMultiplier = 1.0;
    private double flowMultiplier = 1.0;

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
        this.currentFlow += (Math.random() * 10 - 5); // +-5
        this.currentPressure += (Math.random() * 10 - 5);
        this.currentTemp += (Math.random() * 10 - 5);

       return new SensorDTO(
                "Cerro-Dragon-1",
                Instant.now(),
                round(this.currentPressure * pressureMultiplier),
                round(this.currentTemp * temperatureMultiplier),
                round(this.currentFlow * flowMultiplier)
        );
    }

    // Metodo auxiliar para redondear a 2 decimales
    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    // Métodos para modificar los multiplicadores (simular anomalías)
    public void setPressureMultiplier(double multiplier) {
        checkPositive(multiplier);
        this.pressureMultiplier = multiplier;
        System.out.println("Presión multiplicador establecido a: " + multiplier);
    }

    public void setTemperatureMultiplier(double multiplier) {
        checkPositive(multiplier);
        this.temperatureMultiplier = multiplier;
        System.out.println("Temperatura multiplicador establecido a: " + multiplier);
    }

    public void setFlowMultiplier(double multiplier) {
        checkPositive(multiplier);
        this.flowMultiplier = multiplier;
        System.out.println("Caudal multiplicador establecido a: " + multiplier);
    }

    public void resetMultipliers() {
        this.pressureMultiplier = 1.0;
        this.temperatureMultiplier = 1.0;
        this.flowMultiplier = 1.0;
        this.currentFlow = 1213.2;
        this.currentPressure = 3250.0;
        this.currentTemp = 60.0;
        System.out.println("Multiplicadores reiniciados a valores normales");
    }

    public SensorMultipliers getMultipliers() {
        return new SensorMultipliers(pressureMultiplier, temperatureMultiplier, flowMultiplier);
    }

    public record SensorMultipliers(double pressure, double temperature, double flow) {
    }
    private void checkPositive(double value){
        if(value <= 0){
            throw new IllegalArgumentException("El multiplicador debe ser un valor mayor a 0");
        }
    }
}
