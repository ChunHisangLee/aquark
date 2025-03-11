package com.jack.aquark.service;

import com.jack.aquark.dto.RawDataWrapper;
import com.jack.aquark.dto.Summaries;
import com.jack.aquark.entity.SensorData;
import java.time.LocalDateTime;
import java.util.List;

public interface SensorDataService {
  void fetchAndSaveSensorData(String apiUrl);

  void saveRawData(RawDataWrapper wrapper);

  RawDataWrapper fetchRawDataFromUrl(String url);

  Summaries getSummaries(LocalDateTime start, LocalDateTime end);

  List<SensorData> getSensorDataBetween(LocalDateTime start, LocalDateTime end);

  List<Object[]> getHourlyAverage(LocalDateTime start, LocalDateTime end);
}
