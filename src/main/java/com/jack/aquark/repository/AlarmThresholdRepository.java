package com.jack.aquark.repository;

import com.jack.aquark.entity.AlarmThreshold;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmThresholdRepository extends JpaRepository<AlarmThreshold, Long> {
  Optional<AlarmThreshold> findByStationIdAndCsqAndParameter(
      String stationId, String csq, String parameter);
}
