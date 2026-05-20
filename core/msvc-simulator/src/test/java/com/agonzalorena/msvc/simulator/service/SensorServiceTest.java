package com.agonzalorena.msvc.simulator.service;

import com.agonzalorena.msvc.simulator.messaging.producer.SensorProducer;
import com.agonzalorena.msvc.simulator.presentation.dto.SensorDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class SensorServiceTest {

    private SensorService sensorService;

    @Mock
    private SensorProducer sensorProducerMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sensorService = new SensorService(sensorProducerMock);
    }

    @Test
    void testPressureMultiplierIncrease() {
        // Arrange
        double multiplier = 1.3;

        // Act
        sensorService.setPressureMultiplier(multiplier);

        // Assert
        assertEquals(multiplier, sensorService.getMultipliers().pressure());
    }

    @Test
    void testTemperatureMultiplierDecrease() {
        // Arrange
        double multiplier = 0.6;

        // Act
        sensorService.setTemperatureMultiplier(multiplier);

        // Assert
        assertEquals(multiplier, sensorService.getMultipliers().temperature());
    }

    @Test
    void testFlowMultiplierChange() {
        // Arrange
        double multiplier = 1.5;

        // Act
        sensorService.setFlowMultiplier(multiplier);

        // Assert
        assertEquals(multiplier, sensorService.getMultipliers().flow());
    }

    @Test
    void testSetMultiplierThrowsExceptionForZero() {
        // Assert & Act
        assertThrows(IllegalArgumentException.class, () -> {
            sensorService.setPressureMultiplier(0);
        });
    }

    @Test
    void testSetMultiplierThrowsExceptionForNegative() {
        // Assert & Act
        assertThrows(IllegalArgumentException.class, () -> {
            sensorService.setTemperatureMultiplier(-1.0);
        });
    }

    @Test
    void testResetMultipliers() {
        // Arrange
        sensorService.setPressureMultiplier(2.0);
        sensorService.setTemperatureMultiplier(0.5);
        sensorService.setFlowMultiplier(1.5);

        // Act
        sensorService.resetMultipliers();

        // Assert
        SensorService.SensorMultipliers multipliers = sensorService.getMultipliers();
        assertEquals(1.0, multipliers.pressure());
        assertEquals(1.0, multipliers.temperature());
        assertEquals(1.0, multipliers.flow());
    }

    @Test
    void testSendSensorDataCallsProducer() {
        // Act
        sensorService.sendSensorData();

        // Assert
        verify(sensorProducerMock).sendMessage(anyString(), any(SensorDTO.class));
    }

    @Test
    void testSendSensorDataWithMultiplier() {
        // Arrange
        sensorService.setPressureMultiplier(2.0);

        // Act
        sensorService.sendSensorData();

        // Assert
        verify(sensorProducerMock).sendMessage(anyString(), any(SensorDTO.class));
    }

    @Test
    void testMultiplierIsAppliedToValues() {
        // Arrange
        sensorService.setPressureMultiplier(1.0); // Control value

        // Act
        sensorService.sendSensorData();

        // Assert - Verify producer was called
        verify(sensorProducerMock).sendMessage(anyString(), any(SensorDTO.class));
    }

    @Test
    void testGetMultipliersReturnsCorrectValues() {
        // Arrange
        double pressure = 1.5;
        double temperature = 0.8;
        double flow = 1.2;

        sensorService.setPressureMultiplier(pressure);
        sensorService.setTemperatureMultiplier(temperature);
        sensorService.setFlowMultiplier(flow);

        // Act
        SensorService.SensorMultipliers multipliers = sensorService.getMultipliers();

        // Assert
        assertEquals(pressure, multipliers.pressure());
        assertEquals(temperature, multipliers.temperature());
        assertEquals(flow, multipliers.flow());
    }
}

