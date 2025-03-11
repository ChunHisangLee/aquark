package com.jack.aquark.service.impl;

import com.jack.aquark.entity.SensorThreshold;
import com.jack.aquark.repository.SensorThresholdRepository;
import com.jack.aquark.service.ThresholdService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ThresholdServiceImpl implements ThresholdService {
  private final SensorThresholdRepository thresholdRepository;

  @Override
  public List<SensorThreshold> getAllThresholds() {
    return thresholdRepository.findAll();
  }

  @Override
  public SensorThreshold getThresholdBySensor(String sensorName) {
    return thresholdRepository.findBySensorName(sensorName);
  }

  @Override
  public SensorThreshold saveThreshold(SensorThreshold threshold) {
    return thresholdRepository.save(threshold);
  }
}
