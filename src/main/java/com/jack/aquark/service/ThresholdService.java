package com.jack.aquark.service;

import com.jack.aquark.entity.SensorThreshold;
import java.util.List;

public interface ThresholdService {
  List<SensorThreshold> getAllThresholds();

  SensorThreshold getThresholdBySensor(String sensorName);

  SensorThreshold saveThreshold(SensorThreshold threshold);
}
