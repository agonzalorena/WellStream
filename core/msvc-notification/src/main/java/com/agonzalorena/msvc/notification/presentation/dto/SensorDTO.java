package com.agonzalorena.msvc.notification.presentation.dto;

import java.time.Instant;

public record SensorDTO(
        String wellId,
        Instant timestamp,
        Double pressurePsi,
        Double temperatureC,
        Double flowRateBpd
) {
}

