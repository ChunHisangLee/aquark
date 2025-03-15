package com.jack.aquark.service;

import com.jack.aquark.dto.RawDataWrapperDto;
import com.jack.aquark.dto.SummariesDto;
import com.jack.aquark.entity.HourlyAggregation;
import com.jack.aquark.entity.SensorData;
import java.time.LocalDateTime;
import java.util.List;

public interface SensorDataService {
  void fetchAndSaveSensorData(String apiUrl);

  RawDataWrapperDto fetchRawDataFromUrl(String url);

  SummariesDto getSummaries(LocalDateTime start, LocalDateTime end);

  List<SensorData> getSensorDataByTimeRange(LocalDateTime start, LocalDateTime end);

  List<HourlyAggregation> getHourlyAverage(LocalDateTime start, LocalDateTime end);

  List<SensorData> getPeakTimeData(LocalDateTime start, LocalDateTime end);

  List<SensorData> getOffPeakTimeData(LocalDateTime start, LocalDateTime end);
}
