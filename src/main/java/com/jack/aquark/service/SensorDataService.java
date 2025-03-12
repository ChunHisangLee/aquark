package com.jack.aquark.service;

import com.jack.aquark.dto.RawDataWrapperDto;
import com.jack.aquark.dto.SummariesDto;
import com.jack.aquark.entity.SensorData;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface SensorDataService {
  void fetchAndSaveSensorData(String apiUrl);

  void saveRawData(RawDataWrapperDto wrapper);

  RawDataWrapperDto fetchRawDataFromUrl(String url);

  SummariesDto getSummaries(LocalDateTime start, LocalDateTime end);

  List<SensorData> getSensorDataByDate(LocalDate date);

  List<SensorData> getSensorDataBetween(LocalDateTime start, LocalDateTime end);

  List<Object[]> getHourlyAverage(LocalDateTime start, LocalDateTime end);

  List<SensorData> getLatestSensorData(double intervalMinutes);

  List<SensorData> getPeakTimeData(LocalDateTime start, LocalDateTime end);

  List<SensorData> getOffPeakTimeData(LocalDateTime start, LocalDateTime end);
}
