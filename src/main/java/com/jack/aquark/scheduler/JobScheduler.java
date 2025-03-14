package com.jack.aquark.scheduler;

import com.jack.aquark.config.ApiUrlProperties;
import com.jack.aquark.config.SchedulingProperties;
import com.jack.aquark.entity.SensorData;
import com.jack.aquark.service.AggregationService;
import com.jack.aquark.service.AlarmThresholdService;
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
              log.info("Fetching and saving sensor data from API URL {}...", apiUrl);
              sensorDataService.fetchAndSaveSensorData(apiUrl);
            });
  }

  private void checkSensorAlarms() {
    log.info(
        "Checking sensor alarms for the last {} minutes...",
        schedulingProperties.getIntervalMinutes());
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startTime = now.minusMinutes(schedulingProperties.getIntervalMinutes());
    List<SensorData> sensorDataList = sensorDataService.getSensorDataByTimeRange(startTime, now);
    for (SensorData data : sensorDataList) {
      checkAndAlarm(data, "v1", data.getV1());
      checkAndAlarm(data, "v2", data.getV2());
      checkAndAlarm(data, "v3", data.getV3());
      checkAndAlarm(data, "v4", data.getV4());
      checkAndAlarm(data, "v5", data.getV5());
      checkAndAlarm(data, "v6", data.getV6());
      checkAndAlarm(data, "v7", data.getV7());
      checkAndAlarm(data, "rh", data.getRh());
      checkAndAlarm(data, "tx", data.getTx());
      checkAndAlarm(data, "echo", data.getEcho());
      checkAndAlarm(data, "rainD", data.getRainD());
      checkAndAlarm(data, "speed", data.getSpeed());
    }
  }

  private void checkAndAlarm(SensorData data, String parameter, BigDecimal value) {
    if (value == null) return;
    try {
      var threshold =
          alarmThresholdService.getThreshold(data.getStationId(), data.getCsq(), parameter);
      if (value.compareTo(threshold.getThresholdValue()) > 0) {
        String message =
            String.format(
                "Alarm triggered for station %s, parameter %s, csq %s: value %s exceeds threshold %s",
                data.getStationId(),
                parameter,
                data.getCsq(),
                value.toPlainString(),
                threshold.getThresholdValue().toPlainString());
        log.warn("Alarm sent: {}", message);
      }
    } catch (RuntimeException e) {
      log.debug(
          "No threshold configured for station {} parameter {} with csq {}. Skipping alarm check.",
          data.getStationId(),
          parameter,
          data.getCsq());
    }
  }
}
