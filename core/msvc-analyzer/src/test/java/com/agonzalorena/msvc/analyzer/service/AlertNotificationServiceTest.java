package com.agonzalorena.msvc.analyzer.service;

import com.agonzalorena.msvc.analyzer.common.enums.AlertStatus;
import com.agonzalorena.msvc.analyzer.common.enums.LimitType;
import com.agonzalorena.msvc.analyzer.common.enums.MetricType;
import com.agonzalorena.msvc.analyzer.messaging.producer.AlertProducer;
import com.agonzalorena.msvc.analyzer.persistence.entity.WellAlert;
import com.agonzalorena.msvc.analyzer.presentation.dto.AlertNotificationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.clearInvocations;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertNotificationServiceTest - Pruebas unitarias para AlertNotificationService")
class AlertNotificationServiceTest {

    @Mock
    private AlertProducer alertProducerMock;

    private AlertNotificationService alertNotificationService;
    private WellAlert testAlert;
    private Instant testInstant;

    @BeforeEach
    void setup() {
        alertNotificationService = new AlertNotificationService(alertProducerMock);
        testInstant = Instant.now();

        testAlert = new WellAlert();
        testAlert.setWellId("test-well");
        testAlert.setMetricType(MetricType.PRESSURE);
        testAlert.setLimitType(LimitType.MAX);
        testAlert.setCriticalValue(2000.0);
        testAlert.setMaxLimit(1800.0);
        testAlert.setMinLimit(500.0);
        testAlert.setStartTime(testInstant);
    }

    @Test
    @DisplayName("Debe notificar alerta activa correctamente")
    void testNotifyActiveAlert() {
        alertNotificationService.notifyActiveAlert(testAlert);

        ArgumentCaptor<AlertNotificationDTO> captor = ArgumentCaptor.forClass(AlertNotificationDTO.class);
        verify(alertProducerMock, times(1)).send(captor.capture());

        AlertNotificationDTO dto = captor.getValue();
        assertThat(dto.wellId()).isEqualTo("test-well");
        assertThat(dto.alertStatus()).isEqualTo(AlertStatus.ACTIVE);
    }

    @Test
    @DisplayName("Debe notificar alerta resuelta correctamente")
    void testNotifyResolvedAlert() {
        Instant resolvedTime = testInstant.plusSeconds(3600);
        testAlert.setResolved(true);
        testAlert.setResolvedTime(resolvedTime);

        alertNotificationService.notifyResolvedAlert(testAlert);

        ArgumentCaptor<AlertNotificationDTO> captor = ArgumentCaptor.forClass(AlertNotificationDTO.class);
        verify(alertProducerMock, times(1)).send(captor.capture());

        AlertNotificationDTO dto = captor.getValue();
        assertThat(dto.alertStatus()).isEqualTo(AlertStatus.RESOLVED);
    }

    @Test
    @DisplayName("Debe usar correctamente el timestamp de inicio para alerta activa")
    void testActiveAlertUsesStartTime() {
        alertNotificationService.notifyActiveAlert(testAlert);

        ArgumentCaptor<AlertNotificationDTO> captor = ArgumentCaptor.forClass(AlertNotificationDTO.class);
        verify(alertProducerMock).send(captor.capture());

        AlertNotificationDTO dto = captor.getValue();
        assertThat(dto.timestamp()).isEqualTo(testInstant);
    }

    @Test
    @DisplayName("Debe usar correctamente el timestamp de resolución para alerta resuelta")
    void testResolvedAlertUsesResolvedTime() {
        Instant resolvedTime = testInstant.plusSeconds(7200);
        testAlert.setResolved(true);
        testAlert.setResolvedTime(resolvedTime);

        alertNotificationService.notifyResolvedAlert(testAlert);

        ArgumentCaptor<AlertNotificationDTO> captor = ArgumentCaptor.forClass(AlertNotificationDTO.class);
        verify(alertProducerMock).send(captor.capture());

        AlertNotificationDTO dto = captor.getValue();
        assertThat(dto.timestamp()).isEqualTo(resolvedTime);
    }

    @Test
    @DisplayName("Debe usar maxLimit cuando limitType es MAX")
    void testMaxLimitUsedWhenLimitTypeIsMax() {
        testAlert.setLimitType(LimitType.MAX);
        testAlert.setMaxLimit(1800.0);
        testAlert.setMinLimit(500.0);

        alertNotificationService.notifyActiveAlert(testAlert);

        ArgumentCaptor<AlertNotificationDTO> captor = ArgumentCaptor.forClass(AlertNotificationDTO.class);
        verify(alertProducerMock).send(captor.capture());

        AlertNotificationDTO dto = captor.getValue();
        assertThat(dto.limitExceededValue()).isEqualTo(1800.0);
    }

    @Test
    @DisplayName("Debe usar minLimit cuando limitType es MIN")
    void testMinLimitUsedWhenLimitTypeIsMin() {
        testAlert.setLimitType(LimitType.MIN);
        testAlert.setMaxLimit(2000.0);
        testAlert.setMinLimit(500.0);

        alertNotificationService.notifyActiveAlert(testAlert);

        ArgumentCaptor<AlertNotificationDTO> captor = ArgumentCaptor.forClass(AlertNotificationDTO.class);
        verify(alertProducerMock).send(captor.capture());

        AlertNotificationDTO dto = captor.getValue();
        assertThat(dto.limitExceededValue()).isEqualTo(500.0);
    }

