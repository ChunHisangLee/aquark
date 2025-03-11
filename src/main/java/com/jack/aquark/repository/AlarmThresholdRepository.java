package com.jack.aquark.repository;

import com.jack.aquark.entity.AlarmThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlarmThresholdRepository extends JpaRepository<AlarmThreshold, Long> {
  Optional<AlarmThreshold> findBySensorType(String sensorType);
}
