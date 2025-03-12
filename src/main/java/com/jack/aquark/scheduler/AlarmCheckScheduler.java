package com.jack.aquark.scheduler;

import com.jack.aquark.entity.AlarmThreshold;
import com.jack.aquark.entity.SensorData;
import com.jack.aquark.service.AlarmThresholdService;
import com.jack.aquark.service.SensorDataService;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class AlarmCheckScheduler {

  private final SensorDataService sensorDataService;
  private final AlarmThresholdService alarmThresholdService;
  private final KafkaTemplate<String, String> kafkaTemplate;

  // This scheduled task runs every minute. Adjust the cron expression as needed.
  @Scheduled(cron = "${scheduling.cron}")
  public void checkSensorAlarms() {
    log.info("Checking sensor alarms...");
    // Retrieve the latest sensor data records.
    // Adjust this method in SensorDataService to return the records you wish to check.
    List<SensorData> sensorDataList = sensorDataService.getLatestSensorData();

    for (SensorData data : sensorDataList) {
      // Check each sensor field that you want to alarm on.
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
        String msg =
            String.format(
                "Alarm triggered! Sensor: %s, Value: %.2f, Threshold: %.2f",
                sensorName, value, threshold.getThresholdValue());
        kafkaTemplate.send("sensor_alarms", msg);
        log.warn("Alarm sent: {}", msg);
      }
    } catch (RuntimeException e) {
      // If threshold not found, ignore or log
      log.debug("No threshold for sensor: {}. Skipping alarm check.", sensorName);
    }
  }
}
