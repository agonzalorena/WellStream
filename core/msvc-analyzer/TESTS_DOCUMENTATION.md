# Unit Tests - WellStream Core (msvc-analyzer)

## Resumen

Se han implementado **78 unit tests** completos para el módulo `msvc-analyzer` del proyecto WellStream. Todos los tests pasan satisfactoriamente sin fallos ni errores.

## Clases Testeadas

### 1. **TelemetryBufferManagerTest** (10 tests)
**Ubicación:** `/src/test/java/com/agonzalorena/msvc/analyzer/messaging/buffer/TelemetryBufferManagerTest.java`

Pruebas para el gestor de buffer de telemetría que acumula datos de sensores.

#### Tests incluidos:
- ✅ `testAddSensorData` - Agregar un dato de sensor al buffer
- ✅ `testAddMultipleSensorDataSameWell` - Agregar múltiples datos del mismo pozo
- ✅ `testAddSensorDataMultipleWells` - Agregar datos de múltiples pozos
- ✅ `testFlushClearsBuffer` - Limpiar buffer después de flush (atomic swap)
- ✅ `testDataPersistsUntilFlush` - Los datos persisten hasta hacer flush
- ✅ `testFlushReturnsNewEmptyBuffer` - Nuevo buffer vacío después de intercambio
- ✅ `testComplexMultiWellScenario` - Múltiples pozos con múltiples lecturas
- ✅ `testAddDataAfterFlush` - Agregar datos después de flush
- ✅ `testFlushReturnEmptyMapWhenNoData` - Retornar mapa vacío sin datos
- ✅ `testConsecutiveFlushesWithoutData` - Flushes consecutivos sin datos

### 2. **ActiveAlertCacheManagerTest** (12 tests)
**Ubicación:** `/src/test/java/com/agonzalorena/msvc/analyzer/cache/ActiveAlertCacheManagerTest.java`

Pruebas para el gestor de caché de alertas activas.

#### Tests incluidos:
- ✅ `testSaveAlert` - Guardar alerta en cache
- ✅ `testGetAlertByWellIdAndMetricType` - Recuperar alerta por wellId y métrica
- ✅ `testGetNonExistentAlert` - Retornar null para alerta inexistente
- ✅ `testRemoveAlert` - Remover alerta de cache
- ✅ `testMultipleAlertsFromDifferentWells` - Múltiples alertas de diferentes pozos
- ✅ `testMultipleMetricsFromSameWell` - Múltiples métricas del mismo pozo
- ✅ `testOverwriteExistingAlert` - Reemplazar alerta existente
- ✅ `testLoadUnresolvedAlertsOnInit` - Cargar alertas no resueltas al iniciar
- ✅ `testRepositoryCalled` - Verificar que se llama a findByResolvedFalse
- ✅ `testRemoveNonExistentAlert` - Remover alerta que no existe sin error
- ✅ `testConsecutiveOperations` - Operaciones consecutivas manteniendo integridad
- ✅ `testThreadSafety` - Thread-safety con ConcurrentHashMap

### 3. **AlertProducerTest** (12 tests)
**Ubicación:** `/src/test/java/com/agonzalorena/msvc/analyzer/messaging/producer/AlertProducerTest.java`

Pruebas para el productor de alertas a Kafka.

#### Tests incluidos:
- ✅ `testSendAlertToKafka` - Enviar alerta a Kafka
- ✅ `testSendToCorrectTopic` - Enviar al topic "alerts"
- ✅ `testWellIdUsedAsKafkaKey` - Usar wellId como clave
- ✅ `testAlertSerializationToProtobuf` - Serialización a Protobuf
- ✅ `testAlertStatusConversion` - Conversión de AlertStatus
- ✅ `testInstantToProtobufTimestampConversion` - Conversión de Instant a Timestamp
- ✅ `testMultipleSends` - Múltiples envíos sin problemas
- ✅ `testAllFieldsPreservedInProtobuf` - Todos los campos preservados
- ✅ `testPreciseNumericValues` - Precisión numérica en valores
- ✅ `testProducerIsNotNull` - Producer inyectable
- ✅ `testWhenCompleteHandlerIsSet` - whenComplete handler configurado
- ✅ `testSendDifferentMetricTypes` - Enviar diferentes tipos de métrica

