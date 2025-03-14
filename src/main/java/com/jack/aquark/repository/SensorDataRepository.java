package com.jack.aquark.repository;

import com.jack.aquark.entity.SensorData;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
  List<SensorData> findAllByObsTimeBetweenOrderByObsTimeAsc(
      LocalDateTime obsTimeAfter, LocalDateTime obsTimeBefore);

  boolean existsByStationIdAndObsTimeAndCsq(String stationId, LocalDateTime obsTime, String csq);
}
