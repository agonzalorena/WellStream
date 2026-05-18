package com.agonzalorena.msvc.analyzer.persistence.repository;

import com.agonzalorena.msvc.analyzer.persistence.MetricType;
import com.agonzalorena.msvc.analyzer.persistence.entity.WellAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WellAlertRepository extends JpaRepository<WellAlert, Long> {

    List<WellAlert> findByResolvedFalse();

    Optional<WellAlert> findByWellIdAndMetricTypeAndResolvedFalse(String wellId, MetricType metricType);
}
