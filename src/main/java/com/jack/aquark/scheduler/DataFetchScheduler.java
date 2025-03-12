package com.jack.aquark.scheduler;

import com.jack.aquark.config.ApiUrlProperties;
import com.jack.aquark.service.AlarmThresholdService;
import com.jack.aquark.service.FetchedApiService;
import com.jack.aquark.service.SensorDataService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class DataFetchScheduler {

  private final SensorDataService sensorDataService;
  private final AlarmThresholdService alarmThresholdService;
  private final FetchedApiService fetchedApiService;
  private final ApiUrlProperties apiUrlProperties;

  // Use cron expression defined in application.yml
  @Scheduled(cron = "${scheduling.cron}")
  public void fetchDataTask() {
    apiUrlProperties.getUrls().forEach(apiUrl -> {
      if (!fetchedApiService.exists(apiUrl)) {
        log.info("API URL {} not found. Fetching and saving sensor data...", apiUrl);
        sensorDataService.fetchAndSaveSensorData(apiUrl);
        fetchedApiService.saveApiUrl(apiUrl);
        log.info("Data and API URL {} saved.", apiUrl);
      } else {
        log.info("API URL {} already exists. Skipping fetching.", apiUrl);
      }
    });
  }
}
