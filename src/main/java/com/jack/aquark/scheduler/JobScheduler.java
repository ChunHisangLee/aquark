package com.jack.aquark.scheduler;

import com.jack.aquark.config.ApiUrlProperties;
import com.jack.aquark.service.AggregationService;
import com.jack.aquark.service.SensorDataService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class JobScheduler {
  private final ApiUrlProperties apiUrlProperties;
  private final SensorDataService sensorDataService;
  private final AggregationService aggregationService;

  @Scheduled(cron = "${scheduling.cron}")
  public void fetchAndAggregate() {
    log.info("=== Starting Scheduled Job ===");

    fetchNewDataFromApiUrls();
    aggregationService.processTempDataForAggregations();

    log.info("=== Scheduled Job Finished ===");
  }

  private void fetchNewDataFromApiUrls() {
    apiUrlProperties
        .getUrls()
        .forEach(
            apiUrl -> {
              log.info("Fetching and saving sensor data from API URL {}...", apiUrl);
              sensorDataService.fetchAndSaveSensorData(apiUrl);
            });
  }
}
