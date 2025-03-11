package com.jack.aquark.service.impl;

import com.jack.aquark.service.SensorDataService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

  private final SensorDataService sensorDataService;

  public ScheduledTasks(SensorDataService sensorDataService) {
    this.sensorDataService = sensorDataService;
  }

  // e.g. run every hour
  @Scheduled(cron = "0 0 * * * *")
  public void fetchSensorData() {
    //sensorDataService.fetchAndStoreSensorData();
  }
}
