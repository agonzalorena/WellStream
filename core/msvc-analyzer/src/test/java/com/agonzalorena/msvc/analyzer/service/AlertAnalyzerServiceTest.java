package com.agonzalorena.msvc.analyzer.service;

import com.agonzalorena.msvc.analyzer.cache.ActiveAlertCacheManager;
import com.agonzalorena.msvc.analyzer.config.AlertLimitsConfig;
import com.agonzalorena.msvc.analyzer.common.enums.LimitType;
import com.agonzalorena.msvc.analyzer.common.enums.MetricType;
import com.agonzalorena.msvc.analyzer.persistence.entity.WellAlert;
import com.agonzalorena.msvc.analyzer.persistence.repository.WellAlertRepository;
import com.agonzalorena.msvc.analyzer.presentation.dto.SensorDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertAnalyzerServiceTest - Pruebas unitarias para AlertAnalyzerService")
class AlertAnalyzerServiceTest {

    @Mock
    private WellAlertRepository repositoryMock;

    @Mock
    private ActiveAlertCacheManager cacheManagerMock;

    @Mock
    private AlertNotificationService notificationServiceMock;

    private AlertAnalyzerService analyzerService;
    private AlertLimitsConfig limitsConfig;
    private Instant testInstant;

    @BeforeEach
    void setup() {
        // Configurar límites
        limitsConfig = new AlertLimitsConfig(
            new AlertLimitsConfig.Pressure(2000.0, 500.0),
            new AlertLimitsConfig.Temperature(100.0, 30.0),
            new AlertLimitsConfig.FlowRate(1500.0, 500.0),
            50.0  // hysteresisMargin
        );

        analyzerService = new AlertAnalyzerService(
            repositoryMock,
            cacheManagerMock,
            notificationServiceMock,
            limitsConfig
        );

        testInstant = Instant.now();
    }

    @Test
    @DisplayName("Debe crear alerta cuando presión excede maxLimit")
    void testCreateAlertWhenPressureExceedsMax() {
        SensorDTO sensor = new SensorDTO("well-1", testInstant, 2100.0, 75.0, 1200.0);

        when(cacheManagerMock.get("well-1", "PRESSURE")).thenReturn(null);

        analyzerService.checkCriticalValues(sensor);

        ArgumentCaptor<WellAlert> captor = ArgumentCaptor.forClass(WellAlert.class);
        verify(repositoryMock).save(captor.capture());

        WellAlert alert = captor.getValue();
        assertThat(alert.getWellId()).isEqualTo("well-1");
        assertThat(alert.getMetricType()).isEqualTo(MetricType.PRESSURE);
        assertThat(alert.getLimitType()).isEqualTo(LimitType.MAX);
        assertThat(alert.getCriticalValue()).isEqualTo(2100.0);
    }

    @Test
    @DisplayName("Debe crear alerta cuando presión por debajo de minLimit")
    void testCreateAlertWhenPressureBelowMin() {
        SensorDTO sensor = new SensorDTO("well-1", testInstant, 400.0, 75.0, 1200.0);

        when(cacheManagerMock.get("well-1", "PRESSURE")).thenReturn(null);

        analyzerService.checkCriticalValues(sensor);

        ArgumentCaptor<WellAlert> captor = ArgumentCaptor.forClass(WellAlert.class);
        verify(repositoryMock).save(captor.capture());

        WellAlert alert = captor.getValue();
        assertThat(alert.getLimitType()).isEqualTo(LimitType.MIN);
    }

    @Test
    @DisplayName("No debe crear alerta cuando valor está dentro de límites")
    void testNoAlertWhenValueWithinLimits() {
        SensorDTO sensor = new SensorDTO("well-1", testInstant, 1500.0, 75.0, 1200.0);

        when(cacheManagerMock.get("well-1", "PRESSURE")).thenReturn(null);

        analyzerService.checkCriticalValues(sensor);

        verify(repositoryMock, never()).save(any(WellAlert.class));
    }

    @Test
    @DisplayName("Debe resolver alerta cuando valor baja del límite máximo menos margen de histéresis")
    void testResolveAlertWhenValueBelowMaxWithHysteresis() {
        // Alert activa por máximo
        WellAlert activeAlert = new WellAlert();
        activeAlert.setWellId("well-1");
        activeAlert.setMetricType(MetricType.PRESSURE);
        activeAlert.setLimitType(LimitType.MAX);
        activeAlert.setMaxLimit(2000.0);

        when(cacheManagerMock.get("well-1", "PRESSURE")).thenReturn(activeAlert);
        when(repositoryMock.resolveAlert("well-1", MetricType.PRESSURE, testInstant)).thenReturn(1);

        // Valor baja del límite máximo menos margen (2000 - 50 = 1950)
        SensorDTO sensor = new SensorDTO("well-1", testInstant, 1900.0, 75.0, 1200.0);

        analyzerService.checkCriticalValues(sensor);

        verify(repositoryMock).resolveAlert("well-1", MetricType.PRESSURE, testInstant);
    }

