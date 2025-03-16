package com.jack.aquark.service;

import com.jack.aquark.entity.HourlyAggregation;
import com.jack.aquark.entity.SensorData;

import java.time.LocalDateTime;
import java.util.List;

public interface AggregationService {
  void saveOrUpdateHourlyAggregation(HourlyAggregation aggregation);

  void aggregateHourlyData();

  void aggregateDailyData();

  void processTempDataForAggregations();

  List<SensorData> getSensorDataByTimeRange(LocalDateTime start, LocalDateTime end);
}
