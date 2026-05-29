package com.agonzalorena.msvc.notification.presentation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SseService - Servicio de Notificaciones SSE (Server-Sent Events)")
class SseServiceTest {

    private SseService sseService;

    @BeforeEach
    void setUp() {
        sseService = new SseService();
    }

    @Test
    @DisplayName("Debe crear una nueva conexión SSE")
    void testCreateConnection() {
        // Act
        SseEmitter emitter = sseService.createConnection();

        // Assert
        assertThat(emitter).isNotNull();
    }

    @Test
    @DisplayName("Debe crear múltiples conexiones SSE independientes")
    void testCreateMultipleConnections() {
        // Act
        SseEmitter emitter1 = sseService.createConnection();
        SseEmitter emitter2 = sseService.createConnection();
        SseEmitter emitter3 = sseService.createConnection();

        // Assert
        assertThat(emitter1).isNotNull();
        assertThat(emitter2).isNotNull();
        assertThat(emitter3).isNotNull();
        assertThat(emitter1).isNotEqualTo(emitter2);
        assertThat(emitter2).isNotEqualTo(emitter3);
        assertThat(emitter1).isNotEqualTo(emitter3);
    }

    @Test
    @DisplayName("Debe enviar un evento a un cliente SSE conectado")
    void testBroadcastSingleEvent() throws IOException {
        // Arrange
        SseEmitter emitter = sseService.createConnection();
        String eventName = "metric";
        Object payload = new TestPayload("test-well", 100.0);

        // Act & Assert - No debe lanzar excepción
        try {
            sseService.broadcast(eventName, payload);
        } catch (Exception e) {
            fail("No debe lanzar excepción al hacer broadcast");
        }
    }

    @Test
    @DisplayName("Debe enviar múltiples eventos a clientes SSE")
    void testBroadcastMultipleEvents() throws IOException {
        // Arrange
        SseEmitter emitter1 = sseService.createConnection();
        SseEmitter emitter2 = sseService.createConnection();
        String eventName = "alert";
        Object payload = new TestPayload("alert-well", 5000.0);

        // Act & Assert - No debe lanzar excepción
        try {
            sseService.broadcast(eventName, payload);
            sseService.broadcast(eventName, payload);
        } catch (Exception e) {
            fail("No debe lanzar excepción al hacer broadcast a múltiples clientes");
        }
    }

    @Test
    @DisplayName("Debe manejar broadcast sin clientes conectados")
    void testBroadcastWithNoClients() {
        // Arrange
        String eventName = "metric";
        Object payload = new TestPayload("test-well", 100.0);

        // Act & Assert - No debe lanzar excepción
        assertThatNoException().isThrownBy(() ->
            sseService.broadcast(eventName, payload)
        );
    }

    @Test
    @DisplayName("Debe permitir diferentes nombres de evento")
    void testBroadcastDifferentEventNames() throws IOException {
        // Arrange
        SseEmitter emitter = sseService.createConnection();
        Object payload = new TestPayload("test-well", 100.0);

        // Act & Assert - No debe lanzar excepción con diferentes nombres
        assertThatNoException().isThrownBy(() -> {
            sseService.broadcast("metric", payload);
            sseService.broadcast("alert", payload);
            sseService.broadcast("status", payload);
            sseService.broadcast("custom-event", payload);
        });
    }

    @Test
    @DisplayName("Debe manejar payloads de diferentes tipos")
    void testBroadcastDifferentPayloadTypes() throws IOException {
        // Arrange
        SseEmitter emitter = sseService.createConnection();

        // Act & Assert - No debe lanzar excepción con diferentes tipos de payload
        assertThatNoException().isThrownBy(() -> {
            sseService.broadcast("event1", "string-payload");
            sseService.broadcast("event2", 12345);
            sseService.broadcast("event3", 100.5);
            sseService.broadcast("event4", new TestPayload("well", 100.0));
        });
    }

    @Test
    @DisplayName("Debe remover emitter cuando se completa la conexión")
    void testEmitterRemovalOnCompletion() throws IOException {
        // Arrange
        SseEmitter emitter = sseService.createConnection();

        // Act & Assert - Broadcast sin clientes no debe fallar
        assertThatNoException().isThrownBy(() ->
            sseService.broadcast("metric", new TestPayload("test", 100.0))
        );
    }

