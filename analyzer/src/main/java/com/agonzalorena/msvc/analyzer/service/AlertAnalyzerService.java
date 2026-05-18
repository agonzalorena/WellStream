package com.agonzalorena.msvc.analyzer.service;

import com.agonzalorena.msvc.analyzer.persistence.LimitType;
import com.agonzalorena.msvc.analyzer.persistence.MetricType;
import com.agonzalorena.msvc.analyzer.persistence.entity.WellAlert;
import com.agonzalorena.msvc.analyzer.persistence.repository.WellAlertRepository;
import com.agonzalorena.msvc.analyzer.presentation.dto.SensorDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AlertAnalyzerService {
    private static final double MAX_PRESSURE_PSI = 4000.0;
    private static final double MIN_PRESSURE_PSI = 250.0;
    private static final double MAX_TEMPERATURE_C = 85.0;
    private static final double MIN_TEMPERATURE_C = 25.0;
    private static final double MIN_FLOW_RATE_BPD = 600.0;
    private static final double MAX_FLOW_RATE_BPD = 3500.0;
    // Zona muerta para evitar alertas si los valores oscilan cerca del límite
    private static final double HYSTERESIS_MAGIN = 6.0;
    private int testCounter = 0;
    private final WellAlertRepository wellAlertRepository;

    //wellId-metric("pozo1-Temperature) --> LimitType(MAX o MIN)
    private final Map<String, LimitType> activeAlertsCache = new ConcurrentHashMap<>();

    public AlertAnalyzerService(WellAlertRepository wellAlertRepository) {
        this.wellAlertRepository = wellAlertRepository;
        loadUnresolvedAlerts();
    }

    private void loadUnresolvedAlerts() {
        wellAlertRepository.findByResolvedFalse().forEach(alert ->
                activeAlertsCache.put(alert.getWellId() + "-" + alert.getMetricType().name(), alert.getLimitType()));
        System.out.println("Loaded unresolved alerts into cache: " + activeAlertsCache);
    }

    public void checkCriticalValues(SensorDTO dto) {
        LocalDateTime timestamp = LocalDateTime.ofInstant(dto.timestamp(), java.time.ZoneId.systemDefault());
        evaluateMetric(dto.wellId(), MetricType.PRESSURE, dto.pressurePsi(), MAX_PRESSURE_PSI, MIN_PRESSURE_PSI, timestamp);
        evaluateMetric(dto.wellId(), MetricType.TEMPERATURE, dto.temperatureC(), MAX_TEMPERATURE_C, MIN_TEMPERATURE_C, timestamp);
        evaluateMetric(dto.wellId(), MetricType.FLOW_RATE, dto.flowRateBpd(), MAX_FLOW_RATE_BPD, MIN_FLOW_RATE_BPD, timestamp);
    }

    private void evaluateMetric(String wellId, MetricType metricType, double currentValue, double maxLimit, double minLimit, LocalDateTime timestamp) {
        String cacheKey = wellId + "-" + metricType.name();

        // Obtenemos qué tipo de alerta está activa (si es null, no hay alerta)
        LimitType activeLimitType = activeAlertsCache.get(cacheKey);

        if (activeLimitType == null) {
            // 1. NO HAY ALERTA: Evaluamos si hay que abrir una nueva
            if (currentValue > maxLimit) {
                createAlert(currentValue, maxLimit, minLimit, LimitType.MAX, metricType, wellId, timestamp, cacheKey);
            } else if (currentValue < minLimit) {
                createAlert(currentValue, maxLimit, minLimit, LimitType.MIN, metricType, wellId, timestamp, cacheKey);
            }
        } else {
            // 2. HAY UNA ALERTA ACTIVA: Evaluamos si ya se resolvió (CON histéresis)
            if (activeLimitType == LimitType.MAX) {
                // Se activó por alta. Se resuelve si baja del límite máximo menos el margen.
                if (currentValue < (maxLimit - HYSTERESIS_MAGIN)) {
                    resolveAlert(wellId, metricType, timestamp, cacheKey);
                }
            } else if (activeLimitType == LimitType.MIN) {
                // Se activó por baja. Se resuelve si sube del límite mínimo más el margen.
                if (currentValue > (minLimit + HYSTERESIS_MAGIN)) {
                    resolveAlert(wellId, metricType, timestamp, cacheKey);
                }
            }
        }
    }

    private void createAlert(double criticalValue, double maxLimit, double minLimit, LimitType limitType,
                             MetricType metricType, String wellId, LocalDateTime startTime, String cacheKey) {
        WellAlert alert = new WellAlert();
        alert.setCriticalValue(criticalValue);
        alert.setMetricType(metricType);
        alert.setStartTime(startTime);
        alert.setWellId(wellId);
        alert.setLimitType(limitType);
        alert.setMaxLimit(maxLimit);
        alert.setMinLimit(minLimit);
        wellAlertRepository.save(alert);
        activeAlertsCache.put(cacheKey, limitType);
        //enviar notificacion AlertProducer.sendAlertMessage(alertDTO);
    }

    private void resolveAlert(String wellId, MetricType metricType, LocalDateTime resolvedTime, String cacheKey) {
        WellAlert alert = wellAlertRepository.findByWellIdAndMetricTypeAndResolvedFalse(wellId, metricType)
                .orElseThrow(() -> new RuntimeException("No active alert found for well: " + wellId + " and metric: " + metricType));

        alert.setResolved(true);
        alert.setResolvedTime(resolvedTime);

        wellAlertRepository.save(alert);
        //enviar notificacion AlertProducer.sendAlertResolvedMessage(alertDTO);
        activeAlertsCache.remove(cacheKey);

    }

}
