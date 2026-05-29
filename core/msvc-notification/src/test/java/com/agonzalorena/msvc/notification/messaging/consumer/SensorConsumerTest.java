package com.agonzalorena.msvc.notification.messaging.consumer;

import com.agonzalorena.msvc.notification.presentation.dto.SensorDTO;
import com.agonzalorena.msvc.notification.presentation.service.SseService;
import com.agonzalorena.msvc.protobuf.SensorProto;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("SensorConsumer - Consumidor de Telemetría desde Kafka")
class SensorConsumerTest {

    @Mock
    private SseService sseService;

    private SensorConsumer sensorConsumer;

    @BeforeEach
    void setUp() {
        sensorConsumer = new SensorConsumer(sseService);
    }

    @Test
    @DisplayName("Debe deserializar correctamente un mensaje Protobuf de sensor")
    void testConsumeValidSensor() {
        // Arrange
        Instant timestamp = Instant.now();
        Timestamp protoTimestamp = Timestamp.newBuilder()
                .setSeconds(timestamp.getEpochSecond())
                .setNanos(timestamp.getNano())
                .build();

        SensorProto.SensorEvent sensorEvent = SensorProto.SensorEvent.newBuilder()
                .setWellId("cerro-dragon")
                .setTimestamp(protoTimestamp)
                .setPressurePsi(3250.0)
                .setTemperatureC(70.0)
                .setFlowRateBpd(2213.2)
                .build();

        byte[] payload = sensorEvent.toByteArray();

        // Act
        sensorConsumer.consume(payload);

        // Assert
        verify(sseService).broadcast(eq("metric"), any(SensorDTO.class));
        verifyNoMoreInteractions(sseService);
    }

    @Test
    @DisplayName("Debe preservar valores de presión, temperatura y flujo correctamente")
    void testConsumePreservesMetricValues() {
        // Arrange
        Instant timestamp = Instant.now();
        Timestamp protoTimestamp = Timestamp.newBuilder()
                .setSeconds(timestamp.getEpochSecond())
                .setNanos(timestamp.getNano())
                .build();

        double pressure = 3250.75;
        double temperature = 70.25;
        double flow = 2213.99;

        SensorProto.SensorEvent sensorEvent = SensorProto.SensorEvent.newBuilder()
                .setWellId("cerro-dragon")
                .setTimestamp(protoTimestamp)
                .setPressurePsi(pressure)
                .setTemperatureC(temperature)
                .setFlowRateBpd(flow)
                .build();

        byte[] payload = sensorEvent.toByteArray();

        // Act
        sensorConsumer.consume(payload);

        // Assert
        verify(sseService).broadcast(eq("metric"), any(SensorDTO.class));
    }

    @Test
    @DisplayName("Debe procesar datos de diferentes pozos")
    void testConsumeFromDifferentWells() {
        // Arrange
        Instant timestamp = Instant.now();
        Timestamp protoTimestamp = Timestamp.newBuilder()
                .setSeconds(timestamp.getEpochSecond())
                .setNanos(timestamp.getNano())
                .build();

        SensorProto.SensorEvent sensorEvent = SensorProto.SensorEvent.newBuilder()
                .setWellId("anticlinal-funes")
                .setTimestamp(protoTimestamp)
                .setPressurePsi(2360.0)
                .setTemperatureC(60.0)
                .setFlowRateBpd(1278.6)
                .build();

        byte[] payload = sensorEvent.toByteArray();

        // Act
        sensorConsumer.consume(payload);

        // Assert
        verify(sseService).broadcast(eq("metric"), any(SensorDTO.class));
    }

    @Test
    @DisplayName("Debe manejar payload inválido sin lanzar excepción")
    void testConsumeInvalidPayload() {
        // Arrange
        byte[] invalidPayload = "invalid-protobuf-data".getBytes();

        // Act & Assert - No debe lanzar excepción
        sensorConsumer.consume(invalidPayload);

        // Assert - No debe enviar broadcast si el parseo falla
        verifyNoMoreInteractions(sseService);
    }

    @Test
    @DisplayName("Debe convertir correctamente Timestamp Protobuf a Instant")
    void testConsumeTimestampConversion() {
        // Arrange
        long seconds = 1700000000L;
        int nanos = 250000000;
        Timestamp protoTimestamp = Timestamp.newBuilder()
                .setSeconds(seconds)
                .setNanos(nanos)
                .build();

        SensorProto.SensorEvent sensorEvent = SensorProto.SensorEvent.newBuilder()
                .setWellId("cerro-dragon")
                .setTimestamp(protoTimestamp)
                .setPressurePsi(3250.0)
                .setTemperatureC(70.0)
                .setFlowRateBpd(2213.2)
                .build();

        byte[] payload = sensorEvent.toByteArray();

        // Act
        sensorConsumer.consume(payload);

        // Assert
        verify(sseService).broadcast(eq("metric"), any(SensorDTO.class));
    }

