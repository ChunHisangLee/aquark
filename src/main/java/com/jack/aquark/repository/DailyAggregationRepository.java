package com.jack.aquark.repository;

import com.jack.aquark.entity.DailyAggregation;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyAggregationRepository extends JpaRepository<DailyAggregation, Long> {
    Optional<DailyAggregation> findByStationIdAndObsDateAndCsqAndSensorName(
            String stationId, LocalDate obsDate, String csq, String sensorName);
}
