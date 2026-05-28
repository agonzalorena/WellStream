package com.agonzalorena.msvc.analyzer.service;

import com.agonzalorena.msvc.analyzer.messaging.buffer.TelemetryBufferManager;
import com.agonzalorena.msvc.analyzer.persistence.entity.SensorAverage;
import com.agonzalorena.msvc.analyzer.persistence.repository.SensorAverageRepository;
import com.agonzalorena.msvc.analyzer.presentation.dto.SensorDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SensorAverageCalculatorServiceTest - Pruebas unitarias para SensorAverageCalculatorService")
class SensorAverageCalculatorServiceTest {

    @Mock
    private TelemetryBufferManager bufferManagerMock;

    @Mock
    private SensorAverageRepository repositoryMock;

    private SensorAverageCalculatorService calculatorService;
    private Instant baseTime;

    @BeforeEach
    void setup() {
        calculatorService = new SensorAverageCalculatorService(bufferManagerMock, repositoryMock);
        baseTime = Instant.now();
    }

    @Test
    @DisplayName("Debe calcular promedio de presión correctamente")
    void testCalculateAveragePressure() {
        // Datos de entrada
        List<SensorDTO> sensorData = new ArrayList<>();
        sensorData.add(new SensorDTO("well-1", baseTime, 1000.0, 70.0, 900.0));
        sensorData.add(new SensorDTO("well-1", baseTime.plusSeconds(10), 1100.0, 72.0, 950.0));
        sensorData.add(new SensorDTO("well-1", baseTime.plusSeconds(20), 1200.0, 74.0, 1000.0));

        Map<String, List<SensorDTO>> buffer = new HashMap<>();
        buffer.put("well-1", sensorData);

        when(bufferManagerMock.flush()).thenReturn(buffer);

        calculatorService.processIncomingTelemetry();

        ArgumentCaptor<SensorAverage> captor = ArgumentCaptor.forClass(SensorAverage.class);
        verify(repositoryMock).save(captor.capture());

        SensorAverage average = captor.getValue();
        // Promedio: (1000 + 1100 + 1200) / 3 = 1100.0
        assertThat(average.getAvgPressurePsi()).isEqualTo(1100.0);
    }

    @Test
    @DisplayName("Debe calcular promedio de temperatura correctamente")
    void testCalculateAverageTemperature() {
        List<SensorDTO> sensorData = new ArrayList<>();
        sensorData.add(new SensorDTO("well-1", baseTime, 1000.0, 70.0, 900.0));
        sensorData.add(new SensorDTO("well-1", baseTime.plusSeconds(10), 1100.0, 80.0, 950.0));
        sensorData.add(new SensorDTO("well-1", baseTime.plusSeconds(20), 1200.0, 90.0, 1000.0));

        Map<String, List<SensorDTO>> buffer = new HashMap<>();
        buffer.put("well-1", sensorData);

        when(bufferManagerMock.flush()).thenReturn(buffer);

        calculatorService.processIncomingTelemetry();

        ArgumentCaptor<SensorAverage> captor = ArgumentCaptor.forClass(SensorAverage.class);
        verify(repositoryMock).save(captor.capture());

        SensorAverage average = captor.getValue();
        // Promedio: (70 + 80 + 90) / 3 = 80.0
        assertThat(average.getAvgTemperatureC()).isEqualTo(80.0);
    }

    @Test
    @DisplayName("Debe calcular promedio de flujo correctamente")
    void testCalculateAverageFlow() {
        List<SensorDTO> sensorData = new ArrayList<>();
        sensorData.add(new SensorDTO("well-1", baseTime, 1000.0, 70.0, 1000.0));
        sensorData.add(new SensorDTO("well-1", baseTime.plusSeconds(10), 1100.0, 72.0, 1100.0));
        sensorData.add(new SensorDTO("well-1", baseTime.plusSeconds(20), 1200.0, 74.0, 1200.0));

        Map<String, List<SensorDTO>> buffer = new HashMap<>();
        buffer.put("well-1", sensorData);

        when(bufferManagerMock.flush()).thenReturn(buffer);

        calculatorService.processIncomingTelemetry();

        ArgumentCaptor<SensorAverage> captor = ArgumentCaptor.forClass(SensorAverage.class);
        verify(repositoryMock).save(captor.capture());

        SensorAverage average = captor.getValue();
        // Promedio: (1000 + 1100 + 1200) / 3 = 1100.0
        assertThat(average.getAvgFlowRateBpd()).isEqualTo(1100.0);
    }