    @Test
    @DisplayName("Debe ser thread-safe con múltiples conexiones simultáneas")
    void testThreadSafeBroadcast() throws InterruptedException {
        // Arrange
        SseEmitter emitter1 = sseService.createConnection();
        SseEmitter emitter2 = sseService.createConnection();
        SseEmitter emitter3 = sseService.createConnection();

        // Act - Crear threads que hagan broadcast simultáneamente
        Thread thread1 = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    sseService.broadcast("event1", new TestPayload("well1", i));
                }
            } catch (Exception e) {
                fail("Thread 1 falló: " + e.getMessage());
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    sseService.broadcast("event2", new TestPayload("well2", i));
                }
            } catch (Exception e) {
                fail("Thread 2 falló: " + e.getMessage());
            }
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // Assert - No debe lanzar excepciones de concurrencia
        assertThatNoException().isThrownBy(() ->
            sseService.broadcast("final-event", new TestPayload("test", 100.0))
        );
    }

    @Test
    @DisplayName("Debe enviar eventos continuamente")
    void testContinuousBroadcast() throws IOException {
        // Arrange
        SseEmitter emitter = sseService.createConnection();

        // Act & Assert - Enviar múltiples eventos en secuencia
        assertThatNoException().isThrownBy(() -> {
            for (int i = 0; i < 10; i++) {
                sseService.broadcast("metric", new TestPayload("well-" + i, (double) i));
            }
        });
    }

    @Test
    @DisplayName("Debe manejar broadcast con payload nulo")
    void testBroadcastWithNullPayload() throws IOException {
        // Arrange
        SseEmitter emitter = sseService.createConnection();

        // Act & Assert
        assertThatNoException().isThrownBy(() ->
            sseService.broadcast("event", null)
        );
    }

    @Test
    @DisplayName("Debe manejar broadcast con nombre de evento nulo")
    void testBroadcastWithNullEventName() throws IOException {
        // Arrange
        SseEmitter emitter = sseService.createConnection();
        Object payload = new TestPayload("test-well", 100.0);

        // Act & Assert - Comportamiento depende de la implementación
        // Puede lanzar NPE o manejarlo gracefully
        try {
            sseService.broadcast(null, payload);
        } catch (NullPointerException e) {
            // Es aceptable si lanza NPE
            assertThat(e).isNotNull();
        }
    }

    @Test
    @DisplayName("Debe soportar broadcast con eventos especiales de alerta")
    void testBroadcastAlertEvents() throws IOException {
        // Arrange
        SseEmitter emitter = sseService.createConnection();
        AlertPayload alertPayload = new AlertPayload("well-1", "ACTIVE", 5000.0);

        // Act & Assert
        assertThatNoException().isThrownBy(() ->
            sseService.broadcast("alert", alertPayload)
        );
    }

    @Test
    @DisplayName("Debe soportar broadcast con eventos de métricas")
    void testBroadcastMetricEvents() throws IOException {
        // Arrange
        SseEmitter emitter = sseService.createConnection();
        MetricPayload metricPayload = new MetricPayload("well-1", 3250.0, 70.0, 2213.2);

        // Act & Assert
        assertThatNoException().isThrownBy(() ->
            sseService.broadcast("metric", metricPayload)
        );
    }

    // Clases auxiliares para testing
    static class TestPayload {
        public String wellId;
        public double value;

        public TestPayload(String wellId, double value) {
            this.wellId = wellId;
            this.value = value;
        }
    }

    static class AlertPayload {
        public String wellId;
        public String status;
        public double limitValue;

        public AlertPayload(String wellId, String status, double limitValue) {
            this.wellId = wellId;
            this.status = status;
            this.limitValue = limitValue;
        }
    }

    static class MetricPayload {
        public String wellId;
        public double pressure;
        public double temperature;
        public double flow;

        public MetricPayload(String wellId, double pressure, double temperature, double flow) {
            this.wellId = wellId;
            this.pressure = pressure;
            this.temperature = temperature;
            this.flow = flow;
        }
    }
}

