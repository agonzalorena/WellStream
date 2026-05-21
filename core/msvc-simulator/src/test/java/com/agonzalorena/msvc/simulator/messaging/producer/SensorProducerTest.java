package com.agonzalorena.msvc.simulator.messaging.producer;

import com.agonzalorena.msvc.simulator.presentation.dto.SensorDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensorProducerTest {

    private SensorProducer sensorProducer;

    @Mock
    private KafkaTemplate<String, byte[]> kafkaTemplateMock;

    @BeforeEach
    void setUp() {
        sensorProducer = new SensorProducer(kafkaTemplateMock);
        // Inyectar el telemetryTopic usando reflection ya que es privado y anotado con @Value
        ReflectionTestUtils.setField(sensorProducer, "telemetryTopic", "topic-telemetry");
    }

    @Test
    void testSendMessageCallsKafkaTemplate() {
        // Arrange
        String wellId = "Cerro-Dragon-1";
        SensorDTO sensorData = new SensorDTO(wellId, Instant.now(), 3450.0, 60.0, 1013.2);
        when(kafkaTemplateMock.send(anyString(), anyString(), any(byte[].class))).thenReturn(
            CompletableFuture.completedFuture(null)
        );

        // Act
        sensorProducer.sendMessage(wellId, sensorData);

        // Assert
        verify(kafkaTemplateMock).send(anyString(), eq(wellId), any(byte[].class));
    }

    @Test
    void testSendMessageUsesCorrectTopic() {
        // Arrange
        String wellId = "Cerro-Dragon-1";
        SensorDTO sensorData = new SensorDTO(wellId, Instant.now(), 3450.0, 60.0, 1013.2);
        when(kafkaTemplateMock.send(anyString(), anyString(), any(byte[].class))).thenReturn(
            CompletableFuture.completedFuture(null)
        );

        // Act
        sensorProducer.sendMessage(wellId, sensorData);

        // Assert
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplateMock).send(topicCaptor.capture(), anyString(), any(byte[].class));
        assertEquals("topic-telemetry", topicCaptor.getValue());
    }

    @Test
    void testSendMessageUsesCorrectWellIdAsKey() {
        // Arrange
        String wellId = "Cerro-Dragon-1";
        SensorDTO sensorData = new SensorDTO(wellId, Instant.now(), 3450.0, 60.0, 1013.2);
        when(kafkaTemplateMock.send(anyString(), anyString(), any(byte[].class))).thenReturn(
            CompletableFuture.completedFuture(null)
        );

        // Act
        sensorProducer.sendMessage(wellId, sensorData);

        // Assert
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplateMock).send(anyString(), keyCaptor.capture(), any(byte[].class));
        assertEquals(wellId, keyCaptor.getValue());
    }

    @Test
    void testSendMessageWithDifferentWellIds() {
        // Arrange
        String wellId1 = "Well-A";
        String wellId2 = "Well-B";
        SensorDTO sensorData1 = new SensorDTO(wellId1, Instant.now(), 3450.0, 60.0, 1013.2);
        SensorDTO sensorData2 = new SensorDTO(wellId2, Instant.now(), 3450.0, 60.0, 1013.2);
        when(kafkaTemplateMock.send(anyString(), anyString(), any(byte[].class))).thenReturn(
            CompletableFuture.completedFuture(null)
        );

        // Act
        sensorProducer.sendMessage(wellId1, sensorData1);
        sensorProducer.sendMessage(wellId2, sensorData2);

        // Assert
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplateMock, times(2)).send(anyString(), keyCaptor.capture(), any(byte[].class));

        assertEquals(wellId1, keyCaptor.getAllValues().get(0));
        assertEquals(wellId2, keyCaptor.getAllValues().get(1));
    }

    @Test
    void testSendMessagePassesSensorData() {
        // Arrange
        String wellId = "Cerro-Dragon-1";
        SensorDTO sensorData = new SensorDTO(wellId, Instant.now(), 100.5, 50.0, 1000.0);
        when(kafkaTemplateMock.send(anyString(), anyString(), any(byte[].class))).thenReturn(
            CompletableFuture.completedFuture(null)
        );

        // Act
        sensorProducer.sendMessage(wellId, sensorData);

        // Assert
        verify(kafkaTemplateMock).send(anyString(), anyString(), any(byte[].class));
    }
}