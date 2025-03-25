package com.jack.aquark.repository;

import com.jack.aquark.entity.DailyAggregation;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyAggregationRepository extends JpaRepository<DailyAggregation, Long> {
  List<DailyAggregation> findByObsDateBetween(LocalDate start, LocalDate end);

  Optional<DailyAggregation> findByStationIdAndObsDateAndCsqAndTimeCategory(
      String stationId, LocalDate obsDate, String csq, String timeCategory);
}
