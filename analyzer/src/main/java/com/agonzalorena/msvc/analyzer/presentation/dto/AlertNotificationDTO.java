package com.agonzalorena.msvc.analyzer.presentation.dto;

import com.agonzalorena.msvc.analyzer.persistence.LimitType;
import com.agonzalorena.msvc.analyzer.persistence.MetricType;

import java.time.Instant;

public record AlertNotificationDTO(
        String wellId,
        MetricType metricType,
        LimitType limitType,
        double criticalValue,
        double limitExceededValue,
        Instant timestamp,
        AlertStatus alertStatus

) {
}

