package com.jack.aquark.service;

import com.jack.aquark.entity.HourlyAggregation;

public interface AggregationService {
  void saveHourlyAggregation(HourlyAggregation aggregation);

  void aggregateHourlyData();

  void aggregateDailyData();

  void processTempDataForAggregations();
}
