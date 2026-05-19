package com.agonzalorena.msvc.analyzer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.limits")
public record AlertLimitsConfig(
        Pressure pressure,
        Temperature temperature,
        FlowRate flowRate,
        double hysteresisMargin
) {

    public record Pressure(double max, double min) {
    }

    public record Temperature(double max, double min) {
    }

    public record FlowRate(double max, double min) {
    }
}