    @Test
    @DisplayName("Debe resolver alerta cuando valor sube del límite mínimo más margen de histéresis")
    void testResolveAlertWhenValueAboveMinWithHysteresis() {
        // Alert activa por mínimo
        WellAlert activeAlert = new WellAlert();
        activeAlert.setWellId("well-1");
        activeAlert.setMetricType(MetricType.PRESSURE);
        activeAlert.setLimitType(LimitType.MIN);
        activeAlert.setMinLimit(500.0);

        when(cacheManagerMock.get("well-1", "PRESSURE")).thenReturn(activeAlert);
        when(repositoryMock.resolveAlert("well-1", MetricType.PRESSURE, testInstant)).thenReturn(1);

        // Valor sube del límite mínimo más margen (500 + 50 = 550)
        SensorDTO sensor = new SensorDTO("well-1", testInstant, 600.0, 75.0, 1200.0);

        analyzerService.checkCriticalValues(sensor);

        verify(repositoryMock).resolveAlert("well-1", MetricType.PRESSURE, testInstant);
    }

    @Test
    @DisplayName("No debe resolver alerta cuando está dentro del margen de histéresis")
    void testNoResolveAlertWhenWithinHysteresisMargin() {
        WellAlert activeAlert = new WellAlert();
        activeAlert.setWellId("well-1");
        activeAlert.setMetricType(MetricType.PRESSURE);
        activeAlert.setLimitType(LimitType.MAX);
        activeAlert.setMaxLimit(2000.0);

        when(cacheManagerMock.get("well-1", "PRESSURE")).thenReturn(activeAlert);

        // Valor baja a 1960 (menos que 2000 pero más que 1950)
        SensorDTO sensor = new SensorDTO("well-1", testInstant, 1960.0, 75.0, 1200.0);

        analyzerService.checkCriticalValues(sensor);

        verify(repositoryMock, never()).resolveAlert(anyString(), any(), any());
    }

    @Test
    @DisplayName("Debe evaluar todas las métricas (presión, temperatura, flujo)")
    void testEvaluateAllMetrics() {
        SensorDTO sensor = new SensorDTO("well-1", testInstant, 2100.0, 105.0, 1600.0);

        when(cacheManagerMock.get(anyString(), anyString())).thenReturn(null);

        analyzerService.checkCriticalValues(sensor);

        // Debe crear 3 alertas (presión, temperatura, flujo)
        verify(repositoryMock, times(3)).save(any(WellAlert.class));
    }

    @Test
    @DisplayName("Debe manejar race condition al resolver alerta")
    void testHandleRaceConditionWhenResolvingAlert() {
        WellAlert activeAlert = new WellAlert();
        activeAlert.setWellId("well-1");
        activeAlert.setMetricType(MetricType.PRESSURE);
        activeAlert.setLimitType(LimitType.MAX);
        activeAlert.setMaxLimit(2000.0);

        when(cacheManagerMock.get("well-1", "PRESSURE")).thenReturn(activeAlert);
        // Simular que otro hilo ya resolvió la alerta (rowsUpdate = 0)
        when(repositoryMock.resolveAlert("well-1", MetricType.PRESSURE, testInstant)).thenReturn(0);

        SensorDTO sensor = new SensorDTO("well-1", testInstant, 1900.0, 75.0, 1200.0);

        // No debe lanzar excepción
        assertThatCode(() -> analyzerService.checkCriticalValues(sensor))
            .doesNotThrowAnyException();

        // No debe notificar ni actualizar cache
        verify(notificationServiceMock, never()).notifyResolvedAlert(any());
        verify(cacheManagerMock, never()).remove(anyString(), anyString());
    }

    @Test
    @DisplayName("Debe guardar alerta en cache")
    void testSaveAlertInCache() {
        SensorDTO sensor = new SensorDTO("well-1", testInstant, 2100.0, 75.0, 1200.0);

        when(cacheManagerMock.get("well-1", "PRESSURE")).thenReturn(null);

        analyzerService.checkCriticalValues(sensor);

        ArgumentCaptor<WellAlert> captor = ArgumentCaptor.forClass(WellAlert.class);
        verify(cacheManagerMock).save(captor.capture());

        WellAlert alert = captor.getValue();
        assertThat(alert.getWellId()).isEqualTo("well-1");
    }

    @Test
    @DisplayName("Debe notificar alerta activa")
    void testNotifyActiveAlert() {
        SensorDTO sensor = new SensorDTO("well-1", testInstant, 2100.0, 75.0, 1200.0);

        when(cacheManagerMock.get("well-1", "PRESSURE")).thenReturn(null);

        analyzerService.checkCriticalValues(sensor);

        ArgumentCaptor<WellAlert> captor = ArgumentCaptor.forClass(WellAlert.class);
        verify(notificationServiceMock).notifyActiveAlert(captor.capture());

        WellAlert alert = captor.getValue();
        assertThat(alert.getWellId()).isEqualTo("well-1");
    }

