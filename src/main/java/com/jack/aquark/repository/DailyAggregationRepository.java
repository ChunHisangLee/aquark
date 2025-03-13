package com.jack.aquark.repository;

import com.jack.aquark.entity.DailyAggregation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyAggregationRepository extends JpaRepository<DailyAggregation, Long> {}
