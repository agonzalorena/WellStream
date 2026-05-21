package com.agonzalorena.msvc.analyzer.messaging.consumer;

import com.agonzalorena.msvc.analyzer.messaging.consumer.parser.SensorEventParser;
import com.agonzalorena.msvc.analyzer.presentation.dto.SensorDTO;
import com.agonzalorena.msvc.analyzer.service.AlertAnalyzerService;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class RealTimeAlertConsumer implements ConsumerSeekAware {
    private final AlertAnalyzerService alertAnalyzerService;
    private final SensorEventParser sensorEventParser;

    public RealTimeAlertConsumer(AlertAnalyzerService alertAnalyzerService, SensorEventParser sensorEventParser) {
        this.alertAnalyzerService = alertAnalyzerService;
        this.sensorEventParser = sensorEventParser;
    }
    /*
    Implementamos ConsumerSeekAware para controlar el offset de consumo y asegurarnos de que este consumidor
    solo procese los mensajes nuevos que lleguen a partir de su inicio.
    Al implementar onPartitionsAssigned y llamar a seekToEnd, garantizamos que el consumidor comience a leer
    desde el final de la partición, es decir, solo los mensajes que se publiquen después de que el consumidor haya comenzado a escuchar.
    */
    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        callback.seekToEnd(assignments.keySet());
    }

    @KafkaListener(topics = "topic-telemetry", groupId = "real-time-alert-group")
    public void consume(byte[] payload) {
        SensorDTO dto = sensorEventParser.parse(payload);
        if(dto != null) {
            System.out.println("Received telemetry data for real-time alert analysis: " + dto);
            alertAnalyzerService.checkCriticalValues(dto);
        }
    }
}
