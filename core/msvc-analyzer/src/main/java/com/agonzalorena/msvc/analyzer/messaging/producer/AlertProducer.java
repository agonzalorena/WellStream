package com.agonzalorena.msvc.analyzer.messaging.producer;

import com.agonzalorena.msvc.analyzer.presentation.dto.AlertNotificationDTO;
import com.agonzalorena.msvc.protobuf.AlertProto;
import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.agonzalorena.msvc.protobuf.AlertProto.AlertEvent;

@Slf4j
@Component
public class AlertProducer {
    private static final String TOPIC = "alerts";

    private KafkaTemplate<String, byte[]> kafkaTemplate;

    public AlertProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(AlertNotificationDTO alertNotification) {
        // Convertimos Instant a google.protobuf.Timestamp
        Timestamp protoTimestamp = Timestamp.newBuilder()
                .setSeconds(alertNotification.timestamp().getEpochSecond())
                .setNanos(alertNotification.timestamp().getNano())
                .build();

        AlertEvent alertEvent = AlertEvent.newBuilder()
                .setWellId(alertNotification.wellId())
                .setMetricType(AlertProto.MetricType.valueOf(alertNotification.metricType().name()))
                .setLimitType(AlertProto.LimitType.valueOf(alertNotification.limitType().name()))
                .setCriticalValue(alertNotification.criticalValue())
                .setLimitExceededValue(alertNotification.limitExceededValue())
                .setTimestamp(protoTimestamp)
                .setAlertStatus(AlertProto.AlertStatus.valueOf(alertNotification.alertStatus().name()))
                .build();

        kafkaTemplate.send(TOPIC, alertNotification.wellId(), alertEvent.toByteArray())
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("Error sending alert notification: {}", exception.getMessage());
                    } else {
                        log.info("Alert notification sent to partition: {} with offset: {}", result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                    }
                });
        System.out.println("Sent alert notification: " + alertNotification.alertStatus());
    }
}
