package com.jack.aquark.repository;

import com.jack.aquark.entity.SensorData;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
  // 查詢指定時間區間內的數據（例如：尖峰或離峰）
  List<SensorData> findAllByObsTimeBetween(LocalDateTime start, LocalDateTime end);

  @Query("SELECT FUNCTION('DATE_FORMAT', sd.obsTime, '%Y-%m-%d %H:00') as hour, AVG(sd.v1) as avgV1, AVG(sd.v2) as avgV2, AVG(sd.v3) as avgV3, AVG(sd.v4) as avgV4, AVG(sd.v5) as avgV5, AVG(sd.v6) as avgV6, AVG(sd.v7) as avgV7, AVG(sd.rh) as avgRh, AVG(sd.tx) as avgTx, AVG(sd.echo) as avgEcho, AVG(sd.speed) as avgSpeed " +
          "FROM SensorData sd WHERE sd.obsTime BETWEEN ?1 AND ?2 GROUP BY hour")
  List<Object[]> findHourlyAverages(LocalDateTime start, LocalDateTime end);
}
