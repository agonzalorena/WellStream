package com.agonzalorena.msvc.simulator.messaging.producer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SensorProducerTest {

    private SensorProducer sensorProducer;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplateMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sensorProducer = new SensorProducer(kafkaTemplateMock);
    }

    @Test
    void testSendMessageCallsKafkaTemplate() {
        // Arrange
        String wellId = "Cerro-Dragon-1";
        Object sensorData = new Object();
        when(kafkaTemplateMock.send(anyString(), anyString(), any())).thenReturn(
            CompletableFuture.completedFuture(null)
        );

        // Act
        sensorProducer.sendMessage(wellId, sensorData);

        // Assert
        verify(kafkaTemplateMock).send("topic_telemetry", wellId, sensorData);
    }

    @Test
    void testSendMessageUsesCorrectTopic() {
        // Arrange
        String wellId = "Cerro-Dragon-1";
        Object sensorData = new Object();
        when(kafkaTemplateMock.send(anyString(), anyString(), any())).thenReturn(
            CompletableFuture.completedFuture(null)
        );

        // Act
        sensorProducer.sendMessage(wellId, sensorData);

        // Assert
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplateMock).send(topicCaptor.capture(), anyString(), any());
        assertEquals("topic_telemetry", topicCaptor.getValue());
    }

    @Test
    void testSendMessageUsesCorrectWellIdAsKey() {
        // Arrange
        String wellId = "Cerro-Dragon-1";
        Object sensorData = new Object();
        when(kafkaTemplateMock.send(anyString(), anyString(), any())).thenReturn(
            CompletableFuture.completedFuture(null)
        );

        // Act
        sensorProducer.sendMessage(wellId, sensorData);

        // Assert
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplateMock).send(anyString(), keyCaptor.capture(), any());
        assertEquals(wellId, keyCaptor.getValue());
    }

    @Test
    void testSendMessageWithDifferentWellIds() {
        // Arrange
        String wellId1 = "Well-A";
        String wellId2 = "Well-B";
        Object sensorData = new Object();
        when(kafkaTemplateMock.send(anyString(), anyString(), any())).thenReturn(
            CompletableFuture.completedFuture(null)
        );

        // Act
        sensorProducer.sendMessage(wellId1, sensorData);
        sensorProducer.sendMessage(wellId2, sensorData);

        // Assert
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplateMock, times(2)).send(anyString(), keyCaptor.capture(), any());

        assertEquals(wellId1, keyCaptor.getAllValues().get(0));
        assertEquals(wellId2, keyCaptor.getAllValues().get(1));
    }

    @Test
    void testSendMessagePassesSensorData() {
        // Arrange
        String wellId = "Cerro-Dragon-1";
        Object sensorData = new MockSensorData(100.5, 50.0, 1000.0);
        when(kafkaTemplateMock.send(anyString(), anyString(), any())).thenReturn(
            CompletableFuture.completedFuture(null)
        );

        // Act
        sensorProducer.sendMessage(wellId, sensorData);

        // Assert
        ArgumentCaptor<Object> dataCaptor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplateMock).send(anyString(), anyString(), dataCaptor.capture());
        assertEquals(sensorData, dataCaptor.getValue());
    }

    // Clase auxiliar para testing
    private static class MockSensorData {
        double pressure;
        double temperature;
        double flow;

        MockSensorData(double pressure, double temperature, double flow) {
            this.pressure = pressure;
            this.temperature = temperature;
            this.flow = flow;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MockSensorData that = (MockSensorData) o;
            return Double.compare(that.pressure, pressure) == 0 &&
                    Double.compare(that.temperature, temperature) == 0 &&
                    Double.compare(that.flow, flow) == 0;
        }
    }
}

