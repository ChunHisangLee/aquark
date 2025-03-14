package com.jack.aquark.scheduler;

import com.jack.aquark.config.ApiUrlProperties;
import com.jack.aquark.config.SchedulingProperties;
import com.jack.aquark.entity.AlarmThreshold;
import com.jack.aquark.entity.SensorData;
import com.jack.aquark.service.AggregationService;
import com.jack.aquark.service.AlarmThresholdService;
import com.jack.aquark.service.FetchedApiService;
import com.jack.aquark.service.SensorDataService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class JobScheduler {
  private final ApiUrlProperties apiUrlProperties;
  private final SchedulingProperties schedulingProperties;
  private final FetchedApiService fetchedApiService;
  private final SensorDataService sensorDataService;
  private final AggregationService aggregationService;
  private final AlarmThresholdService alarmThresholdService;

  @Scheduled(cron = "${scheduling.cron}")
  public void fetchAggregateAndCheckAlarms() {
    log.info("=== Starting Scheduled Job ===");

    fetchNewDataFromApiUrls();

    aggregationService.processTempDataForAggregations();

    checkSensorAlarms();

    log.info("=== Scheduled Job Finished ===");
  }

  private void fetchNewDataFromApiUrls() {
    apiUrlProperties
        .getUrls()
        .forEach(
            apiUrl -> {
              log.info("API URL {} not found. Fetching and saving sensor data...", apiUrl);
              sensorDataService.fetchAndSaveSensorData(apiUrl);
              fetchedApiService.saveApiUrl(apiUrl);
            });
  }

  private void checkSensorAlarms() {
    log.info(
        "Checking sensor alarms for the last {} minutes...",
        schedulingProperties.getIntervalMinutes());

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startTime = now.minusMinutes(schedulingProperties.getIntervalMinutes());

    // Retrieve sensor data in the calculated time range.
    List<SensorData> sensorDataList = sensorDataService.getSensorDataByTimeRange(startTime, now);

    // For each sensor record, check each sensor field for alarm conditions.
    for (SensorData data : sensorDataList) {
      checkAndAlarm("v1", data.getV1());
      checkAndAlarm("v2", data.getV2());
      checkAndAlarm("v3", data.getV3());
      checkAndAlarm("v4", data.getV4());
      checkAndAlarm("v5", data.getV5());
      checkAndAlarm("v6", data.getV6());
      checkAndAlarm("v7", data.getV7());
      checkAndAlarm("rh", data.getRh());
      checkAndAlarm("tx", data.getTx());
      checkAndAlarm("echo", data.getEcho());
      checkAndAlarm("rainD", data.getRainD());
      checkAndAlarm("speed", data.getSpeed());
    }
  }

  private void checkAndAlarm(String sensorName, BigDecimal value) {
    if (value == null) {
      return;
    }

    try {
      AlarmThreshold threshold = alarmThresholdService.getThreshold(sensorName);
      // Compare using BigDecimal's compareTo method.
      if (value.compareTo(threshold.getThresholdValue()) > 0) {
        String message =
            String.format(
                "Alarm triggered! Sensor: %s, Value: %s, Threshold: %s",
                sensorName, value.toPlainString(), threshold.getThresholdValue().toPlainString());
        log.warn("Alarm sent: {}", message);
      }
    } catch (RuntimeException e) {
      log.debug("No threshold configured for sensor: {}. Skipping alarm check.", sensorName);
    }
  }
}
