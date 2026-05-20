package com.agonzalorena.msvc.analyzer.service;

import com.agonzalorena.msvc.analyzer.messaging.producer.AlertProducer;
import com.agonzalorena.msvc.analyzer.common.enums.LimitType;
import com.agonzalorena.msvc.analyzer.persistence.entity.WellAlert;
import com.agonzalorena.msvc.analyzer.presentation.dto.AlertNotificationDTO;
import com.agonzalorena.msvc.analyzer.common.enums.AlertStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AlertNotificationService {
    private final AlertProducer alertProducer;

    public AlertNotificationService(AlertProducer alertProducer) {
        this.alertProducer = alertProducer;
    }

    public void notifyActiveAlert(WellAlert alert) {
        AlertNotificationDTO dto = buildDto(alert,alert.getStartTime(), AlertStatus.ACTIVE);
        alertProducer.send(dto);
    }
    public void notifyResolvedAlert(WellAlert alert) {
        AlertNotificationDTO dto = buildDto(alert,alert.getResolvedTime(), AlertStatus.RESOLVED);
        alertProducer.send(dto);
    }

    private AlertNotificationDTO buildDto(WellAlert alert, Instant eventTime, AlertStatus status) {
        double applicableLimit = (alert.getLimitType() == LimitType.MAX)
                ? alert.getMaxLimit()
                : alert.getMinLimit();

        return new AlertNotificationDTO(
                alert.getWellId(),
                alert.getMetricType(),
                alert.getLimitType(),
                alert.getCriticalValue(),
                applicableLimit,
                eventTime,
                status
        );
    }
}
