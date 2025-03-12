package com.jack.aquark.repository;

import com.jack.aquark.entity.DailyAggregation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyAggregationRepository extends JpaRepository<DailyAggregation, Long> {
  List<DailyAggregation> findByObsDate(LocalDate obsDate);
}
