package com.jack.aquark.scheduler;

import com.jack.aquark.config.ApiUrlProperties;
import com.jack.aquark.config.SchedulingProperties;
import com.jack.aquark.entity.AlarmThreshold;
import com.jack.aquark.entity.HourlyAggregation;
import com.jack.aquark.entity.SensorData;
import com.jack.aquark.service.AggregationService;
import com.jack.aquark.service.AlarmThresholdService;
import com.jack.aquark.service.FetchedApiService;
import com.jack.aquark.service.SensorDataService;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
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
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final RedisTemplate<String, Object> redisTemplate;

  @Scheduled(cron = "${scheduling.cron}")
  public void fetchAggregateAndCheckAlarms() {
    log.info("=== Starting Scheduled Job ===");

    fetchNewDataFromApiUrls();

    aggregateHourlyData();

    checkSensorAlarms();

    log.info("=== Scheduled Job Finished ===");
  }

  private void fetchNewDataFromApiUrls() {
    apiUrlProperties
        .getUrls()
        .forEach(
            apiUrl -> {
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

  private void aggregateHourlyData() {
    log.info("Aggregating data for the last hour...");

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime oneHourAgo = now.minusHours(1);

    // Retrieve all sensor data for the last hour.
    List<SensorData> hourlyData = sensorDataService.getSensorDataByTimeRange(oneHourAgo, now);
    if (hourlyData.isEmpty()) {
      log.info("No sensor data available for the last hour.");
      return;
    }

    // Define sensor fields to aggregate.
    String[] sensors = {
      "v1", "v2", "v3", "v4", "v5", "v6", "v7", "rh", "tx", "echo", "rainD", "speed"
    };

    // Use a helper POJO to hold the aggregated result per sensor.
    Map<String, AggregationResult> aggregationResults = new LinkedHashMap<>();

    // Compute sum and average per sensor field.
    for (String sensor : sensors) {
      double sum = 0.0;
      int count = 0;
      for (SensorData data : hourlyData) {
        Double value =
            switch (sensor) {
              case "v1" -> data.getV1();
              case "v2" -> data.getV2();
              case "v3" -> data.getV3();
              case "v4" -> data.getV4();
              case "v5" -> data.getV5();
              case "v6" -> data.getV6();
              case "v7" -> data.getV7();
              case "rh" -> data.getRh();
              case "tx" -> data.getTx();
              case "echo" -> data.getEcho();
              case "rainD" -> data.getRainD();
              case "speed" -> data.getSpeed();
              default -> null;
            };
        if (value != null) {
          sum += value;
          count++;
        }
      }
      double avg = count == 0 ? 0.0 : sum / count;
      AggregationResult result = new AggregationResult(sensor, sum, avg);
      aggregationResults.put(sensor, result);
    }

    // Create HourlyAggregation records and save them.
    // Here, we let the AggregationService handle saving.
    LocalDateTime nowTime = LocalDateTime.now();
    for (Map.Entry<String, AggregationResult> entry : aggregationResults.entrySet()) {
      HourlyAggregation agg =
          HourlyAggregation.builder()
              .obsDate(nowTime.toLocalDate())
              .obsHour(nowTime.getHour())
              .sensorName(entry.getKey())
              .sumValue(entry.getValue().getSum())
              .avgValue(entry.getValue().getAvg())
              .build();

      // Save using AggregationService (which calls saveHourlyAggregation).
      aggregationService.saveHourlyAggregation(agg);

      // Also cache in Redis.
      String redisKey =
          "hourlyAgg:" + nowTime.toLocalDate() + ":" + nowTime.getHour() + ":" + entry.getKey();
      redisTemplate.opsForValue().set(redisKey, agg);
      log.info(
          "Saved hourly aggregation for {} at hour {} sensor {}: sum={}, avg={}",
          nowTime.toLocalDate(),
          nowTime.getHour(),
          entry.getKey(),
          entry.getValue().getSum(),
          entry.getValue().getAvg());
    }
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

  private void checkAndAlarm(String sensorName, Double value) {
    if (value == null) {
      return;
    }

    try {
      AlarmThreshold threshold = alarmThresholdService.getThreshold(sensorName);
      if (value > threshold.getThresholdValue()) {
        String message =
            String.format(
                "Alarm triggered! Sensor: %s, Value: %.2f, Threshold: %.2f",
                sensorName, value, threshold.getThresholdValue());
        kafkaTemplate.send("sensor_alarms", message);
        log.warn("Alarm sent: {}", message);
      }
    } catch (RuntimeException e) {
      log.debug("No threshold configured for sensor: {}. Skipping alarm check.", sensorName);
    }
  }

  @Data
  @AllArgsConstructor
  private static class AggregationResult {
    private String sensorName;
    private double sum;
    private double avg;
  }
}
