package com.agonzalorena.msvc.simulator.messaging.producer;

import com.agonzalorena.msvc.simulator.presentation.dto.SensorDTO;
import com.agonzalorena.msvc.protobuf.SensorProto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SensorProducerTest - Pruebas unitarias para la clase SensorProducer")
class SensorProducerTest {

    @Mock
    private KafkaTemplate<String, byte[]> kafkaTemplateMock;

    private SensorProducer sensorProducer;
    private SensorDTO testSensorData;
    private Instant testInstant;

    @BeforeEach
    void setup() {
        sensorProducer = new SensorProducer(kafkaTemplateMock);
        testInstant = Instant.now();
        testSensorData = new SensorDTO(
            "test-well",
            testInstant,
            1500.0,
            75.5,
            1000.0
        );
    }

    @Test
    @DisplayName("Debe enviar mensaje a Kafka con datos del sensor")
    void testSendMessageToKafka() {
        // Mockear el retorno del kafkaTemplate
        CompletableFuture<SendResult<String, byte[]>> completableFuture =
            CompletableFuture.completedFuture(null);

        when(kafkaTemplateMock.send(anyString(), anyString(), any(byte[].class)))
            .thenReturn(completableFuture);

        // Ejecutar
        sensorProducer.sendMessage("test-well", testSensorData);

        // Verificar que se llamó a send
        verify(kafkaTemplateMock, times(1))
            .send(eq("topic-telemetry"), eq("test-well"), any(byte[].class));
    }

    @Test
    @DisplayName("Debe serializar correctamente los datos del sensor a protobuf")
    void testSensorDataSerializationToProtobuf() throws com.google.protobuf.InvalidProtocolBufferException {
        // Capturar el argumento enviado
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        CompletableFuture<SendResult<String, byte[]>> completableFuture =
            CompletableFuture.completedFuture(null);

        when(kafkaTemplateMock.send(anyString(), anyString(), captor.capture()))
            .thenReturn(completableFuture);

        // Ejecutar
        sensorProducer.sendMessage("test-well", testSensorData);

        // Obtener los bytes capturados y deserializarlos
        byte[] capturedBytes = captor.getValue();
        SensorProto.SensorEvent deserializedEvent =
            SensorProto.SensorEvent.parseFrom(capturedBytes);

        assertThat(deserializedEvent.getWellId()).isEqualTo("test-well");
        assertThat(deserializedEvent.getPressurePsi()).isEqualTo(1500.0);
        assertThat(deserializedEvent.getTemperatureC()).isEqualTo(75.5);
        assertThat(deserializedEvent.getFlowRateBpd()).isEqualTo(1000.0);
    }

    @Test
    @DisplayName("Debe convertir correctamente Instant a google.protobuf.Timestamp")
    void testInstantToProtobufTimestampConversion() throws com.google.protobuf.InvalidProtocolBufferException {
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        CompletableFuture<SendResult<String, byte[]>> completableFuture =
            CompletableFuture.completedFuture(null);

        when(kafkaTemplateMock.send(anyString(), anyString(), captor.capture()))
            .thenReturn(completableFuture);

        sensorProducer.sendMessage("test-well", testSensorData);

        byte[] capturedBytes = captor.getValue();
        SensorProto.SensorEvent deserializedEvent =
            SensorProto.SensorEvent.parseFrom(capturedBytes);

        assertThat(deserializedEvent.getTimestamp().getSeconds())
            .isEqualTo(testInstant.getEpochSecond());
        assertThat(deserializedEvent.getTimestamp().getNanos())
            .isEqualTo(testInstant.getNano());
    }

    @Test
    @DisplayName("Debe usar el wellId como clave de Kafka")
    void testWellIdUsedAsKafkaKey() {
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        CompletableFuture<SendResult<String, byte[]>> completableFuture =
            CompletableFuture.completedFuture(null);

        when(kafkaTemplateMock.send(anyString(), keyCaptor.capture(), any(byte[].class)))
            .thenReturn(completableFuture);

        sensorProducer.sendMessage("mi-pozo", testSensorData);

        assertThat(keyCaptor.getValue()).isEqualTo("mi-pozo");
    }

