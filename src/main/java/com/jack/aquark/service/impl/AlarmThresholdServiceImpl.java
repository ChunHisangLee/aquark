package com.jack.aquark.service.impl;

import com.jack.aquark.entity.AlarmThreshold;
import com.jack.aquark.repository.AlarmThresholdRepository;
import com.jack.aquark.service.AlarmThresholdService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AlarmThresholdServiceImpl implements AlarmThresholdService {
  private final AlarmThresholdRepository alarmThresholdRepository;
  private final KafkaTemplate<String, String> kafkaTemplate;

  @Override
  public AlarmThreshold getThreshold(String sensorType) {
    return alarmThresholdRepository
        .findBySensorType(sensorType)
        .orElseThrow(() -> new RuntimeException("Threshold not found for " + sensorType));
  }

  @Override
  public AlarmThreshold updateThreshold(AlarmThreshold threshold) {
    AlarmThreshold savedThreshold = alarmThresholdRepository.save(threshold);
    String message =
        "Threshold updated for sensor: "
            + savedThreshold.getSensorType()
            + " new threshold: "
            + savedThreshold.getThreshold();
    kafkaTemplate.send("threshold_updates", message);
    log.info("Kafka notification sent: {}", message);
    return savedThreshold;
  }
}
