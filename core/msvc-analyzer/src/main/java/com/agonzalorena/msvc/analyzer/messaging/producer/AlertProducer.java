package com.agonzalorena.msvc.analyzer.messaging.producer;

import com.agonzalorena.msvc.analyzer.presentation.dto.AlertNotificationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AlertProducer {
    private static final String TOPIC = "alerts";

    private KafkaTemplate<String, AlertNotificationDTO> kafkaTemplate;

    public AlertProducer(KafkaTemplate<String, AlertNotificationDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(AlertNotificationDTO alertNotification) {
        kafkaTemplate.send(TOPIC, alertNotification.wellId(), alertNotification)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("Error sending alert notification: {}", exception.getMessage());
                    } else {
                        log.info("Alert notification sent to partition: {} with offset: {}", result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                    }
                });
        //TODO
        System.out.println("Sent alert notification: " + alertNotification.alertStatus());
    }
}
