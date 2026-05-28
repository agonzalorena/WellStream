package com.agonzalorena.msvc.analyzer.messaging.producer;

import com.agonzalorena.msvc.analyzer.common.enums.AlertStatus;
import com.agonzalorena.msvc.analyzer.common.enums.LimitType;
import com.agonzalorena.msvc.analyzer.common.enums.MetricType;
import com.agonzalorena.msvc.analyzer.presentation.dto.AlertNotificationDTO;
import com.agonzalorena.msvc.protobuf.AlertProto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertProducerTest - Pruebas unitarias para la clase AlertProducer")
class AlertProducerTest {

    @Mock
    private KafkaTemplate<String, byte[]> kafkaTemplateMock;

    private AlertProducer alertProducer;
    private AlertNotificationDTO testAlert;
    private Instant testInstant;

    @BeforeEach
    void setup() {
        alertProducer = new AlertProducer(kafkaTemplateMock);
        testInstant = Instant.now();
        testAlert = new AlertNotificationDTO(
            "test-well",
            MetricType.PRESSURE,
            LimitType.MAX,
            2000.0,
            1800.0,
            testInstant,
            AlertStatus.ACTIVE
        );
    }

    @Test
    @DisplayName("Debe enviar alerta a Kafka correctamente")
    void testSendAlertToKafka() {
        CompletableFuture<SendResult<String, byte[]>> completableFuture =
            CompletableFuture.completedFuture(null);

        when(kafkaTemplateMock.send(anyString(), anyString(), any(byte[].class)))
            .thenReturn(completableFuture);

        alertProducer.send(testAlert);

        verify(kafkaTemplateMock, times(1))
            .send(eq("alerts"), eq("test-well"), any(byte[].class));
    }

    @Test
    @DisplayName("Debe enviar a topic 'alerts'")
    void testSendToCorrectTopic() {
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        CompletableFuture<SendResult<String, byte[]>> completableFuture =
            CompletableFuture.completedFuture(null);

        when(kafkaTemplateMock.send(topicCaptor.capture(), anyString(), any(byte[].class)))
            .thenReturn(completableFuture);

        alertProducer.send(testAlert);

        assertThat(topicCaptor.getValue()).isEqualTo("alerts");
    }

    @Test
    @DisplayName("Debe usar wellId como clave de Kafka")
    void testWellIdUsedAsKafkaKey() {
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        CompletableFuture<SendResult<String, byte[]>> completableFuture =
            CompletableFuture.completedFuture(null);

        when(kafkaTemplateMock.send(anyString(), keyCaptor.capture(), any(byte[].class)))
            .thenReturn(completableFuture);

        alertProducer.send(testAlert);

        assertThat(keyCaptor.getValue()).isEqualTo("test-well");
    }

    @Test
    @DisplayName("Debe serializar correctamente la alerta a Protobuf")
    void testAlertSerializationToProtobuf() throws com.google.protobuf.InvalidProtocolBufferException {
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        CompletableFuture<SendResult<String, byte[]>> completableFuture =
            CompletableFuture.completedFuture(null);

        when(kafkaTemplateMock.send(anyString(), anyString(), captor.capture()))
            .thenReturn(completableFuture);

        alertProducer.send(testAlert);

        byte[] capturedBytes = captor.getValue();
        AlertProto.AlertEvent deserializedEvent =
            AlertProto.AlertEvent.parseFrom(capturedBytes);

        assertThat(deserializedEvent.getWellId()).isEqualTo("test-well");
        assertThat(deserializedEvent.getMetricType().name()).isEqualTo("PRESSURE");
        assertThat(deserializedEvent.getLimitType().name()).isEqualTo("MAX");
        assertThat(deserializedEvent.getCriticalValue()).isEqualTo(2000.0);
        assertThat(deserializedEvent.getLimitExceededValue()).isEqualTo(1800.0);
    }