### 4. **AlertNotificationServiceTest** (14 tests)
**Ubicación:** `/src/test/java/com/agonzalorena/msvc/analyzer/service/AlertNotificationServiceTest.java`

Pruebas para el servicio de notificación de alertas.

#### Tests incluidos:
- ✅ `testNotifyActiveAlert` - Notificar alerta activa
- ✅ `testNotifyResolvedAlert` - Notificar alerta resuelta
- ✅ `testActiveAlertUsesStartTime` - Usar timestamp de inicio para alerta activa
- ✅ `testResolvedAlertUsesResolvedTime` - Usar timestamp de resolución
- ✅ `testMaxLimitUsedWhenLimitTypeIsMax` - Usar maxLimit cuando LimitType es MAX
- ✅ `testMinLimitUsedWhenLimitTypeIsMin` - Usar minLimit cuando LimitType es MIN
- ✅ `testAllFieldsPreservedInNotification` - Preservar todos los campos
- ✅ `testNotifyDifferentMetricTypes` - Notificar diferentes tipos de métrica
- ✅ `testNotifyMultipleWells` - Notificar múltiples pozos
- ✅ `testSendCalledOnceForActiveAlert` - send() llamado una sola vez
- ✅ `testSendCalledOnceForResolvedAlert` - send() llamado una sola vez (resuelta)
- ✅ `testPreciseNumericValues` - Valores numéricos con decimales
- ✅ `testMetricTypePreserved` - Preservar metricType
- ✅ `testLimitTypePreserved` - Preservar limitType

### 5. **SensorAverageCalculatorServiceTest** (14 tests)
**Ubicación:** `/src/test/java/com/agonzalorena/msvc/analyzer/service/SensorAverageCalculatorServiceTest.java`

Pruebas para el servicio que calcula promedios de sensores.

#### Tests incluidos:
- ✅ `testCalculateAveragePressure` - Calcular promedio de presión
- ✅ `testCalculateAverageTemperature` - Calcular promedio de temperatura
- ✅ `testCalculateAverageFlow` - Calcular promedio de flujo
- ✅ `testRoundAverageValues` - Redondear promedios a 2 decimales
- ✅ `testSetWellId` - Establecer wellId correctamente
- ✅ `testSetWindowTimes` - Establecer tiempos de ventana
- ✅ `testReadingsCount` - Contar lecturas correctamente
- ✅ `testProcessMultipleWells` - Procesar múltiples pozos simultáneamente
- ✅ `testNoProcessingWhenBufferEmpty` - Sin procesamiento si buffer vacío
- ✅ `testHandleExceptionForOneWellWithMultiple` - Manejar excepción sin perder datos
- ✅ `testCalculateWithSingleReading` - Calcular con una sola lectura
- ✅ `testDecimalPrecision` - Precisión decimal en cálculos
- ✅ `testFlushCalledOnce` - flush() llamado una sola vez
- ✅ `testRealWorldScenario` - Escenario del mundo real

### 6. **AlertAnalyzerServiceTest** (16 tests)
**Ubicación:** `/src/test/java/com/agonzalorena/msvc/analyzer/service/AlertAnalyzerServiceTest.java`

Pruebas para el servicio principal de análisis de alertas.

#### Tests incluidos:
- ✅ `testCreateAlertWhenPressureExceedsMax` - Crear alerta cuando presión excede máximo
- ✅ `testCreateAlertWhenPressureBelowMin` - Crear alerta cuando presión bajo mínimo
- ✅ `testNoAlertWhenValueWithinLimits` - Sin alerta dentro de límites
- ✅ `testResolveAlertWhenValueBelowMaxWithHysteresis` - Resolver alerta con histéresis
- ✅ `testResolveAlertWhenValueAboveMinWithHysteresis` - Resolver con histéresis (mínimo)
- ✅ `testNoResolveAlertWhenWithinHysteresisMargin` - Sin resolver dentro de margen
- ✅ `testEvaluateAllMetrics` - Evaluar todas las métricas
- ✅ `testHandleRaceConditionWhenResolvingAlert` - Manejar race condition
- ✅ `testSaveAlertInCache` - Guardar alerta en cache
- ✅ `testNotifyActiveAlert` - Notificar alerta activa
- ✅ `testNotifyResolvedAlert` - Notificar alerta resuelta
- ✅ `testAlertFieldsSetCorrectly` - Campos de alerta configurados correctamente
- ✅ `testRemoveAlertFromCacheWhenResolved` - Remover alerta de cache al resolver
- ✅ `testEvaluateTemperature` - Evaluar temperatura
- ✅ `testEvaluateFlowRate` - Evaluar flujo
- ✅ `testHandleMultipleWellsIndependently` - Manejar múltiples pozos independientemente

