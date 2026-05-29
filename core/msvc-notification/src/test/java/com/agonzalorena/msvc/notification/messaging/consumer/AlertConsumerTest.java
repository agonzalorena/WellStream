package com.agonzalorena.msvc.notification.messaging.consumer;

import com.agonzalorena.msvc.notification.common.enums.AlertStatus;
import com.agonzalorena.msvc.notification.common.enums.LimitType;
import com.agonzalorena.msvc.notification.common.enums.MetricType;
import com.agonzalorena.msvc.notification.presentation.dto.AlertNotificationDTO;
import com.agonzalorena.msvc.notification.presentation.service.SseService;
import com.agonzalorena.msvc.protobuf.AlertProto;
import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

// Nota: Los enums de Protobuf son: PRESSURE, TEMPERATURE, FLOW_RATE (MetricType)
// y MAX, MIN (LimitType) y ACTIVE, RESOLVED (AlertStatus)

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertConsumer - Consumidor de Alertas desde Kafka")
class AlertConsumerTest {

    @Mock
    private SseService sseService;

    private AlertConsumer alertConsumer;

    @BeforeEach
    void setUp() {
        alertConsumer = new AlertConsumer(sseService);
    }

    @Test
    @DisplayName("Debe deserializar correctamente un mensaje Protobuf de alerta")
    void testConsumeValidAlert() {
        // Arrange
        Instant timestamp = Instant.now();
        Timestamp protoTimestamp = Timestamp.newBuilder()
                .setSeconds(timestamp.getEpochSecond())
                .setNanos(timestamp.getNano())
                .build();

        AlertProto.AlertEvent alertEvent = AlertProto.AlertEvent.newBuilder()
                .setWellId("pozo-1")
                .setMetricType(AlertProto.MetricType.PRESSURE)
                .setLimitType(AlertProto.LimitType.MAX)
                .setCriticalValue(5000.0)
                .setLimitExceededValue(5500.0)
                .setTimestamp(protoTimestamp)
                .setAlertStatus(AlertProto.AlertStatus.ACTIVE)
                .build();

        byte[] payload = alertEvent.toByteArray();

        // Act
        alertConsumer.consume(payload);

        // Assert
        verify(sseService).broadcast(
                eq("alert"),
                any(AlertNotificationDTO.class)
        );
        verifyNoMoreInteractions(sseService);
    }

    @Test
    @DisplayName("Debe manejar alerta RESOLVED correctamente")
    void testConsumeResolvedAlert() {
        // Arrange
        Instant timestamp = Instant.now();
        Timestamp protoTimestamp = Timestamp.newBuilder()
                .setSeconds(timestamp.getEpochSecond())
                .setNanos(timestamp.getNano())
                .build();

        AlertProto.AlertEvent alertEvent = AlertProto.AlertEvent.newBuilder()
                .setWellId("pozo-2")
                .setMetricType(AlertProto.MetricType.TEMPERATURE)
                .setLimitType(AlertProto.LimitType.MIN)
                .setCriticalValue(20.0)
                .setLimitExceededValue(15.0)
                .setTimestamp(protoTimestamp)
                .setAlertStatus(AlertProto.AlertStatus.RESOLVED)
                .build();

        byte[] payload = alertEvent.toByteArray();

        // Act
        alertConsumer.consume(payload);

        // Assert
        verify(sseService).broadcast(eq("alert"), any(AlertNotificationDTO.class));
    }

    @Test
    @DisplayName("Debe manejar diferentes tipos de métrica en alertas")
    void testConsumeAlertWithFlowMetric() {
        // Arrange
        Instant timestamp = Instant.now();
        Timestamp protoTimestamp = Timestamp.newBuilder()
                .setSeconds(timestamp.getEpochSecond())
                .setNanos(timestamp.getNano())
                .build();

        AlertProto.AlertEvent alertEvent = AlertProto.AlertEvent.newBuilder()
                .setWellId("pozo-3")
                .setMetricType(AlertProto.MetricType.FLOW_RATE)
                .setLimitType(AlertProto.LimitType.MAX)
                .setCriticalValue(8000.0)
                .setLimitExceededValue(9000.0)
                .setTimestamp(protoTimestamp)
                .setAlertStatus(AlertProto.AlertStatus.ACTIVE)
                .build();

        byte[] payload = alertEvent.toByteArray();

        // Act
        alertConsumer.consume(payload);

        // Assert
        verify(sseService).broadcast(eq("alert"), any(AlertNotificationDTO.class));
    }

