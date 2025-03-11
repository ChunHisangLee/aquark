package com.jack.aquark.repository;

import com.jack.aquark.entity.SensorThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorThresholdRepository extends JpaRepository<SensorThreshold, Long> {
  SensorThreshold findBySensorName(String sensorName);
}
