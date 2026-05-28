package com.agonzalorena.msvc.analyzer.messaging.buffer;

import com.agonzalorena.msvc.analyzer.presentation.dto.SensorDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TelemetryBufferManagerTest - Pruebas unitarias para la clase TelemetryBufferManager")
class TelemetryBufferManagerTest {

    private TelemetryBufferManager bufferManager;
    private SensorDTO testSensorData;

    @BeforeEach
    void setup() {
        bufferManager = new TelemetryBufferManager();
        testSensorData = new SensorDTO("test-well", Instant.now(), 1500.0, 75.5, 1000.0);
    }

    @Test
    @DisplayName("Debe agregar datos de sensor al buffer correctamente")
    void testAddSensorData() {
        bufferManager.addSensorData(testSensorData);

        Map<String, List<SensorDTO>> flushed = bufferManager.flush();

        assertThat(flushed).hasSize(1);
        assertThat(flushed.get("test-well")).hasSize(1);
        assertThat(flushed.get("test-well").get(0)).isEqualTo(testSensorData);
    }

    @Test
    @DisplayName("Debe agregar múltiples datos del mismo pozo")
    void testAddMultipleSensorDataSameWell() {
        SensorDTO data1 = new SensorDTO("well-1", Instant.now(), 1500.0, 75.5, 1000.0);
        SensorDTO data2 = new SensorDTO("well-1", Instant.now(), 1510.0, 76.0, 1005.0);
        SensorDTO data3 = new SensorDTO("well-1", Instant.now(), 1520.0, 76.5, 1010.0);

        bufferManager.addSensorData(data1);
        bufferManager.addSensorData(data2);
        bufferManager.addSensorData(data3);

        Map<String, List<SensorDTO>> flushed = bufferManager.flush();

        assertThat(flushed).hasSize(1);
        assertThat(flushed.get("well-1")).hasSize(3).containsExactly(data1, data2, data3);
    }

    @Test
    @DisplayName("Debe agregar datos de múltiples pozos")
    void testAddSensorDataMultipleWells() {
        SensorDTO dataWell1 = new SensorDTO("well-1", Instant.now(), 1500.0, 75.5, 1000.0);
        SensorDTO dataWell2 = new SensorDTO("well-2", Instant.now(), 2000.0, 80.0, 1500.0);
        SensorDTO dataWell3 = new SensorDTO("well-3", Instant.now(), 1800.0, 78.0, 1200.0);

        bufferManager.addSensorData(dataWell1);
        bufferManager.addSensorData(dataWell2);
        bufferManager.addSensorData(dataWell3);

        Map<String, List<SensorDTO>> flushed = bufferManager.flush();

        assertThat(flushed).hasSize(3);
        assertThat(flushed.get("well-1")).hasSize(1).contains(dataWell1);
        assertThat(flushed.get("well-2")).hasSize(1).contains(dataWell2);
        assertThat(flushed.get("well-3")).hasSize(1).contains(dataWell3);
    }

    @Test
    @DisplayName("Debe limpiar el buffer después de flush (atomic swap)")
    void testFlushClearsBuffer() {
        bufferManager.addSensorData(testSensorData);

        // Primer flush
        Map<String, List<SensorDTO>> flushed1 = bufferManager.flush();
        assertThat(flushed1).hasSize(1);

        // Segundo flush después (debe estar vacío)
        Map<String, List<SensorDTO>> flushed2 = bufferManager.flush();
        assertThat(flushed2).isEmpty();
    }

    @Test
    @DisplayName("Debe mantener datos en buffer hasta que se haga flush")
    void testDataPersistsUntilFlush() {
        bufferManager.addSensorData(testSensorData);

        // Agregar más datos
        SensorDTO additionalData = new SensorDTO("test-well", Instant.now(), 1600.0, 76.0, 1050.0);
        bufferManager.addSensorData(additionalData);

        // Los datos están en el buffer esperando flush
        Map<String, List<SensorDTO>> flushed = bufferManager.flush();

        assertThat(flushed.get("test-well")).hasSize(2);
    }