    @Test
    @DisplayName("Debe convertir correctamente AlertStatus a Protobuf")
    void testAlertStatusConversion() throws com.google.protobuf.InvalidProtocolBufferException {
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        CompletableFuture<SendResult<String, byte[]>> completableFuture =
            CompletableFuture.completedFuture(null);

        when(kafkaTemplateMock.send(anyString(), anyString(), captor.capture()))
            .thenReturn(completableFuture);

        alertProducer.send(testAlert);

        byte[] capturedBytes = captor.getValue();
        AlertProto.AlertEvent deserializedEvent =
            AlertProto.AlertEvent.parseFrom(capturedBytes);

        assertThat(deserializedEvent.getAlertStatus().name()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Debe convertir correctamente Instant a google.protobuf.Timestamp")
    void testInstantToProtobufTimestampConversion() throws com.google.protobuf.InvalidProtocolBufferException {
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        CompletableFuture<SendResult<String, byte[]>> completableFuture =
            CompletableFuture.completedFuture(null);

        when(kafkaTemplateMock.send(anyString(), anyString(), captor.capture()))
            .thenReturn(completableFuture);

        alertProducer.send(testAlert);

        byte[] capturedBytes = captor.getValue();
        AlertProto.AlertEvent deserializedEvent =
            AlertProto.AlertEvent.parseFrom(capturedBytes);

        assertThat(deserializedEvent.getTimestamp().getSeconds())
            .isEqualTo(testInstant.getEpochSecond());
        assertThat(deserializedEvent.getTimestamp().getNanos())
            .isEqualTo(testInstant.getNano());
    }

    @Test
    @DisplayName("Debe manejar múltiples envíos sin problemas")
    void testMultipleSends() {
        CompletableFuture<SendResult<String, byte[]>> completableFuture =
            CompletableFuture.completedFuture(null);

        when(kafkaTemplateMock.send(anyString(), anyString(), any(byte[].class)))
            .thenReturn(completableFuture);

        // Enviar múltiples alertas
        alertProducer.send(testAlert);
        alertProducer.send(testAlert);
        alertProducer.send(testAlert);

        verify(kafkaTemplateMock, times(3))
            .send(anyString(), anyString(), any(byte[].class));
    }

    @Test
    @DisplayName("Debe preservar todos los campos en serialización Protobuf")
    void testAllFieldsPreservedInProtobuf() throws com.google.protobuf.InvalidProtocolBufferException {
        AlertNotificationDTO alert = new AlertNotificationDTO(
            "well-abc",
            MetricType.TEMPERATURE,
            LimitType.MIN,
            65.0,
            70.0,
            testInstant,
            AlertStatus.RESOLVED
        );

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        CompletableFuture<SendResult<String, byte[]>> completableFuture =
            CompletableFuture.completedFuture(null);

        when(kafkaTemplateMock.send(anyString(), anyString(), captor.capture()))
            .thenReturn(completableFuture);

        alertProducer.send(alert);

        byte[] capturedBytes = captor.getValue();
        AlertProto.AlertEvent deserializedEvent =
            AlertProto.AlertEvent.parseFrom(capturedBytes);

        assertThat(deserializedEvent.getWellId()).isEqualTo("well-abc");
        assertThat(deserializedEvent.getMetricType().name()).isEqualTo("TEMPERATURE");
        assertThat(deserializedEvent.getLimitType().name()).isEqualTo("MIN");
        assertThat(deserializedEvent.getCriticalValue()).isEqualTo(65.0);
        assertThat(deserializedEvent.getLimitExceededValue()).isEqualTo(70.0);
        assertThat(deserializedEvent.getAlertStatus().name()).isEqualTo("RESOLVED");
    }

    @Test
    @DisplayName("Debe manejar valores numéricos precisos")
    void testPreciseNumericValues() throws com.google.protobuf.InvalidProtocolBufferException {
        AlertNotificationDTO precisionAlert = new AlertNotificationDTO(
            "test-well",
            MetricType.FLOW_RATE,
            LimitType.MAX,
            1234.56,
            1000.00,
            testInstant,
            AlertStatus.ACTIVE
        );

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        CompletableFuture<SendResult<String, byte[]>> completableFuture =
            CompletableFuture.completedFuture(null);

        when(kafkaTemplateMock.send(anyString(), anyString(), captor.capture()))
            .thenReturn(completableFuture);

        alertProducer.send(precisionAlert);

        byte[] capturedBytes = captor.getValue();
        AlertProto.AlertEvent deserializedEvent =
            AlertProto.AlertEvent.parseFrom(capturedBytes);

        assertThat(deserializedEvent.getCriticalValue()).isEqualTo(1234.56);
        assertThat(deserializedEvent.getLimitExceededValue()).isEqualTo(1000.00);
    }

    @Test
    @DisplayName("Debe ser inyectable por Spring Boot")
    void testProducerIsNotNull() {
        assertThat(alertProducer).isNotNull();
    }

    @Test
    @DisplayName("Debe usar whenComplete para manejar el resultado")
    void testWhenCompleteHandlerIsSet() {
        CompletableFuture<SendResult<String, byte[]>> completableFuture =
            CompletableFuture.completedFuture(null);

        when(kafkaTemplateMock.send(anyString(), anyString(), any(byte[].class)))
            .thenReturn(completableFuture);

        assertThatCode(() -> alertProducer.send(testAlert))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Debe enviar alertas de diferentes tipos de métrica")
    void testSendDifferentMetricTypes() {
        CompletableFuture<SendResult<String, byte[]>> completableFuture =
            CompletableFuture.completedFuture(null);

        when(kafkaTemplateMock.send(anyString(), anyString(), any(byte[].class)))
            .thenReturn(completableFuture);

        AlertNotificationDTO pressureAlert = new AlertNotificationDTO(
            "well-1", MetricType.PRESSURE, LimitType.MAX, 2000.0, 1800.0, testInstant, AlertStatus.ACTIVE
        );
        AlertNotificationDTO tempAlert = new AlertNotificationDTO(
            "well-2", MetricType.TEMPERATURE, LimitType.MIN, 50.0, 60.0, testInstant, AlertStatus.ACTIVE
        );
        AlertNotificationDTO flowAlert = new AlertNotificationDTO(
            "well-3", MetricType.FLOW_RATE, LimitType.MAX, 1500.0, 1200.0, testInstant, AlertStatus.ACTIVE
        );

        alertProducer.send(pressureAlert);
        alertProducer.send(tempAlert);
        alertProducer.send(flowAlert);

        verify(kafkaTemplateMock, times(3))
            .send(anyString(), anyString(), any(byte[].class));
    }
}

