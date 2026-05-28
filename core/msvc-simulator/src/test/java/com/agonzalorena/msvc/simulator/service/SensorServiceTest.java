package com.agonzalorena.msvc.simulator.service;

import com.agonzalorena.msvc.simulator.common.enums.MultiplierType;
import com.agonzalorena.msvc.simulator.messaging.producer.SensorProducer;
import com.agonzalorena.msvc.simulator.model.Well;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SensorServiceTest - Pruebas unitarias para la clase SensorService")
class SensorServiceTest {

    @Mock
    private SensorProducer sensorProducerMock;

    private SensorService sensorService;

    @BeforeEach
    void setup() {
        sensorService = new SensorService(sensorProducerMock);
    }

    @Test
    @DisplayName("Debe retornar lista de pozos correctamente inicializados")
    void testGetWellsReturnsInitializedWells() {
        List<Well> wells = sensorService.getWells();

        assertThat(wells)
            .isNotNull()
            .isNotEmpty()
            .hasSize(2);

        assertThat(wells)
            .extracting(Well::getWellId)
            .containsExactlyInAnyOrder("cerro-dragon", "anticlinal-funes");
    }

    @Test
    @DisplayName("Debe retornar pozos con valores base correctos")
    void testGetWellsReturnsCorrectBaseValues() {
        List<Well> wells = sensorService.getWells();

        Well cerroDragon = wells.stream()
            .filter(w -> w.getWellId().equals("cerro-dragon"))
            .findFirst()
            .orElseThrow();

        assertThat(cerroDragon.getBasePressure()).isEqualTo(3250.0);
        assertThat(cerroDragon.getBaseTemp()).isEqualTo(70.0);
        assertThat(cerroDragon.getBaseFlow()).isEqualTo(2213.2);
    }