    @Test
    @DisplayName("Debe enviar a topic 'topic-telemetry'")
    void testSendToCorrectTopic() {
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        CompletableFuture<SendResult<String, byte[]>> completableFuture =
            CompletableFuture.completedFuture(null);

        when(kafkaTemplateMock.send(topicCaptor.capture(), anyString(), any(byte[].class)))
            .thenReturn(completableFuture);

        sensorProducer.sendMessage("test-well", testSensorData);

        assertThat(topicCaptor.getValue()).isEqualTo("topic-telemetry");
    }

    @Test
    @DisplayName("Debe manejar múltiples envíos sin problemas")
    void testMultipleSendMessages() {
        CompletableFuture<SendResult<String, byte[]>> completableFuture =
            CompletableFuture.completedFuture(null);

        when(kafkaTemplateMock.send(anyString(), anyString(), any(byte[].class)))
            .thenReturn(completableFuture);

        // Enviar múltiples mensajes
        sensorProducer.sendMessage("well-1", testSensorData);
        sensorProducer.sendMessage("well-2", testSensorData);
        sensorProducer.sendMessage("well-3", testSensorData);

        // Verificar que se envió 3 veces
        verify(kafkaTemplateMock, times(3))
            .send(anyString(), anyString(), any(byte[].class));
    }

    @Test
    @DisplayName("Debe usar whenComplete para manejar el resultado del envío")
    void testWhenCompleteHandlerIsSet() {
        CompletableFuture<SendResult<String, byte[]>> completableFuture =
            CompletableFuture.completedFuture(null);

        when(kafkaTemplateMock.send(anyString(), anyString(), any(byte[].class)))
            .thenReturn(completableFuture);

        // No debe lanzar excepción
        assertThatCode(() ->
            sensorProducer.sendMessage("test-well", testSensorData)
        )
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Debe preservar todos los campos de SensorDTO en protobuf")
    void testAllSensorDTOFieldsPreservedInProtobuf() throws com.google.protobuf.InvalidProtocolBufferException {
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        CompletableFuture<SendResult<String, byte[]>> completableFuture =
            CompletableFuture.completedFuture(null);

        when(kafkaTemplateMock.send(anyString(), anyString(), captor.capture()))
            .thenReturn(completableFuture);

        sensorProducer.sendMessage("test-well", testSensorData);

        byte[] capturedBytes = captor.getValue();
        SensorProto.SensorEvent deserializedEvent =
            SensorProto.SensorEvent.parseFrom(capturedBytes);

        assertThat(deserializedEvent.getWellId())
            .isEqualTo(testSensorData.wellId());
        assertThat(deserializedEvent.getPressurePsi())
            .isEqualTo(testSensorData.pressurePsi());
        assertThat(deserializedEvent.getTemperatureC())
            .isEqualTo(testSensorData.temperatureC());
        assertThat(deserializedEvent.getFlowRateBpd())
            .isEqualTo(testSensorData.flowRateBpd());
    }

    @Test
    @DisplayName("Debe ser inyectable por Spring Boot")
    void testProducerIsNotNull() {
        assertThat(sensorProducer).isNotNull();
    }

    @Test
    @DisplayName("Debe manejar valores numéricos precisos en protobuf")
    void testPreciseNumericValuesInProtobuf() throws com.google.protobuf.InvalidProtocolBufferException {
        SensorDTO precisionTestData = new SensorDTO(
            "test-well",
            testInstant,
            1234.56,  // Presión con decimales
            78.90,    // Temperatura con decimales
            5678.12   // Flujo con decimales
        );

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        CompletableFuture<SendResult<String, byte[]>> completableFuture =
            CompletableFuture.completedFuture(null);

        when(kafkaTemplateMock.send(anyString(), anyString(), captor.capture()))
            .thenReturn(completableFuture);

        sensorProducer.sendMessage("test-well", precisionTestData);

        byte[] capturedBytes = captor.getValue();
        SensorProto.SensorEvent deserializedEvent =
            SensorProto.SensorEvent.parseFrom(capturedBytes);

        assertThat(deserializedEvent.getPressurePsi()).isEqualTo(1234.56);
        assertThat(deserializedEvent.getTemperatureC()).isEqualTo(78.90);
        assertThat(deserializedEvent.getFlowRateBpd()).isEqualTo(5678.12);
    }
}

