package com.agonzalorena.msvc.analyzer.messaging.producer;

import com.agonzalorena.msvc.analyzer.presentation.dto.AlertNotificationDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

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
                        System.err.println("Error sending alert notification: " + exception.getMessage());
                    } else {
                        System.out.println("Alert notification sent to partition: " + result.getRecordMetadata().partition() + " with offset: " + result.getRecordMetadata().offset());
                    }
                });
        System.out.println("Sent alert notification: " + alertNotification.alertStatus());
    }
}