## Resultados

```
Total de tests ejecutados: 78
Fallos: 0
Errores: 0
Skipped: 0
Tasa de éxito: 100%

Desglose por clase:
  - TelemetryBufferManagerTest: 10 tests ✅
  - ActiveAlertCacheManagerTest: 12 tests ✅
  - AlertProducerTest: 12 tests ✅
  - AlertNotificationServiceTest: 14 tests ✅
  - SensorAverageCalculatorServiceTest: 14 tests ✅
  - AlertAnalyzerServiceTest: 16 tests ✅
```

## Ejecución de Tests

### Ejecutar todos los tests de analyzer:
```bash
cd msvc-analyzer
mvn test
```

### Ejecutar tests específicos:
```bash
# Solo TelemetryBufferManagerTest
mvn test -Dtest=TelemetryBufferManagerTest

# Solo AlertAnalyzerServiceTest
mvn test -Dtest=AlertAnalyzerServiceTest

# Múltiples tests
mvn test -Dtest="TelemetryBufferManagerTest,AlertProducerTest,AlertAnalyzerServiceTest"

# Todos nuestros tests (sin aplicación)
mvn test -Dtest="TelemetryBufferManagerTest,ActiveAlertCacheManagerTest,AlertProducerTest,AlertNotificationServiceTest,SensorAverageCalculatorServiceTest,AlertAnalyzerServiceTest"
```

## Características Principales

### Testing Patterns
- **AAA Pattern** (Arrange-Act-Assert)
- **Mockito** para aislar dependencias
- **AssertJ** para assertions fluidas
- **@DisplayName** para descripción clara
- **@ExtendWith(MockitoExtension.class)** para integración

### Cobertura
- Casos positivos y negativos
- Validaciones y lógica de negocio
- Manejo de excepciones
- Race conditions y concurrencia
- Valores límite y decimales
- Integración entre componentes

### Isolation
- Tests independientes entre sí
- KafkaTemplate y Repository mockeados
- Buffer y Cache aislados
- Sin dependencias externas reales

## Arquitectura de Tests

```
msvc-analyzer/
├── src/test/java/com/agonzalorena/msvc/analyzer/
│   ├── messaging/buffer/
│   │   └── TelemetryBufferManagerTest.java (10)
│   ├── cache/
│   │   └── ActiveAlertCacheManagerTest.java (12)
│   ├── messaging/producer/
│   │   └── AlertProducerTest.java (12)
│   └── service/
│       ├── AlertNotificationServiceTest.java (14)
│       ├── SensorAverageCalculatorServiceTest.java (14)
│       └── AlertAnalyzerServiceTest.java (16)
```

## Notas Importantes

1. **Histéresis**: Se valida correctamente el margen de histéresis para evitar oscilaciones en alertas
2. **Race Conditions**: Se maneja correctamente cuando múltiples hilos resuelven la misma alerta
3. **Atomic Swap**: El buffer utiliza intercambio de referencias atómico para evitar perder datos
4. **Thread-Safety**: ConcurrentHashMap validado con múltiples operaciones
5. **Serialización Protobuf**: Se deserializa y valida correctamente

## Próximos Pasos (Opcional)

- Agregar tests de integración para el pipeline completo
- Configurar cobertura de código con JaCoCo
- Tests parametrizados para variaciones de entrada
- Tests de rendimiento bajo carga
- Integration tests con embebbed Kafka/Database