    @Test
    @DisplayName("Debe redondear promedios a 2 decimales")
    void testRoundAverageValues() {
        List<SensorDTO> sensorData = new ArrayList<>();
        sensorData.add(new SensorDTO("well-1", baseTime, 1000.11, 70.11, 900.11));
        sensorData.add(new SensorDTO("well-1", baseTime.plusSeconds(10), 1000.22, 70.22, 900.22));
        sensorData.add(new SensorDTO("well-1", baseTime.plusSeconds(20), 1000.33, 70.33, 900.33));

        Map<String, List<SensorDTO>> buffer = new HashMap<>();
        buffer.put("well-1", sensorData);

        when(bufferManagerMock.flush()).thenReturn(buffer);

        calculatorService.processIncomingTelemetry();

        ArgumentCaptor<SensorAverage> captor = ArgumentCaptor.forClass(SensorAverage.class);
        verify(repositoryMock).save(captor.capture());

        SensorAverage average = captor.getValue();
        // Los valores deben estar redondeados a 2 decimales máximo
        String presStr = String.valueOf(average.getAvgPressurePsi());
        String tempStr = String.valueOf(average.getAvgTemperatureC());
        String flowStr = String.valueOf(average.getAvgFlowRateBpd());

        // Verificar que no tengan más de 2 decimales
        assertThat(presStr.split("\\.")[1].length()).isLessThanOrEqualTo(2);
        assertThat(tempStr.split("\\.")[1].length()).isLessThanOrEqualTo(2);
        assertThat(flowStr.split("\\.")[1].length()).isLessThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Debe establecer wellId correctamente")
    void testSetWellId() {
        List<SensorDTO> sensorData = new ArrayList<>();
        sensorData.add(new SensorDTO("well-test", baseTime, 1000.0, 70.0, 900.0));

        Map<String, List<SensorDTO>> buffer = new HashMap<>();
        buffer.put("well-test", sensorData);

        when(bufferManagerMock.flush()).thenReturn(buffer);

        calculatorService.processIncomingTelemetry();

        ArgumentCaptor<SensorAverage> captor = ArgumentCaptor.forClass(SensorAverage.class);
        verify(repositoryMock).save(captor.capture());

        SensorAverage average = captor.getValue();
        assertThat(average.getWellId()).isEqualTo("well-test");
    }

    @Test
    @DisplayName("Debe establecer tiempos de ventana correctamente")
    void testSetWindowTimes() {
        Instant time1 = baseTime;
        Instant time2 = baseTime.plusSeconds(10);
        Instant time3 = baseTime.plusSeconds(20);

        List<SensorDTO> sensorData = new ArrayList<>();
        sensorData.add(new SensorDTO("well-1", time1, 1000.0, 70.0, 900.0));
        sensorData.add(new SensorDTO("well-1", time2, 1100.0, 72.0, 950.0));
        sensorData.add(new SensorDTO("well-1", time3, 1200.0, 74.0, 1000.0));

        Map<String, List<SensorDTO>> buffer = new HashMap<>();
        buffer.put("well-1", sensorData);

        when(bufferManagerMock.flush()).thenReturn(buffer);

        calculatorService.processIncomingTelemetry();

        ArgumentCaptor<SensorAverage> captor = ArgumentCaptor.forClass(SensorAverage.class);
        verify(repositoryMock).save(captor.capture());

        SensorAverage average = captor.getValue();
        assertThat(average.getStartWindowTime()).isEqualTo(time1);
        assertThat(average.getEndWindowTime()).isEqualTo(time3);
    }

    @Test
    @DisplayName("Debe contar lecturas correctamente")
    void testReadingsCount() {
        List<SensorDTO> sensorData = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            sensorData.add(new SensorDTO("well-1", baseTime.plusSeconds(i * 10), 1000.0 + i * 10, 70.0 + i, 900.0 + i * 5));
        }

        Map<String, List<SensorDTO>> buffer = new HashMap<>();
        buffer.put("well-1", sensorData);

        when(bufferManagerMock.flush()).thenReturn(buffer);

        calculatorService.processIncomingTelemetry();

        ArgumentCaptor<SensorAverage> captor = ArgumentCaptor.forClass(SensorAverage.class);
        verify(repositoryMock).save(captor.capture());

        SensorAverage average = captor.getValue();
        assertThat(average.getReadingsCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("Debe procesar múltiples pozos simultáneamente")
    void testProcessMultipleWells() {
        List<SensorDTO> well1Data = new ArrayList<>();
        well1Data.add(new SensorDTO("well-1", baseTime, 1000.0, 70.0, 900.0));
        well1Data.add(new SensorDTO("well-1", baseTime.plusSeconds(10), 1100.0, 72.0, 950.0));

        List<SensorDTO> well2Data = new ArrayList<>();
        well2Data.add(new SensorDTO("well-2", baseTime, 2000.0, 80.0, 1800.0));
        well2Data.add(new SensorDTO("well-2", baseTime.plusSeconds(10), 2100.0, 82.0, 1850.0));

        Map<String, List<SensorDTO>> buffer = new HashMap<>();
        buffer.put("well-1", well1Data);
        buffer.put("well-2", well2Data);

        when(bufferManagerMock.flush()).thenReturn(buffer);

        calculatorService.processIncomingTelemetry();

        verify(repositoryMock, times(2)).save(any(SensorAverage.class));
    }

    @Test
    @DisplayName("Debe retornar sin procesar si buffer está vacío")
    void testNoProcessingWhenBufferEmpty() {
        when(bufferManagerMock.flush()).thenReturn(new HashMap<>());

        calculatorService.processIncomingTelemetry();

        verify(repositoryMock, never()).save(any(SensorAverage.class));
    }

    @Test
    @DisplayName("Debe manejar excepción sin perder datos de otros pozos")
    void testHandleExceptionForOneWellWithMultiple() {
        List<SensorDTO> well1Data = new ArrayList<>();
        well1Data.add(new SensorDTO("well-1", baseTime, 1000.0, 70.0, 900.0));

        List<SensorDTO> well2Data = new ArrayList<>();
        well2Data.add(new SensorDTO("well-2", baseTime, 2000.0, 80.0, 1800.0));

        Map<String, List<SensorDTO>> buffer = new HashMap<>();
        buffer.put("well-1", well1Data);
        buffer.put("well-2", well2Data);

        when(bufferManagerMock.flush()).thenReturn(buffer);
        doThrow(new RuntimeException("DB Error"))
            .when(repositoryMock).save(any(SensorAverage.class));

        // No debe lanzar excepción
        assertThatCode(() -> calculatorService.processIncomingTelemetry())
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Debe calcular correctamente con una sola lectura")
    void testCalculateWithSingleReading() {
        List<SensorDTO> sensorData = new ArrayList<>();
        sensorData.add(new SensorDTO("well-1", baseTime, 1500.0, 75.0, 1000.0));

        Map<String, List<SensorDTO>> buffer = new HashMap<>();
        buffer.put("well-1", sensorData);

        when(bufferManagerMock.flush()).thenReturn(buffer);

        calculatorService.processIncomingTelemetry();

        ArgumentCaptor<SensorAverage> captor = ArgumentCaptor.forClass(SensorAverage.class);
        verify(repositoryMock).save(captor.capture());

        SensorAverage average = captor.getValue();
        assertThat(average.getAvgPressurePsi()).isEqualTo(1500.0);
        assertThat(average.getAvgTemperatureC()).isEqualTo(75.0);
        assertThat(average.getAvgFlowRateBpd()).isEqualTo(1000.0);
        assertThat(average.getReadingsCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Debe preservar precisión de decimales en cálculo promedio")
    void testDecimalPrecision() {
        List<SensorDTO> sensorData = new ArrayList<>();
        sensorData.add(new SensorDTO("well-1", baseTime, 1000.5, 70.5, 900.5));
        sensorData.add(new SensorDTO("well-1", baseTime.plusSeconds(10), 1001.5, 71.5, 901.5));

        Map<String, List<SensorDTO>> buffer = new HashMap<>();
        buffer.put("well-1", sensorData);

        when(bufferManagerMock.flush()).thenReturn(buffer);

        calculatorService.processIncomingTelemetry();

        ArgumentCaptor<SensorAverage> captor = ArgumentCaptor.forClass(SensorAverage.class);
        verify(repositoryMock).save(captor.capture());

        SensorAverage average = captor.getValue();
        // Promedio: (1000.5 + 1001.5) / 2 = 1001.0
        assertThat(average.getAvgPressurePsi()).isEqualTo(1001.0);
    }

    @Test
    @DisplayName("Debe llamar a flush exactamente una vez")
    void testFlushCalledOnce() {
        when(bufferManagerMock.flush()).thenReturn(new HashMap<>());

        calculatorService.processIncomingTelemetry();

        verify(bufferManagerMock, times(1)).flush();
    }

    @Test
    @DisplayName("Debe procesarBien con datos del mundo real")
    void testRealWorldScenario() {
        // Simular 10 lecturas de 3 pozos
        Map<String, List<SensorDTO>> buffer = new HashMap<>();

        for (String well : new String[]{"well-a", "well-b", "well-c"}) {
            List<SensorDTO> readings = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                readings.add(new SensorDTO(
                    well,
                    baseTime.plusSeconds(i * 5),
                    1000.0 + (Math.random() * 100),
                    70.0 + (Math.random() * 10),
                    900.0 + (Math.random() * 50)
                ));
            }
            buffer.put(well, readings);
        }

        when(bufferManagerMock.flush()).thenReturn(buffer);

        calculatorService.processIncomingTelemetry();

        verify(repositoryMock, times(3)).save(any(SensorAverage.class));
    }
}

