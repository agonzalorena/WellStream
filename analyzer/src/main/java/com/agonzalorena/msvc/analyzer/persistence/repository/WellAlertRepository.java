package com.agonzalorena.msvc.analyzer.persistence.repository;

import com.agonzalorena.msvc.analyzer.persistence.MetricType;
import com.agonzalorena.msvc.analyzer.persistence.entity.WellAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface WellAlertRepository extends JpaRepository<WellAlert, Long> {

    List<WellAlert> findByResolvedFalse();

    Optional<WellAlert> findByWellIdAndMetricTypeAndResolvedFalse(String wellId, MetricType metricType);

    // Es necesario usar @Modifying para indicar que esta consulta modifica datos, y @Query para definir la consulta personalizada
    @Modifying
    @Transactional
    @Query("UPDATE WellAlert w SET w.resolved = true, w.resolvedTime = :resolvedTime WHERE w.wellId = :wellId " +
            "AND w.metricType = :metricType AND w.resolved = false")
    int resolveAlert(String wellId, MetricType metricType, Instant resolvedTime);
}
