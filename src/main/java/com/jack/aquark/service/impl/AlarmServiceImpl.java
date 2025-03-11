package com.jack.aquark.service.impl;

import com.jack.aquark.entity.SensorData;
import com.jack.aquark.entity.SensorThreshold;
import com.jack.aquark.repository.SensorThresholdRepository;
import com.jack.aquark.service.AlarmService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AlarmServiceImpl implements AlarmService {
  private final SensorThresholdRepository thresholdRepository;

  @Override
  public void checkAndTriggerAlarms(SensorData data) {
    checkSensor("v1", data.getV1());
    checkSensor("v5", data.getV5());
    checkSensor("v6", data.getV6());
    checkSensor("rh", data.getRh());
    checkSensor("tx", data.getTx());
    checkSensor("echo", data.getEcho());
    checkSensor("rainD", data.getRainD());
    checkSensor("speed", data.getSpeed());
  }

  private void checkSensor(String sensorName, Double value) {
    if (value == null) return;
    SensorThreshold threshold = thresholdRepository.findBySensorName(sensorName);
    if (threshold != null && value > threshold.getThresholdValue()) {
      // Alarm triggered
      triggerAlarm(sensorName, value, threshold.getThresholdValue());
    }
  }

  private void triggerAlarm(String sensorName, Double sensorValue, Double thresholdValue) {
    System.out.println(
        "ALARM TRIGGERED for "
            + sensorName
            + ": value="
            + sensorValue
            + " exceeded threshold="
            + thresholdValue);

    // Optionally:
    // 1. Call a separate REST endpoint for alarm
    // 2. Send a Kafka event
    // kafkaTemplate.send("alarm-topic", "ALARM: " + sensorName + " -> " + sensorValue);
  }
}
