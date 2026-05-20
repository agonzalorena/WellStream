package com.agonzalorena.msvc.analyzer.persistence.repository;

import com.agonzalorena.msvc.analyzer.persistence.entity.SensorAverage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorAverageRepository extends JpaRepository<SensorAverage, Long> {
}
