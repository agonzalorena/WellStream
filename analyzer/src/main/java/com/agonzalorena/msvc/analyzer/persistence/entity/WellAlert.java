package com.agonzalorena.msvc.analyzer.persistence.entity;

import com.agonzalorena.msvc.analyzer.persistence.LimitType;
import com.agonzalorena.msvc.analyzer.persistence.MetricType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class WellAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String wellId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetricType metricType;
    @Column(nullable = false)
    private Double criticalValue;
    private Double minLimit;
    private Double maxLimit;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LimitType limitType;
    @Column(nullable = false)
    private LocalDateTime startTime;
    @Column(nullable = false)
    private boolean resolved = false;
    private LocalDateTime resolvedTime;
}
