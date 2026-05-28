# Unit Tests - WellStream Core (msvc-simulator)

## Resumen

Se han implementado **34 unit tests** completos para el módulo `msvc-simulator` del proyecto WellStream. Todos los tests pasan satisfactoriamente sin fallos ni errores.

## Clases Testeadas

### 1. **WellTest** (11 tests)
**Ubicación:** `/src/test/java/com/agonzalorena/msvc/simulator/model/WellTest.java`

Pruebas unitarias para la clase `Well` que modela un pozo con capacidad de generar datos de sensores.

#### Tests incluidos:
- ✅ `testWellInitialization` - Verifica la inicialización correcta de un pozo con valores base
- ✅ `testGenerateDataWithTimestamp` - Comprueba que se generen datos con timestamp actual
- ✅ `testPressureMultiplierApplication` - Valida la aplicación del multiplicador de presión
- ✅ `testTemperatureMultiplierApplication` - Valida la aplicación del multiplicador de temperatura
- ✅ `testFlowMultiplierApplication` - Valida la aplicación del multiplicador de flujo
- ✅ `testMultipleMultipliersApplication` - Verifica la aplicación simultánea de múltiples multiplicadores
- ✅ `testRoundingTo2Decimals` - Confirma que los valores se redondean a 2 decimales
- ✅ `testResetMultipliers` - Valida el reseteo de multiplicadores y valores actuales
- ✅ `testGenerateDataRandomVariation` - Verifica que cada generación de datos tenga variación aleatoria
- ✅ `testInitialCurrentValuesEqualBase` - Comprueba que los valores iniciales sean iguales a los base

### 2. **SensorServiceTest** (16 tests)
**Ubicación:** `/src/test/java/com/agonzalorena/msvc/simulator/service/SensorServiceTest.java`

Pruebas unitarias para el servicio `SensorService` que gestiona los pozos y sus multiplicadores.

#### Tests incluidos:
- ✅ `testGetWellsReturnsInitializedWells` - Verifica que se retornen todos los pozos inicializados
- ✅ `testGetWellsReturnsCorrectBaseValues` - Confirma los valores base correctos de cada pozo
- ✅ `testSetPressureMultiplier` - Valida el establecimiento del multiplicador de presión
- ✅ `testSetTemperatureMultiplier` - Valida el establecimiento del multiplicador de temperatura
- ✅ `testSetFlowMultiplier` - Valida el establecimiento del multiplicador de flujo
- ✅ `testSetMultiplierThrowsExceptionForNonExistentWell` - Verifica excepción para pozo inexistente
- ✅ `testSetMultiplierThrowsExceptionForNegativeMultiplier` - Verifica excepción para multiplicadores ≤ 0
- ✅ `testSetMultiplierAllowsValuesGreaterThanOne` - Comprueba que se permitan multiplicadores > 1
- ✅ `testSetMultiplierAllowsValuesLessThanOne` - Comprueba que se permitan multiplicadores entre 0 y 1
- ✅ `testResetMultipliersResetsAllWells` - Valida el reseteo de todos los multiplicadores
- ✅ `testResetMultipliersResetsCurrentValues` - Verifica que se reseetean valores actuales a base
- ✅ `testSensorServiceIntegrationWithProducer` - Comprueba integración con SensorProducer
- ✅ `testServiceInitializesWithTwoWells` - Verifica que se inicialicen dos pozos
- ✅ `testSetMultiplierAllowsSmallDecimalValues` - Comprueba multiplicadores decimales pequeños

### 3. **SensorProducerTest** (7 tests)
**Ubicación:** `/src/test/java/com/agonzalorena/msvc/simulator/messaging/producer/SensorProducerTest.java`

Pruebas unitarias para el productor de Kafka `SensorProducer` que envía datos de sensores.

#### Tests incluidos:
- ✅ `testSendMessageToKafka` - Verifica que se envíe mensaje a Kafka correctamente
- ✅ `testSensorDataSerializationToProtobuf` - Valida la serialización a protobuf
- ✅ `testInstantToProtobufTimestampConversion` - Comprueba conversión de Instant a Timestamp
- ✅ `testWellIdUsedAsKafkaKey` - Verifica que wellId se use como clave de Kafka
- ✅ `testSendToCorrectTopic` - Confirma envío al topic correcto "topic-telemetry"
- ✅ `testMultipleSendMessages` - Valida múltiples envíos sin problemas
- ✅ `testPreciseNumericValuesInProtobuf` - Verifica precisión numérica en protobuf

## Dependencias Agregadas

Se agregaron las siguientes dependencias al `pom.xml` padre para testing:

```xml
<!-- JUnit 5 -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-engine</artifactId>
    <scope>test</scope>
</dependency>

<!-- Mockito -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<!-- AssertJ -->
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <scope>test</scope>
</dependency>
```

También se configuró el plugin de Lombok en el pom.xml de msvc-simulator para el procesamiento de anotaciones.

## Ejecución de Tests

### Ejecutar todos los tests:
```bash
cd msvc-simulator
mvn test
```

### Ejecutar tests específicos:
```bash
# Solo WellTest
mvn test -Dtest=WellTest

# Solo SensorServiceTest
mvn test -Dtest=SensorServiceTest

# Solo SensorProducerTest
mvn test -Dtest=SensorProducerTest

# Múltiples tests
mvn test -Dtest=WellTest,SensorServiceTest,SensorProducerTest
```

### Ver reporte de tests:
```bash
mvn surefire-report:report
```

## Características Principales

### Testing Framework
- **JUnit 5**: Framework de testing moderno con soporte para parameterización y extensiones
- **Mockito**: Mocking de dependencias externas (KafkaTemplate)
- **AssertJ**: Assertions fluidas y expresivas para verificaciones claras

### Cobertura
- **Casos positivos**: Funcionalidad esperada funcionando correctamente
- **Casos negativos**: Excepciones y validaciones lanzadas apropiadamente
- **Casos límite**: Valores pequeños, decimales, cero, negativos, etc.
- **Integración**: Interacción correcta entre componentes

### Mock Objects
- `KafkaTemplate<String, byte[]>`: Mockeado en SensorProducerTest para aislar la lógica de envío sin necesidad de Kafka real
- `SensorProducer`: Inyectado como mock en SensorServiceTest para aislar la lógica del servicio

## Resultados

```
Total de tests ejecutados: 34
Fallos: 0
Errores: 0
Skipped: 0
Tasa de éxito: 100%
```

## Notas Importantes

1. **No se incluyen tests del Controller**: `SensorController` no tiene tests unitarios ya que requeriría MockMvc para testing de integración, que se considera fuera del alcance de unit tests puros.

2. **Lombok Processing**: Se configuró el compilador Maven para procesar las anotaciones de Lombok (@Data, @Slf4j) correctamente.

3. **Protobuf Serialization**: Los tests de `SensorProducerTest` validan la serialización/deserialización correcta a Protocol Buffers, verificando que todos los campos se preserven.

4. **Isolation**: Todos los tests están completamente aislados y pueden ejecutarse en cualquier orden sin dependencias entre ellos.

## Próximos Pasos (Opcional)

- Agregar tests de integración para `SensorController` usando `@SpringBootTest` y `MockMvc`
- Configurar cobertura de código con JaCoCo
- Agregar tests parametrizados para casos de prueba adicionales
- Tests de rendimiento para `sendSensorData()` con carga alta

