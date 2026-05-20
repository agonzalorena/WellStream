package com.agonzalorena.msvc.analyzer.service;

import com.agonzalorena.msvc.analyzer.cache.ActiveAlertCacheManager;
import com.agonzalorena.msvc.analyzer.config.AlertLimitsConfig;
import com.agonzalorena.msvc.analyzer.common.enums.LimitType;
import com.agonzalorena.msvc.analyzer.common.enums.MetricType;
import com.agonzalorena.msvc.analyzer.persistence.entity.WellAlert;
import com.agonzalorena.msvc.analyzer.persistence.repository.WellAlertRepository;
import com.agonzalorena.msvc.analyzer.presentation.dto.SensorDTO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AlertAnalyzerService {
    private final AlertLimitsConfig limits;
    private final WellAlertRepository wellAlertRepository;
    private final ActiveAlertCacheManager cacheManager;
    private final AlertNotificationService alertNotificationService;


    public AlertAnalyzerService(WellAlertRepository wellAlertRepository,
                                ActiveAlertCacheManager cacheManager,
                                AlertNotificationService alertNotificationService,
                                AlertLimitsConfig limits) {
        this.wellAlertRepository = wellAlertRepository;
        this.cacheManager = cacheManager;
        this.alertNotificationService = alertNotificationService;
        this.limits = limits;
    }


    @Async
    public void checkCriticalValues(SensorDTO dto) {
        evaluateMetric(dto.wellId(), MetricType.PRESSURE, dto.pressurePsi(), limits.pressure().max(), limits.pressure().min(), dto.timestamp());
        evaluateMetric(dto.wellId(), MetricType.TEMPERATURE, dto.temperatureC(), limits.temperature().max(), limits.temperature().min(), dto.timestamp());
        evaluateMetric(dto.wellId(), MetricType.FLOW_RATE, dto.flowRateBpd(), limits.flowRate().max(), limits.flowRate().min(), dto.timestamp());
    }

    private void evaluateMetric(String wellId, MetricType metricType, double currentValue, double maxLimit, double minLimit, Instant timestamp) {

        // Obtenemos qué tipo de alerta está activa (si es null, no hay alerta)
        WellAlert cachedAlert = cacheManager.get(wellId, metricType.name());
        LimitType activeLimitType = (cachedAlert != null) ? cachedAlert.getLimitType() : null;

        if (activeLimitType == null) {
            // 1. NO HAY ALERTA: Evaluamos si hay que abrir una nueva
            if (currentValue > maxLimit) {
                createAlert(currentValue, maxLimit, minLimit, LimitType.MAX, metricType, wellId, timestamp);
            } else if (currentValue < minLimit) {
                createAlert(currentValue, maxLimit, minLimit, LimitType.MIN, metricType, wellId, timestamp);
            }
        } else {
            // 2. HAY UNA ALERTA ACTIVA: Evaluamos si ya se resolvió (CON histéresis)
            if (activeLimitType == LimitType.MAX) {
                // Se activó por alta. Se resuelve si baja del límite máximo menos el margen.
                if (currentValue < (maxLimit - limits.hysteresisMargin())) {
                    resolveAlert(wellId, metricType, timestamp);
                }
            } else if (activeLimitType == LimitType.MIN) {
                // Se activó por baja. Se resuelve si sube del límite mínimo más el margen.
                if (currentValue > (minLimit + limits.hysteresisMargin())) {
                    resolveAlert(wellId, metricType, timestamp);
                }
            }
        }
    }

    private void createAlert(double criticalValue, double maxLimit, double minLimit, LimitType limitType,
                             MetricType metricType, String wellId, Instant startTime) {
        WellAlert alert = new WellAlert();
        alert.setCriticalValue(criticalValue);
        alert.setMetricType(metricType);
        alert.setStartTime(startTime);
        alert.setWellId(wellId);
        alert.setLimitType(limitType);
        alert.setMaxLimit(maxLimit);
        alert.setMinLimit(minLimit);

        wellAlertRepository.save(alert);
        cacheManager.save(alert);
        alertNotificationService.notifyActiveAlert(alert);
    }

    private void resolveAlert(String wellId, MetricType metricType, Instant resolvedTime) {
        int rowsUpdate = wellAlertRepository.resolveAlert(wellId, metricType, resolvedTime);
        if (rowsUpdate == 0) {
            throw new RuntimeException("Failed to resolve alert for well: " + wellId + " and metric: " + metricType);
        }

        WellAlert alert = cacheManager.get(wellId, metricType.name());
        alert.setResolved(true);
        alert.setResolvedTime(resolvedTime);
        cacheManager.remove(wellId, metricType.name());
        alertNotificationService.notifyResolvedAlert(alert);
    }
}