    @Test
    @DisplayName("Debe preservar todos los campos de la alerta en la notificación")
    void testAllFieldsPreservedInNotification() {
        alertNotificationService.notifyActiveAlert(testAlert);

        ArgumentCaptor<AlertNotificationDTO> captor = ArgumentCaptor.forClass(AlertNotificationDTO.class);
        verify(alertProducerMock).send(captor.capture());

        AlertNotificationDTO dto = captor.getValue();
        assertThat(dto.wellId()).isEqualTo("test-well");
        assertThat(dto.metricType()).isEqualTo(MetricType.PRESSURE);
        assertThat(dto.limitType()).isEqualTo(LimitType.MAX);
        assertThat(dto.criticalValue()).isEqualTo(2000.0);
        assertThat(dto.timestamp()).isEqualTo(testInstant);
    }

    @Test
    @DisplayName("Debe notificar diferentes métricas")
    void testNotifyDifferentMetricTypes() {
        // Alerta de presión
        testAlert.setMetricType(MetricType.PRESSURE);
        alertNotificationService.notifyActiveAlert(testAlert);

        // Alerta de temperatura
        testAlert.setMetricType(MetricType.TEMPERATURE);
        alertNotificationService.notifyActiveAlert(testAlert);

        // Alerta de flujo
        testAlert.setMetricType(MetricType.FLOW_RATE);
        alertNotificationService.notifyActiveAlert(testAlert);

        verify(alertProducerMock, times(3)).send(any(AlertNotificationDTO.class));
    }

    @Test
    @DisplayName("Debe notificar múltiples alertas de diferentes pozos")
    void testNotifyMultipleWells() {
        // Alerta pozo 1
        testAlert.setWellId("well-1");
        alertNotificationService.notifyActiveAlert(testAlert);

        // Alerta pozo 2
        testAlert.setWellId("well-2");
        alertNotificationService.notifyActiveAlert(testAlert);

        // Alerta pozo 3
        testAlert.setWellId("well-3");
        alertNotificationService.notifyActiveAlert(testAlert);

        verify(alertProducerMock, times(3)).send(any(AlertNotificationDTO.class));
    }

    @Test
    @DisplayName("Debe llamar a alertProducer.send exactamente una vez por notificación activa")
    void testSendCalledOnceForActiveAlert() {
        alertNotificationService.notifyActiveAlert(testAlert);

        verify(alertProducerMock, times(1)).send(any(AlertNotificationDTO.class));
        verifyNoMoreInteractions(alertProducerMock);
    }

    @Test
    @DisplayName("Debe llamar a alertProducer.send exactamente una vez por notificación resuelta")
    void testSendCalledOnceForResolvedAlert() {
        testAlert.setResolved(true);
        testAlert.setResolvedTime(Instant.now());

        alertNotificationService.notifyResolvedAlert(testAlert);

        verify(alertProducerMock, times(1)).send(any(AlertNotificationDTO.class));
        verifyNoMoreInteractions(alertProducerMock);
    }

    @Test
    @DisplayName("Debe manejar valores numéricos con decimales correctamente")
    void testPreciseNumericValues() {
        testAlert.setCriticalValue(1234.56);
        testAlert.setMaxLimit(1000.99);
        testAlert.setMinLimit(500.01);

        alertNotificationService.notifyActiveAlert(testAlert);

        ArgumentCaptor<AlertNotificationDTO> captor = ArgumentCaptor.forClass(AlertNotificationDTO.class);
        verify(alertProducerMock).send(captor.capture());

        AlertNotificationDTO dto = captor.getValue();
        assertThat(dto.criticalValue()).isEqualTo(1234.56);
        assertThat(dto.limitExceededValue()).isEqualTo(1000.99);
    }

    @Test
    @DisplayName("Debe preservar metricType en la notificación")
    void testMetricTypePreserved() {
        for (MetricType metricType : MetricType.values()) {
            testAlert.setMetricType(metricType);
            alertNotificationService.notifyActiveAlert(testAlert);

            ArgumentCaptor<AlertNotificationDTO> captor = ArgumentCaptor.forClass(AlertNotificationDTO.class);
            verify(alertProducerMock).send(captor.capture());

            AlertNotificationDTO dto = captor.getValue();
            assertThat(dto.metricType()).isEqualTo(metricType);

            clearInvocations(alertProducerMock);
        }
    }

    @Test
    @DisplayName("Debe preservar limitType en la notificación")
    void testLimitTypePreserved() {
        for (LimitType limitType : LimitType.values()) {
            testAlert.setLimitType(limitType);
            alertNotificationService.notifyActiveAlert(testAlert);

            ArgumentCaptor<AlertNotificationDTO> captor = ArgumentCaptor.forClass(AlertNotificationDTO.class);
            verify(alertProducerMock).send(captor.capture());

            AlertNotificationDTO dto = captor.getValue();
            assertThat(dto.limitType()).isEqualTo(limitType);

            clearInvocations(alertProducerMock);
        }
    }
}