    @Test
    @DisplayName("Debe notificar alerta resuelta")
    void testNotifyResolvedAlert() {
        WellAlert activeAlert = new WellAlert();
        activeAlert.setWellId("well-1");
        activeAlert.setMetricType(MetricType.PRESSURE);
        activeAlert.setLimitType(LimitType.MAX);
        activeAlert.setMaxLimit(2000.0);
        activeAlert.setResolved(false);

        when(cacheManagerMock.get("well-1", "PRESSURE")).thenReturn(activeAlert);
        when(repositoryMock.resolveAlert("well-1", MetricType.PRESSURE, testInstant)).thenReturn(1);

        SensorDTO sensor = new SensorDTO("well-1", testInstant, 1900.0, 75.0, 1200.0);

        analyzerService.checkCriticalValues(sensor);

        verify(notificationServiceMock).notifyResolvedAlert(any(WellAlert.class));
    }

    @Test
    @DisplayName("Debe establecer campos correctos al crear alerta")
    void testAlertFieldsSetCorrectly() {
        SensorDTO sensor = new SensorDTO("well-abc", testInstant, 2100.0, 75.0, 1200.0);

        when(cacheManagerMock.get("well-abc", "PRESSURE")).thenReturn(null);

        analyzerService.checkCriticalValues(sensor);

        ArgumentCaptor<WellAlert> captor = ArgumentCaptor.forClass(WellAlert.class);
        verify(repositoryMock).save(captor.capture());

        WellAlert alert = captor.getValue();
        assertThat(alert.getWellId()).isEqualTo("well-abc");
        assertThat(alert.getMetricType()).isEqualTo(MetricType.PRESSURE);
        assertThat(alert.getLimitType()).isEqualTo(LimitType.MAX);
        assertThat(alert.getCriticalValue()).isEqualTo(2100.0);
        assertThat(alert.getMaxLimit()).isEqualTo(2000.0);
        assertThat(alert.getMinLimit()).isEqualTo(500.0);
        assertThat(alert.getStartTime()).isEqualTo(testInstant);
    }

    @Test
    @DisplayName("Debe remover alerta de cache cuando se resuelve")
    void testRemoveAlertFromCacheWhenResolved() {
        WellAlert activeAlert = new WellAlert();
        activeAlert.setWellId("well-1");
        activeAlert.setMetricType(MetricType.PRESSURE);
        activeAlert.setLimitType(LimitType.MAX);
        activeAlert.setMaxLimit(2000.0);

        when(cacheManagerMock.get("well-1", "PRESSURE")).thenReturn(activeAlert);
        when(repositoryMock.resolveAlert("well-1", MetricType.PRESSURE, testInstant)).thenReturn(1);

        SensorDTO sensor = new SensorDTO("well-1", testInstant, 1900.0, 75.0, 1200.0);

        analyzerService.checkCriticalValues(sensor);

        verify(cacheManagerMock).remove("well-1", "PRESSURE");
    }

    @Test
    @DisplayName("Debe evaluar temperatura correctamente")
    void testEvaluateTemperature() {
        SensorDTO sensor = new SensorDTO("well-1", testInstant, 1500.0, 105.0, 1200.0);

        when(cacheManagerMock.get(anyString(), anyString())).thenReturn(null);

        analyzerService.checkCriticalValues(sensor);

        ArgumentCaptor<WellAlert> captor = ArgumentCaptor.forClass(WellAlert.class);
        verify(repositoryMock, atLeastOnce()).save(captor.capture());

        WellAlert temperatureAlert = captor.getAllValues().stream()
            .filter(a -> a.getMetricType() == MetricType.TEMPERATURE)
            .findFirst()
            .orElseThrow();

        assertThat(temperatureAlert.getCriticalValue()).isEqualTo(105.0);
    }

    @Test
    @DisplayName("Debe evaluar flujo correctamente")
    void testEvaluateFlowRate() {
        SensorDTO sensor = new SensorDTO("well-1", testInstant, 1500.0, 75.0, 1600.0);

        when(cacheManagerMock.get(anyString(), anyString())).thenReturn(null);

        analyzerService.checkCriticalValues(sensor);

        ArgumentCaptor<WellAlert> captor = ArgumentCaptor.forClass(WellAlert.class);
        verify(repositoryMock, atLeastOnce()).save(captor.capture());

        WellAlert flowAlert = captor.getAllValues().stream()
            .filter(a -> a.getMetricType() == MetricType.FLOW_RATE)
            .findFirst()
            .orElseThrow();

        assertThat(flowAlert.getCriticalValue()).isEqualTo(1600.0);
    }

    @Test
    @DisplayName("Debe manejar múltiples pozos independientemente")
    void testHandleMultipleWellsIndependently() {
        // Ambos pozos con presión alta para generar alertas
        SensorDTO well1Sensor = new SensorDTO("well-1", testInstant, 2100.0, 75.0, 1200.0);
        SensorDTO well2Sensor = new SensorDTO("well-2", testInstant, 2200.0, 75.0, 1200.0);

        when(cacheManagerMock.get(anyString(), anyString())).thenReturn(null);

        analyzerService.checkCriticalValues(well1Sensor);
        analyzerService.checkCriticalValues(well2Sensor);

        // Se deben haber guardado alertas de ambos pozos
        verify(repositoryMock, atLeast(2)).save(any(WellAlert.class));
    }
}

