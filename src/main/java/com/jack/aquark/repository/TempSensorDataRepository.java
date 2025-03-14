package com.jack.aquark.repository;

import com.jack.aquark.entity.TempSensorData;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TempSensorDataRepository extends JpaRepository<TempSensorData, Long> {
  List<TempSensorData> findAllByObsTimeBetween(LocalDateTime start, LocalDateTime end);

  void deleteAll();
}
