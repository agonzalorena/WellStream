package com.agonzalorena.msvc.analyzer.messaging.buffer;

import com.agonzalorena.msvc.analyzer.presentation.dto.SensorDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TelemetryBufferManager {
    private Map<String, List<SensorDTO>> telemetryBuffer = new ConcurrentHashMap<>();

    public synchronized void addSensorData(SensorDTO sensorDTO) {
        String key = sensorDTO.wellId();
        telemetryBuffer.computeIfAbsent(key, k -> new ArrayList<>()).add(sensorDTO);
    }
    /**
     * Extrae los datos usando un "Intercambio Atómico" (Pointer Swap).
     * NO usamos .clear() ni copiamos el mapa para evitar dos problemas:
     * 1. Race Condition: Perder mensajes de Kafka que entren en este exacto milisegundo.
     * 2. ConcurrentModificationException: Modificar las listas mientras se promedian.
     * Al cambiar la referencia a un mapa nuevo, Kafka escribe en el nuevo y nosotros
     * procesamos el viejo de forma 100% segura.
     */
    /*
    * TODO poner limite al buffer para no llenar ram
    *  Procesar datos del buffer con el scheluded
    *  Y procesar datos cuando se llena el buffer, asi si llegan 1500 msj va procesando de a 100 */
    public synchronized Map<String, List<SensorDTO>> flush() {
        Map<String, List<SensorDTO>> dataToProcess = this.telemetryBuffer;
        this.telemetryBuffer = new ConcurrentHashMap<>();
        return dataToProcess;
    }
}
