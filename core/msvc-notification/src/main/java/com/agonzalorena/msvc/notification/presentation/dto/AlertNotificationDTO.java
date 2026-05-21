package com.agonzalorena.msvc.notification.presentation.dto;

import com.agonzalorena.msvc.notification.common.enums.AlertStatus;
import com.agonzalorena.msvc.notification.common.enums.LimitType;
import com.agonzalorena.msvc.notification.common.enums.MetricType;

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