    @Test
    @DisplayName("Debe establecer multiplicador de presión correctamente")
    void testSetPressureMultiplier() {
        sensorService.setMultiplier("cerro-dragon", MultiplierType.PRESSURE, 2.0);

        Well well = sensorService.getWells().stream()
            .filter(w -> w.getWellId().equals("cerro-dragon"))
            .findFirst()
            .orElseThrow();

        assertThat(well.getPressureMultiplier()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("Debe establecer multiplicador de temperatura correctamente")
    void testSetTemperatureMultiplier() {
        sensorService.setMultiplier("anticlinal-funes", MultiplierType.TEMPERATURE, 1.5);

        Well well = sensorService.getWells().stream()
            .filter(w -> w.getWellId().equals("anticlinal-funes"))
            .findFirst()
            .orElseThrow();

        assertThat(well.getTemperatureMultiplier()).isEqualTo(1.5);
    }

    @Test
    @DisplayName("Debe establecer multiplicador de flujo correctamente")
    void testSetFlowMultiplier() {
        sensorService.setMultiplier("cerro-dragon", MultiplierType.FLOW, 0.5);

        Well well = sensorService.getWells().stream()
            .filter(w -> w.getWellId().equals("cerro-dragon"))
            .findFirst()
            .orElseThrow();

        assertThat(well.getFlowMultiplier()).isEqualTo(0.5);
    }

    @Test
    @DisplayName("Debe lanzar excepción al intentar establecer multiplicador en pozo no existente")
    void testSetMultiplierThrowsExceptionForNonExistentWell() {
        assertThatThrownBy(() ->
            sensorService.setMultiplier("pozo-inexistente", MultiplierType.PRESSURE, 2.0)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Pozo no encontrado");
    }

    @Test
    @DisplayName("Debe lanzar excepción al establecer multiplicador menor o igual a 0")
    void testSetMultiplierThrowsExceptionForNegativeMultiplier() {
        assertThatThrownBy(() ->
            sensorService.setMultiplier("cerro-dragon", MultiplierType.PRESSURE, 0)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("mayor a 0");

        assertThatThrownBy(() ->
            sensorService.setMultiplier("cerro-dragon", MultiplierType.PRESSURE, -1.5)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("mayor a 0");
    }

    @Test
    @DisplayName("Debe permitir multiplicadores mayores a 1")
    void testSetMultiplierAllowsValuesGreaterThanOne() {
        assertThatCode(() ->
            sensorService.setMultiplier("cerro-dragon", MultiplierType.PRESSURE, 5.0)
        )
            .doesNotThrowAnyException();

        Well well = sensorService.getWells().stream()
            .filter(w -> w.getWellId().equals("cerro-dragon"))
            .findFirst()
            .orElseThrow();

        assertThat(well.getPressureMultiplier()).isEqualTo(5.0);
    }

    @Test
    @DisplayName("Debe permitir multiplicadores menores a 1 pero mayores a 0")
    void testSetMultiplierAllowsValuesLessThanOne() {
        assertThatCode(() ->
            sensorService.setMultiplier("cerro-dragon", MultiplierType.PRESSURE, 0.1)
        )
            .doesNotThrowAnyException();

        Well well = sensorService.getWells().stream()
            .filter(w -> w.getWellId().equals("cerro-dragon"))
            .findFirst()
            .orElseThrow();

        assertThat(well.getPressureMultiplier()).isEqualTo(0.1);
    }

    @Test
    @DisplayName("Debe resetear todos los multiplicadores a 1.0 para todos los pozos")
    void testResetMultipliersResetsAllWells() {
        // Establecer diferentes multiplicadores
        sensorService.setMultiplier("cerro-dragon", MultiplierType.PRESSURE, 2.0);
        sensorService.setMultiplier("cerro-dragon", MultiplierType.TEMPERATURE, 1.5);
        sensorService.setMultiplier("anticlinal-funes", MultiplierType.FLOW, 0.5);

        // Resetear
        sensorService.resetMultipliers();

        // Verificar que todos los multiplicadores se han reseteado
        List<Well> wells = sensorService.getWells();
        wells.forEach(well -> {
            assertThat(well.getPressureMultiplier()).isEqualTo(1.0);
            assertThat(well.getTemperatureMultiplier()).isEqualTo(1.0);
            assertThat(well.getFlowMultiplier()).isEqualTo(1.0);
        });
    }

    @Test
    @DisplayName("Debe resetear valores actuales a valores base para todos los pozos")
    void testResetMultipliersResetsCurrentValues() {
        // Generar datos para cambiar valores actuales
        sensorService.getWells().forEach(Well::generateData);

        // Cambiar multiplicadores también
        sensorService.setMultiplier("cerro-dragon", MultiplierType.PRESSURE, 2.0);

        // Resetear
        sensorService.resetMultipliers();

        // Verificar que los valores actuales se han reseteado a base
        List<Well> wells = sensorService.getWells();
        wells.forEach(well -> {
            assertThat(well.getCurrentPressure()).isEqualTo(well.getBasePressure());
            assertThat(well.getCurrentTemp()).isEqualTo(well.getBaseTemp());
            assertThat(well.getCurrentFlow()).isEqualTo(well.getBaseFlow());
        });
    }

    @Test
    @DisplayName("El servicio debe estar integrado correctamente con SensorProducer")
    void testSensorServiceIntegrationWithProducer() {
        assertThat(sensorProducerMock).isNotNull();
        // El producer es inyectado correctamente a través del constructor
        verifyNoInteractions(sensorProducerMock);
    }

    @Test
    @DisplayName("Debe mantener dos pozos en la inicialización")
    void testServiceInitializesWithTwoWells() {
        List<Well> wells = sensorService.getWells();

        assertThat(wells).hasSize(2);
        assertThat(wells.stream().map(Well::getWellId))
            .containsExactlyInAnyOrder("cerro-dragon", "anticlinal-funes");
    }

    @Test
    @DisplayName("Debe permitir establecer multiplicador decimal positivo pequeño")
    void testSetMultiplierAllowsSmallDecimalValues() {
        assertThatCode(() ->
            sensorService.setMultiplier("cerro-dragon", MultiplierType.PRESSURE, 0.001)
        )
            .doesNotThrowAnyException();

        Well well = sensorService.getWells().stream()
            .filter(w -> w.getWellId().equals("cerro-dragon"))
            .findFirst()
            .orElseThrow();

        assertThat(well.getPressureMultiplier()).isEqualTo(0.001);
    }
}