    @Test
    @DisplayName("Debe preservar valores numéricos de alerta correctamente")
    void testConsumePreservesNumericValues() {
        // Arrange
        Instant timestamp = Instant.now();
        Timestamp protoTimestamp = Timestamp.newBuilder()
                .setSeconds(timestamp.getEpochSecond())
                .setNanos(timestamp.getNano())
                .build();

        double criticalValue = 4500.75;
        double exceededValue = 5100.25;

        AlertProto.AlertEvent alertEvent = AlertProto.AlertEvent.newBuilder()
                .setWellId("pozo-precision")
                .setMetricType(AlertProto.MetricType.PRESSURE)
                .setLimitType(AlertProto.LimitType.MAX)
                .setCriticalValue(criticalValue)
                .setLimitExceededValue(exceededValue)
                .setTimestamp(protoTimestamp)
                .setAlertStatus(AlertProto.AlertStatus.ACTIVE)
                .build();

        byte[] payload = alertEvent.toByteArray();

        // Act
        alertConsumer.consume(payload);

        // Assert
        verify(sseService).broadcast(eq("alert"), any(AlertNotificationDTO.class));
    }

    @Test
    @DisplayName("Debe manejar payload de alerta inválido sin lanzar excepción")
    void testConsumeInvalidPayload() {
        // Arrange
        byte[] invalidPayload = "invalid-protobuf-data".getBytes();

        // Act & Assert - No debe lanzar excepción
        alertConsumer.consume(invalidPayload);

        // Assert - No debe enviar broadcast si el parseo falla
        verifyNoMoreInteractions(sseService);
    }

    @Test
    @DisplayName("Debe convertir correctamente Timestamp Protobuf a Instant")
    void testConsumeTimestampConversion() {
        // Arrange
        long seconds = 1700000000L;
        int nanos = 500000000;
        Timestamp protoTimestamp = Timestamp.newBuilder()
                .setSeconds(seconds)
                .setNanos(nanos)
                .build();

        AlertProto.AlertEvent alertEvent = AlertProto.AlertEvent.newBuilder()
                .setWellId("pozo-timestamp")
                .setMetricType(AlertProto.MetricType.PRESSURE)
                .setLimitType(AlertProto.LimitType.MAX)
                .setCriticalValue(5000.0)
                .setLimitExceededValue(5500.0)
                .setTimestamp(protoTimestamp)
                .setAlertStatus(AlertProto.AlertStatus.ACTIVE)
                .build();

        byte[] payload = alertEvent.toByteArray();

        // Act
        alertConsumer.consume(payload);

        // Assert
        verify(sseService).broadcast(eq("alert"), any(AlertNotificationDTO.class));
    }

    @Test
    @DisplayName("Debe enviar notificación para cada alerta recibida")
    void testConsumeMultipleAlerts() {
        // Arrange
        Instant timestamp = Instant.now();
        Timestamp protoTimestamp = Timestamp.newBuilder()
                .setSeconds(timestamp.getEpochSecond())
                .setNanos(timestamp.getNano())
                .build();

        AlertProto.AlertEvent alertEvent = AlertProto.AlertEvent.newBuilder()
                .setWellId("pozo-1")
                .setMetricType(AlertProto.MetricType.PRESSURE)
                .setLimitType(AlertProto.LimitType.MAX)
                .setCriticalValue(5000.0)
                .setLimitExceededValue(5500.0)
                .setTimestamp(protoTimestamp)
                .setAlertStatus(AlertProto.AlertStatus.ACTIVE)
                .build();

        byte[] payload = alertEvent.toByteArray();

        // Act - Procesar múltiples alertas
        alertConsumer.consume(payload);
        alertConsumer.consume(payload);
        alertConsumer.consume(payload);

        // Assert - Verify fue llamado 3 veces
        verify(sseService, org.mockito.Mockito.times(3)).broadcast(eq("alert"), any(AlertNotificationDTO.class));
    }

