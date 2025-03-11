package com.jack.aquark.controller;

import com.jack.aquark.dto.RawDataWrapper;
import com.jack.aquark.dto.Summaries;
import com.jack.aquark.entity.SensorData;
import com.jack.aquark.service.SensorDataService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sensor")
@AllArgsConstructor
public class SensorDataController {

  private final SensorDataService sensorDataService;

  /** 1. Receive entire JSON with "raw" array */
  @GetMapping("/fetchAndUpload")
  public String fetchAndUploadRawData(@RequestParam String stationId) {
    // Compose URL dynamically using the provided stationId
    String url = "https://app.aquark.com.tw/api/raw/Angle2024/" + stationId;
    RawDataWrapper wrapper = sensorDataService.fetchRawDataFromUrl(url);
    sensorDataService.saveRawData(wrapper);
    return "Fetched and saved successfully for station: " + stationId;
  }

  /** 查詢指定時間區間內的數據 */
  @GetMapping("/search")
  public List<SensorData> searchSensorData(
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
    return sensorDataService.getSensorDataBetween(start, end);
  }

  /** 查詢每小時平均（以 v1 為例） */
  @GetMapping("/statistics/hourly")
  public List<Object[]> getHourlyStats(
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
    return sensorDataService.getHourlyAverage(start, end);
  }

  /**
   * 2. Summaries (sum & average) in memory e.g.
   * /api/sensor/summaries?start=2025-01-10T00:00:00&end=2025-01-10T23:59:59
   */
  @GetMapping("/summaries")
  public Summaries getSummaries(
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
    return sensorDataService.getSummaries(start, end);
  }
}
