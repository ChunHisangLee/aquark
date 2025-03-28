package com.jack.aquark.repository;

import com.jack.aquark.entity.HourlyAggregation;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HourlyAggregationRepository extends JpaRepository<HourlyAggregation, Long> {
  List<HourlyAggregation> findByObsDateBetween(LocalDate start, LocalDate end);

  Optional<HourlyAggregation> findByStationIdAndObsDateAndObsHourAndCsqAndTimeCategory(
      String stationId, LocalDate obsDate, int obsHour, String csq, String timeCategory);
}