    @Test
    @DisplayName("Flush debe retornar nuevo buffer vacío después de intercambio")
    void testFlushReturnsNewEmptyBuffer() {
        bufferManager.addSensorData(testSensorData);

        Map<String, List<SensorDTO>> flushed = bufferManager.flush();

        // Agregar más datos después del flush
        SensorDTO newData = new SensorDTO("test-well", Instant.now(), 1700.0, 77.0, 1100.0);
        bufferManager.addSensorData(newData);

        Map<String, List<SensorDTO>> secondFlush = bufferManager.flush();

        assertThat(flushed.get("test-well")).hasSize(1).contains(testSensorData);
        assertThat(secondFlush.get("test-well")).hasSize(1).contains(newData);
    }

    @Test
    @DisplayName("Debe manejar correctamente múltiples pozos con múltiples lecturas")
    void testComplexMultiWellScenario() {
        // Well 1: 3 lecturas
        bufferManager.addSensorData(new SensorDTO("well-1", Instant.now(), 1500.0, 75.0, 1000.0));
        bufferManager.addSensorData(new SensorDTO("well-1", Instant.now(), 1510.0, 75.5, 1005.0));
        bufferManager.addSensorData(new SensorDTO("well-1", Instant.now(), 1520.0, 76.0, 1010.0));

        // Well 2: 2 lecturas
        bufferManager.addSensorData(new SensorDTO("well-2", Instant.now(), 2000.0, 80.0, 1500.0));
        bufferManager.addSensorData(new SensorDTO("well-2", Instant.now(), 2010.0, 80.5, 1505.0));

        // Well 3: 1 lectura
        bufferManager.addSensorData(new SensorDTO("well-3", Instant.now(), 1800.0, 78.0, 1200.0));

        Map<String, List<SensorDTO>> flushed = bufferManager.flush();

        assertThat(flushed).hasSize(3);
        assertThat(flushed.get("well-1")).hasSize(3);
        assertThat(flushed.get("well-2")).hasSize(2);
        assertThat(flushed.get("well-3")).hasSize(1);
    }

    @Test
    @DisplayName("Debe permitir agregar datos después de flush")
    void testAddDataAfterFlush() {
        SensorDTO firstData = new SensorDTO("well-1", Instant.now(), 1500.0, 75.0, 1000.0);
        bufferManager.addSensorData(firstData);

        Map<String, List<SensorDTO>> firstFlush = bufferManager.flush();
        assertThat(firstFlush).hasSize(1);

        // Agregar nuevos datos
        SensorDTO secondData = new SensorDTO("well-1", Instant.now(), 1600.0, 76.0, 1100.0);
        bufferManager.addSensorData(secondData);

        Map<String, List<SensorDTO>> secondFlush = bufferManager.flush();
        assertThat(secondFlush).hasSize(1);
        assertThat(secondFlush.get("well-1")).hasSize(1).contains(secondData);
    }

    @Test
    @DisplayName("Debe retornar mapa vacío cuando no hay datos que procesar")
    void testFlushReturnEmptyMapWhenNoData() {
        Map<String, List<SensorDTO>> flushed = bufferManager.flush();

        assertThat(flushed).isEmpty();
    }

    @Test
    @DisplayName("Buffer vacío debe retornar mapa vacío nuevamente")
    void testConsecutiveFlushesWithoutData() {
        Map<String, List<SensorDTO>> flush1 = bufferManager.flush();
        Map<String, List<SensorDTO>> flush2 = bufferManager.flush();
        Map<String, List<SensorDTO>> flush3 = bufferManager.flush();

        assertThat(flush1).isEmpty();
        assertThat(flush2).isEmpty();
        assertThat(flush3).isEmpty();
    }
}

