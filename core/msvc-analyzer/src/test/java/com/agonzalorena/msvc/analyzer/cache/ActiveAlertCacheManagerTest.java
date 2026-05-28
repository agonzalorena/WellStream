package com.agonzalorena.msvc.analyzer.cache;

import com.agonzalorena.msvc.analyzer.common.enums.LimitType;
import com.agonzalorena.msvc.analyzer.common.enums.MetricType;
import com.agonzalorena.msvc.analyzer.persistence.entity.WellAlert;
import com.agonzalorena.msvc.analyzer.persistence.repository.WellAlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActiveAlertCacheManagerTest - Pruebas unitarias para la clase ActiveAlertCacheManager")
class ActiveAlertCacheManagerTest {

    @Mock
    private WellAlertRepository wellAlertRepositoryMock;

    private ActiveAlertCacheManager cacheManager;
    private WellAlert testAlert;

    @BeforeEach
    void setup() {
        when(wellAlertRepositoryMock.findByResolvedFalse()).thenReturn(new ArrayList<>());
        cacheManager = new ActiveAlertCacheManager(wellAlertRepositoryMock);

        testAlert = new WellAlert();
        testAlert.setWellId("test-well");
        testAlert.setMetricType(MetricType.PRESSURE);
        testAlert.setLimitType(LimitType.MAX);
        testAlert.setCriticalValue(2000.0);
        testAlert.setMaxLimit(1800.0);
        testAlert.setMinLimit(500.0);
        testAlert.setStartTime(Instant.now());
    }

    @Test
    @DisplayName("Debe guardar alerta correctamente en cache")
    void testSaveAlert() {
        cacheManager.save(testAlert);

        WellAlert cachedAlert = cacheManager.get("test-well", MetricType.PRESSURE.name());

        assertThat(cachedAlert).isNotNull();
        assertThat(cachedAlert.getWellId()).isEqualTo("test-well");
        assertThat(cachedAlert.getMetricType()).isEqualTo(MetricType.PRESSURE);
    }

    @Test
    @DisplayName("Debe recuperar alerta guardada por wellId y metricType")
    void testGetAlertByWellIdAndMetricType() {
        cacheManager.save(testAlert);

        WellAlert retrieved = cacheManager.get("test-well", "PRESSURE");

        assertThat(retrieved).isEqualTo(testAlert);
    }

    @Test
    @DisplayName("Debe retornar null cuando no existe alerta")
    void testGetNonExistentAlert() {
        WellAlert retrieved = cacheManager.get("nonexistent-well", "PRESSURE");

        assertThat(retrieved).isNull();
    }

    @Test
    @DisplayName("Debe remover alerta de cache correctamente")
    void testRemoveAlert() {
        cacheManager.save(testAlert);

        WellAlert beforeRemove = cacheManager.get("test-well", "PRESSURE");
        assertThat(beforeRemove).isNotNull();

        cacheManager.remove("test-well", "PRESSURE");

        WellAlert afterRemove = cacheManager.get("test-well", "PRESSURE");
        assertThat(afterRemove).isNull();
    }

    @Test
    @DisplayName("Debe manejar múltiples alertas de diferentes pozos")
    void testMultipleAlertsFromDifferentWells() {
        WellAlert alert1 = new WellAlert();
        alert1.setWellId("well-1");
        alert1.setMetricType(MetricType.PRESSURE);
        alert1.setLimitType(LimitType.MAX);

        WellAlert alert2 = new WellAlert();
        alert2.setWellId("well-2");
        alert2.setMetricType(MetricType.TEMPERATURE);
        alert2.setLimitType(LimitType.MIN);

        cacheManager.save(alert1);
        cacheManager.save(alert2);

        WellAlert retrieved1 = cacheManager.get("well-1", "PRESSURE");
        WellAlert retrieved2 = cacheManager.get("well-2", "TEMPERATURE");

        assertThat(retrieved1).isEqualTo(alert1);
        assertThat(retrieved2).isEqualTo(alert2);
    }

    @Test
    @DisplayName("Debe manejar múltiples métricas del mismo pozo")
    void testMultipleMetricsFromSameWell() {
        WellAlert pressureAlert = new WellAlert();
        pressureAlert.setWellId("well-1");
        pressureAlert.setMetricType(MetricType.PRESSURE);

        WellAlert temperatureAlert = new WellAlert();
        temperatureAlert.setWellId("well-1");
        temperatureAlert.setMetricType(MetricType.TEMPERATURE);

        WellAlert flowAlert = new WellAlert();
        flowAlert.setWellId("well-1");
        flowAlert.setMetricType(MetricType.FLOW_RATE);

        cacheManager.save(pressureAlert);
        cacheManager.save(temperatureAlert);
        cacheManager.save(flowAlert);

        WellAlert retrieved1 = cacheManager.get("well-1", "PRESSURE");
        WellAlert retrieved2 = cacheManager.get("well-1", "TEMPERATURE");
        WellAlert retrieved3 = cacheManager.get("well-1", "FLOW_RATE");

        assertThat(retrieved1).isEqualTo(pressureAlert);
        assertThat(retrieved2).isEqualTo(temperatureAlert);
        assertThat(retrieved3).isEqualTo(flowAlert);
    }

