package com.agonzalorena.msvc.simulator.model;

import com.agonzalorena.msvc.simulator.presentation.dto.SensorDTO;
import lombok.Data;

import java.time.Instant;

@Data
public class Well {
    private final String wellId;
    private final double basePressure;
    private final double baseTemp;
    private final double baseFlow;

    private double currentPressure;
    private double currentTemp;
    private double currentFlow;

    private double pressureMultiplier = 1.0;
    private double temperatureMultiplier = 1.0;
    private double flowMultiplier = 1.0;

    public Well(String wellId, double initialPressure, double initialTemp, double initialFlow) {
        this.wellId = wellId;
        this.basePressure = initialPressure;
        this.baseTemp = initialTemp;
        this.baseFlow = initialFlow;
        this.currentPressure = initialPressure;
        this.currentTemp = initialTemp;
        this.currentFlow = initialFlow;
    }

    public SensorDTO generateData() {
        this.currentPressure += (Math.random() * 10 - 5);
        this.currentTemp += (Math.random() * 10 - 5);
        this.currentFlow += (Math.random() * 10 - 5);

        return new SensorDTO(
                wellId,
                Instant.now(),
                round(this.currentPressure * pressureMultiplier),
                round(this.currentTemp * temperatureMultiplier),
                round(this.currentFlow * flowMultiplier)
        );
    }
    public void reset() {
        this.currentPressure = basePressure;
        this.currentTemp = baseTemp;
        this.currentFlow = baseFlow;
        this.pressureMultiplier = 1.0;
        this.temperatureMultiplier = 1.0;
        this.flowMultiplier = 1.0;
    }
    // Metodo auxiliar para redondear a 2 decimales
    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }


}
