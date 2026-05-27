package com.agonzalorena.msvc.simulator.service;

import com.agonzalorena.msvc.simulator.common.enums.MultiplierType;
import com.agonzalorena.msvc.simulator.messaging.producer.SensorProducer;
import com.agonzalorena.msvc.simulator.model.Well;
import com.agonzalorena.msvc.simulator.presentation.dto.SensorDTO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SensorService {
    final private SensorProducer sensorProducer;

    private final Map<String, Well> wells = new HashMap<>();

    public SensorService(SensorProducer sensorProducer) {
        this.sensorProducer = sensorProducer;
        // Inicializar pozos
        wells.put("cerro-dragon", new Well("cerro-dragon", 3250.0, 70.0, 2213.2));
        wells.put("anticlinal-funes", new Well("anticlinal-funes", 2360.0, 60.0, 1278.6));
    }

    @Scheduled(fixedRate = 1000)
    public void sendSensorData() {
        wells.values().forEach(well -> {
            SensorDTO data = well.generateData();
            sensorProducer.sendMessage(data.wellId(), data);

        });
    }

    public List<Well> getWells() {
        return new ArrayList<>(wells.values());
    }

    // Métodos para controlar anomalías por pozo
    public void setMultiplier(String wellId, MultiplierType type, double multiplier) {
        checkPositive(multiplier);
        Well well = wells.get(wellId);
        if (well == null) {
            throw new IllegalArgumentException("Pozo no encontrado: " + wellId);
        }
        switch (type) {
            case PRESSURE -> well.setPressureMultiplier(multiplier);
            case TEMPERATURE -> well.setTemperatureMultiplier(multiplier);
            case FLOW -> well.setFlowMultiplier(multiplier);
        }
    }

    public void resetMultipliers() {
        wells.values().forEach(Well::reset);
    }

    private void checkPositive(double value) {
        if (value <= 0) {
            throw new IllegalArgumentException("El multiplicador debe ser un valor mayor a 0");
        }
    }
}
