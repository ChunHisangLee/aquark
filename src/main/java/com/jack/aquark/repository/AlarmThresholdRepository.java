package com.jack.aquark.repository;

import com.jack.aquark.entity.AlarmThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AlarmThresholdRepository extends JpaRepository<AlarmThreshold, Long> {
  Optional<AlarmThreshold> findBySensorName(String sensorName);
}
