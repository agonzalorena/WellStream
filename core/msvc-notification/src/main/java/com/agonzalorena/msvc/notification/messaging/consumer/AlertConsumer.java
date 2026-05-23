package com.agonzalorena.msvc.notification.messaging.consumer;

import com.agonzalorena.msvc.notification.common.enums.AlertStatus;
import com.agonzalorena.msvc.notification.common.enums.LimitType;
import com.agonzalorena.msvc.notification.common.enums.MetricType;
import com.agonzalorena.msvc.notification.presentation.dto.AlertNotificationDTO;
import com.agonzalorena.msvc.notification.presentation.service.SseService;
import com.agonzalorena.msvc.protobuf.AlertProto;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class AlertConsumer {
    private static final String TOPIC = "alerts";

    private final SseService sseService;

    public AlertConsumer(SseService sseService) {
        this.sseService = sseService;
    }
    @KafkaListener(topics = TOPIC, groupId = "notification-alerts-group")
    public void consume(byte[] payload) {
        try {
            AlertProto.AlertEvent alertEvent = AlertProto.AlertEvent.parseFrom(payload);
            Timestamp protoTime = alertEvent.getTimestamp();
            Instant timestamp = Instant.ofEpochSecond(protoTime.getSeconds(), protoTime.getNanos());

            sseService.broadcast("alert", new AlertNotificationDTO(
                    alertEvent.getWellId(),
                    MetricType.valueOf(alertEvent.getMetricType().name()),
                    LimitType.valueOf(alertEvent.getLimitType().name()),
                    alertEvent.getCriticalValue(),
                    alertEvent.getLimitExceededValue(),
                    timestamp,
                    AlertStatus.valueOf(alertEvent.getAlertStatus().name())
            ));
        }catch (InvalidProtocolBufferException e){
            log.error("Failed to parse protobuf message: {}", e.getMessage());
        }
    }
}