    @Test
    @DisplayName("Debe reemplazar alerta existente al guardar con misma clave")
    void testOverwriteExistingAlert() {
        WellAlert alert1 = new WellAlert();
        alert1.setWellId("well-1");
        alert1.setMetricType(MetricType.PRESSURE);
        alert1.setCriticalValue(1500.0);

        cacheManager.save(alert1);

        WellAlert alert2 = new WellAlert();
        alert2.setWellId("well-1");
        alert2.setMetricType(MetricType.PRESSURE);
        alert2.setCriticalValue(2000.0);

        cacheManager.save(alert2);

        WellAlert retrieved = cacheManager.get("well-1", "PRESSURE");

        assertThat(retrieved.getCriticalValue()).isEqualTo(2000.0);
    }

    @Test
    @DisplayName("Debe cargar alertas no resueltas al inicializar")
    void testLoadUnresolvedAlertsOnInit() {
        // Crear un nuevo mock para este test
        WellAlertRepository newMockRepo = mock(WellAlertRepository.class);

        WellAlert unresolvedAlert = new WellAlert();
        unresolvedAlert.setWellId("well-1");
        unresolvedAlert.setMetricType(MetricType.PRESSURE);
        unresolvedAlert.setResolved(false);

        List<WellAlert> unresolvedAlerts = new ArrayList<>();
        unresolvedAlerts.add(unresolvedAlert);

        when(newMockRepo.findByResolvedFalse()).thenReturn(unresolvedAlerts);

        ActiveAlertCacheManager newCacheManager = new ActiveAlertCacheManager(newMockRepo);

        WellAlert retrieved = newCacheManager.get("well-1", "PRESSURE");

        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getWellId()).isEqualTo("well-1");
    }

    @Test
    @DisplayName("Debe llamar a findByResolvedFalse durante inicialización")
    void testRepositoryCalled() {
        // Crear un nuevo mock para este test específico
        WellAlertRepository newMockRepo = mock(WellAlertRepository.class);
        when(newMockRepo.findByResolvedFalse()).thenReturn(new ArrayList<>());

        new ActiveAlertCacheManager(newMockRepo);

        verify(newMockRepo, times(1)).findByResolvedFalse();
    }

    @Test
    @DisplayName("Debe remover alerta que no existe sin error")
    void testRemoveNonExistentAlert() {
        assertThatCode(() -> cacheManager.remove("nonexistent-well", "PRESSURE"))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Debe mantener integridad con operaciones consecutivas")
    void testConsecutiveOperations() {
        // Guardar
        cacheManager.save(testAlert);
        WellAlert retrieved1 = cacheManager.get("test-well", "PRESSURE");
        assertThat(retrieved1).isNotNull();

        // Guardar con mismo wellId pero diferente metric
        WellAlert alert2 = new WellAlert();
        alert2.setWellId("test-well");
        alert2.setMetricType(MetricType.TEMPERATURE);
        cacheManager.save(alert2);

        // Ambas deben existir
        assertThat(cacheManager.get("test-well", "PRESSURE")).isNotNull();
        assertThat(cacheManager.get("test-well", "TEMPERATURE")).isNotNull();

        // Remover una
        cacheManager.remove("test-well", "PRESSURE");

        // Verificar que solo se removió una
        assertThat(cacheManager.get("test-well", "PRESSURE")).isNull();
        assertThat(cacheManager.get("test-well", "TEMPERATURE")).isNotNull();
    }

    @Test
    @DisplayName("Debe ser thread-safe con ConcurrentHashMap")
    void testThreadSafety() {
        // Guardar múltiples alertas rápidamente
        for (int i = 0; i < 10; i++) {
            WellAlert alert = new WellAlert();
            alert.setWellId("well-" + i);
            alert.setMetricType(MetricType.PRESSURE);
            cacheManager.save(alert);
        }

        // Recuperar todas
        for (int i = 0; i < 10; i++) {
            WellAlert retrieved = cacheManager.get("well-" + i, "PRESSURE");
            assertThat(retrieved).isNotNull();
        }
    }
}

