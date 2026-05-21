package com.agonzalorena.msvc.analyzer.messaging.consumer.parser;


import com.agonzalorena.msvc.analyzer.presentation.dto.SensorDTO;
import com.agonzalorena.msvc.protobuf.SensorProto.SensorEvent;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class SensorEventParser {
    public SensorDTO parse(byte[] payload) {
        try {
            SensorEvent sensorData = SensorEvent.parseFrom(payload);
            //convertir timestamp de google.protobuf.Timestamp a java.time.Instant
            Timestamp protoTime = sensorData.getTimestamp();
            Instant timestamp = Instant.ofEpochSecond(protoTime.getSeconds(), protoTime.getNanos());

            return new SensorDTO(
                    sensorData.getWellId(),
                    timestamp,
                    sensorData.getPressurePsi(),
                    sensorData.getTemperatureC(),
                    sensorData.getFlowRateBpd()
            );
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to parse protobuf message: {}", e.getMessage());
            return null;
        }
    }
}
