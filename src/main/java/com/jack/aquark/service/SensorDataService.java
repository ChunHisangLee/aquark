package com.jack.aquark.service;

import com.jack.aquark.entity.DailyAggregation;
import com.jack.aquark.entity.HourlyAggregation;
import com.jack.aquark.entity.SensorData;
import java.time.LocalDateTime;
import java.util.List;

public interface SensorDataService {
  void fetchAndSaveSensorData(String apiUrl);

  List<HourlyAggregation> getHourlyAverage(LocalDateTime start, LocalDateTime end);

  List<DailyAggregation> getDailyAverage(LocalDateTime start, LocalDateTime end);

  List<SensorData> getPeakTimeData(LocalDateTime start, LocalDateTime end);

  List<SensorData> getOffPeakTimeData(LocalDateTime start, LocalDateTime end);
}