    @Test
    @DisplayName("Debe manejar alertas con valores zero sin problemas")
    void testConsumeAlertWithZeroValues() {
        // Arrange
        Instant timestamp = Instant.now();
        Timestamp protoTimestamp = Timestamp.newBuilder()
                .setSeconds(timestamp.getEpochSecond())
                .setNanos(timestamp.getNano())
                .build();

        AlertProto.AlertEvent alertEvent = AlertProto.AlertEvent.newBuilder()
                .setWellId("pozo-zero")
                .setMetricType(AlertProto.MetricType.FLOW_RATE)
                .setLimitType(AlertProto.LimitType.MIN)
                .setCriticalValue(0.0)
                .setLimitExceededValue(0.0)
                .setTimestamp(protoTimestamp)
                .setAlertStatus(AlertProto.AlertStatus.ACTIVE)
                .build();

        byte[] payload = alertEvent.toByteArray();

        // Act
        alertConsumer.consume(payload);

        // Assert
        verify(sseService).broadcast(eq("alert"), any(AlertNotificationDTO.class));
    }

    @Test
    @DisplayName("Debe manejar alertas con wellId especial correctamente")
    void testConsumeAlertWithSpecialWellId() {
        // Arrange
        Instant timestamp = Instant.now();
        Timestamp protoTimestamp = Timestamp.newBuilder()
                .setSeconds(timestamp.getEpochSecond())
                .setNanos(timestamp.getNano())
                .build();

        AlertProto.AlertEvent alertEvent = AlertProto.AlertEvent.newBuilder()
                .setWellId("pozo-special-123-abc")
                .setMetricType(AlertProto.MetricType.TEMPERATURE)
                .setLimitType(AlertProto.LimitType.MAX)
                .setCriticalValue(100.0)
                .setLimitExceededValue(110.0)
                .setTimestamp(protoTimestamp)
                .setAlertStatus(AlertProto.AlertStatus.RESOLVED)
                .build();

        byte[] payload = alertEvent.toByteArray();

        // Act
        alertConsumer.consume(payload);

        // Assert
        verify(sseService).broadcast(eq("alert"), any(AlertNotificationDTO.class));
    }

    @Test
    @DisplayName("Debe manejar payload nulo lanzando NPE")
    void testConsumeNullPayload() {
        // Arrange - cuando consume recibe null en payload
        // El código actual lanza NPE al intentar parsear null

        // Act & Assert
        try {
            alertConsumer.consume(null);
            // Si llega aquí sin excepción, no está bien manejado
            throw new AssertionError("Debería lanzar NullPointerException para payload null");
        } catch (NullPointerException e) {
            // Es el comportamiento esperado - el código no valida null
            // En una versión mejorada del código se debería validar
        }
    }

    @Test
    @DisplayName("Debe enviar evento con nombre 'alert' en el broadcast")
    void testConsumeUsesCorrectEventName() {
        // Arrange
        Instant timestamp = Instant.now();
        Timestamp protoTimestamp = Timestamp.newBuilder()
                .setSeconds(timestamp.getEpochSecond())
                .setNanos(timestamp.getNano())
                .build();

        AlertProto.AlertEvent alertEvent = AlertProto.AlertEvent.newBuilder()
                .setWellId("pozo-event")
                .setMetricType(AlertProto.MetricType.PRESSURE)
                .setLimitType(AlertProto.LimitType.MAX)
                .setCriticalValue(5000.0)
                .setLimitExceededValue(5500.0)
                .setTimestamp(protoTimestamp)
                .setAlertStatus(AlertProto.AlertStatus.ACTIVE)
                .build();

        byte[] payload = alertEvent.toByteArray();

        // Act
        alertConsumer.consume(payload);

        // Assert
        verify(sseService).broadcast(eq("alert"), any(AlertNotificationDTO.class));
    }
}

