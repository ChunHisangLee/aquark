package com.jack.aquark.repository;

import com.jack.aquark.entity.HourlyAggregation;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HourlyAggregationRepository extends JpaRepository<HourlyAggregation, Long> {
  List<HourlyAggregation> findByObsDate(LocalDate obsDate);

  List<HourlyAggregation> findByObsDateBetween(LocalDate start, LocalDate end);
}
