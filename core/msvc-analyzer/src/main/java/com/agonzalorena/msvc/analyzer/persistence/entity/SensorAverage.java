package com.agonzalorena.msvc.analyzer.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
public class SensorAverage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String wellId;
    private Instant startWindowTime;
    private Instant endWindowTime;
    private Double avgPressurePsi;
    private Double avgTemperatureC;
    private Double avgFlowRateBpd;
    private Integer readingsCount;
}