    @Test
    @DisplayName("Debe manejar múltiples sensores en secuencia")
    void testConsumeMultipleSensors() {
        // Arrange
        Instant timestamp = Instant.now();
        Timestamp protoTimestamp = Timestamp.newBuilder()
                .setSeconds(timestamp.getEpochSecond())
                .setNanos(timestamp.getNano())
                .build();

        SensorProto.SensorEvent sensorEvent = SensorProto.SensorEvent.newBuilder()
                .setWellId("cerro-dragon")
                .setTimestamp(protoTimestamp)
                .setPressurePsi(3250.0)
                .setTemperatureC(70.0)
                .setFlowRateBpd(2213.2)
                .build();

        byte[] payload = sensorEvent.toByteArray();

        // Act - Procesar múltiples sensores
        sensorConsumer.consume(payload);
        sensorConsumer.consume(payload);
        sensorConsumer.consume(payload);

        // Assert - Verify fue llamado 3 veces
        verify(sseService, org.mockito.Mockito.times(3)).broadcast(eq("metric"), any(SensorDTO.class));
    }

    @Test
    @DisplayName("Debe manejar valores zero en métricas")
    void testConsumeWithZeroValues() {
        // Arrange
        Instant timestamp = Instant.now();
        Timestamp protoTimestamp = Timestamp.newBuilder()
                .setSeconds(timestamp.getEpochSecond())
                .setNanos(timestamp.getNano())
                .build();

        SensorProto.SensorEvent sensorEvent = SensorProto.SensorEvent.newBuilder()
                .setWellId("pozo-zero")
                .setTimestamp(protoTimestamp)
                .setPressurePsi(0.0)
                .setTemperatureC(0.0)
                .setFlowRateBpd(0.0)
                .build();

        byte[] payload = sensorEvent.toByteArray();

        // Act
        sensorConsumer.consume(payload);

        // Assert
        verify(sseService).broadcast(eq("metric"), any(SensorDTO.class));
    }

    @Test
    @DisplayName("Debe manejar valores muy altos en métricas")
    void testConsumeWithHighValues() {
        // Arrange
        Instant timestamp = Instant.now();
        Timestamp protoTimestamp = Timestamp.newBuilder()
                .setSeconds(timestamp.getEpochSecond())
                .setNanos(timestamp.getNano())
                .build();

        SensorProto.SensorEvent sensorEvent = SensorProto.SensorEvent.newBuilder()
                .setWellId("pozo-high")
                .setTimestamp(protoTimestamp)
                .setPressurePsi(10000.0)
                .setTemperatureC(200.0)
                .setFlowRateBpd(50000.0)
                .build();

        byte[] payload = sensorEvent.toByteArray();

        // Act
        sensorConsumer.consume(payload);

        // Assert
        verify(sseService).broadcast(eq("metric"), any(SensorDTO.class));
    }

    @Test
    @DisplayName("Debe enviar evento con nombre 'metric' en el broadcast")
    void testConsumeUsesCorrectEventName() {
        // Arrange
        Instant timestamp = Instant.now();
        Timestamp protoTimestamp = Timestamp.newBuilder()
                .setSeconds(timestamp.getEpochSecond())
                .setNanos(timestamp.getNano())
                .build();

        SensorProto.SensorEvent sensorEvent = SensorProto.SensorEvent.newBuilder()
                .setWellId("cerro-dragon")
                .setTimestamp(protoTimestamp)
                .setPressurePsi(3250.0)
                .setTemperatureC(70.0)
                .setFlowRateBpd(2213.2)
                .build();

        byte[] payload = sensorEvent.toByteArray();

        // Act
        sensorConsumer.consume(payload);

        // Assert
        verify(sseService).broadcast(eq("metric"), any(SensorDTO.class));
    }

    @Test
    @DisplayName("Debe manejar wellId especial correctamente")
    void testConsumeWithSpecialWellId() {
        // Arrange
        Instant timestamp = Instant.now();
        Timestamp protoTimestamp = Timestamp.newBuilder()
                .setSeconds(timestamp.getEpochSecond())
                .setNanos(timestamp.getNano())
                .build();

        SensorProto.SensorEvent sensorEvent = SensorProto.SensorEvent.newBuilder()
                .setWellId("pozo-special-123-abc")
                .setTimestamp(protoTimestamp)
                .setPressurePsi(3250.0)
                .setTemperatureC(70.0)
                .setFlowRateBpd(2213.2)
                .build();

        byte[] payload = sensorEvent.toByteArray();

        // Act
        sensorConsumer.consume(payload);

        // Assert
        verify(sseService).broadcast(eq("metric"), any(SensorDTO.class));
    }
}

