package com.agonzalorena.msvc.simulator.model;

import com.agonzalorena.msvc.simulator.presentation.dto.SensorDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("WellTest - Pruebas unitarias para la clase Well")
class WellTest {

    private Well well;

    @BeforeEach
    void setup() {
        well = new Well("test-well", 3000.0, 80.0, 2000.0);
    }

    @Test
    @DisplayName("Debe inicializar correctamente un pozo con valores base")
    void testWellInitialization() {
        assertThat(well.getWellId()).isEqualTo("test-well");
        assertThat(well.getBasePressure()).isEqualTo(3000.0);
        assertThat(well.getBaseTemp()).isEqualTo(80.0);
        assertThat(well.getBaseFlow()).isEqualTo(2000.0);
        assertThat(well.getPressureMultiplier()).isEqualTo(1.0);
        assertThat(well.getTemperatureMultiplier()).isEqualTo(1.0);
        assertThat(well.getFlowMultiplier()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Debe generar datos de sensor con timestamp actual")
    void testGenerateDataWithTimestamp() {
        SensorDTO sensorData = well.generateData();

        assertThat(sensorData).isNotNull();
        assertThat(sensorData.wellId()).isEqualTo("test-well");
        assertThat(sensorData.timestamp()).isNotNull();
        assertThat(sensorData.pressurePsi()).isNotNull();
        assertThat(sensorData.temperatureC()).isNotNull();
        assertThat(sensorData.flowRateBpd()).isNotNull();
    }

    @Test
    @DisplayName("Debe aplicar multiplicador de presión correctamente")
    void testPressureMultiplierApplication() {
        well.setPressureMultiplier(2.0);

        SensorDTO sensorData = well.generateData();

        // El valor debería estar aproximadamente entre base * 2 ± cambio aleatorio
        assertThat(sensorData.pressurePsi()).isGreaterThan(3000.0 * 1.5);
        assertThat(sensorData.pressurePsi()).isLessThan(3000.0 * 2.5);
    }

    @Test
    @DisplayName("Debe aplicar multiplicador de temperatura correctamente")
    void testTemperatureMultiplierApplication() {
        well.setTemperatureMultiplier(1.5);

        SensorDTO sensorData = well.generateData();

        assertThat(sensorData.temperatureC()).isGreaterThan(80.0 * 1.0);
        assertThat(sensorData.temperatureC()).isLessThan(80.0 * 2.0);
    }

    @Test
    @DisplayName("Debe aplicar multiplicador de flujo correctamente")
    void testFlowMultiplierApplication() {
        well.setFlowMultiplier(0.5);

        SensorDTO sensorData = well.generateData();

        assertThat(sensorData.flowRateBpd()).isGreaterThan(2000.0 * 0.1);
        assertThat(sensorData.flowRateBpd()).isLessThan(2000.0 * 1.0);
    }

    @Test
    @DisplayName("Debe aplicar todos los multiplicadores simultáneamente")
    void testMultipleMultipliersApplication() {
        well.setPressureMultiplier(2.0);
        well.setTemperatureMultiplier(1.5);
        well.setFlowMultiplier(0.5);

        SensorDTO sensorData = well.generateData();

        assertThat(sensorData.pressurePsi()).isNotNull();
        assertThat(sensorData.temperatureC()).isNotNull();
        assertThat(sensorData.flowRateBpd()).isNotNull();
    }

    @Test
    @DisplayName("Debe redondear valores a 2 decimales")
    void testRoundingTo2Decimals() {
        // Forzamos valores que generen decimales
        well.setCurrentPressure(3000.123456);
        well.setCurrentTemp(80.987654);
        well.setCurrentFlow(2000.555555);

        SensorDTO sensorData = well.generateData();

        // Verificamos que los valores se redondeen a máximo 2 decimales
        String pressureStr = String.valueOf(sensorData.pressurePsi());
        String tempStr = String.valueOf(sensorData.temperatureC());
        String flowStr = String.valueOf(sensorData.flowRateBpd());

        assertThat(pressureStr.split("\\.")[1].length()).isLessThanOrEqualTo(2);
        assertThat(tempStr.split("\\.")[1].length()).isLessThanOrEqualTo(2);
        assertThat(flowStr.split("\\.")[1].length()).isLessThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Debe resetear correctamente todos los multiplicadores y valores")
    void testResetMultipliers() {
        // Modificamos los multiplicadores
        well.setPressureMultiplier(2.0);
        well.setTemperatureMultiplier(1.5);
        well.setFlowMultiplier(0.5);
        well.generateData(); // Modificamos los valores actuales

        // Reseteamos
        well.reset();

        assertThat(well.getCurrentPressure()).isEqualTo(well.getBasePressure());
        assertThat(well.getCurrentTemp()).isEqualTo(well.getBaseTemp());
        assertThat(well.getCurrentFlow()).isEqualTo(well.getBaseFlow());
        assertThat(well.getPressureMultiplier()).isEqualTo(1.0);
        assertThat(well.getTemperatureMultiplier()).isEqualTo(1.0);
        assertThat(well.getFlowMultiplier()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Debe generar datos diferentes en cada llamada (cambio aleatorio)")
    void testGenerateDataRandomVariation() {
        SensorDTO data1 = well.generateData();
        SensorDTO data2 = well.generateData();

        // Los valores no deben ser idénticos debido a la variación aleatoria
        assertThat(data1.pressurePsi())
            .as("Las presiones generadas deben ser diferentes")
            .isNotEqualTo(data2.pressurePsi());
    }

    @Test
    @DisplayName("Debe inicializar valores actuales con valores base")
    void testInitialCurrentValuesEqualBase() {
        assertThat(well.getCurrentPressure()).isEqualTo(well.getBasePressure());
        assertThat(well.getCurrentTemp()).isEqualTo(well.getBaseTemp());
        assertThat(well.getCurrentFlow()).isEqualTo(well.getBaseFlow());
    }
}

