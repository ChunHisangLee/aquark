package com.jack.aquark.scheduler;

import com.jack.aquark.config.SchedulingProperties;
import com.jack.aquark.entity.SensorData;
import com.jack.aquark.entity.AlarmThreshold;
import com.jack.aquark.service.AlarmThresholdService;
import com.jack.aquark.service.SensorDataService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class AlarmCheckScheduler {

  private final SensorDataService sensorDataService;
  private final AlarmThresholdService alarmThresholdService;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final SchedulingProperties schedulingProperties;

  @Scheduled(cron = "${scheduling.cron}")
  public void checkSensorAlarms() {
    log.info("Checking sensor alarms...");

    // Calculate the start time based on intervalMinutes
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startTime = now.minusMinutes(schedulingProperties.getIntervalMinutes());

    // Retrieve sensor data in the calculated time range
    List<SensorData> sensorDataList = sensorDataService.getSensorDataBetween(startTime, now);

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
    if (value == null) return;
    try {
      AlarmThreshold threshold = alarmThresholdService.getThreshold(sensorName);
      if (value > threshold.getThresholdValue()) {
        String message = String.format(
                "Alarm triggered! Sensor: %s, Value: %.2f, Threshold: %.2f",
                sensorName, value, threshold.getThresholdValue()
        );
        kafkaTemplate.send("sensor_alarms", message);
        log.warn("Alarm sent: {}", message);
      }
    } catch (RuntimeException e) {
      log.debug("No threshold configured for sensor: {}. Skipping alarm check.", sensorName);
    }
  }
}
